/*
 * Copyright 2016 Dagmawi Neway Mekuria <d.mekuria@create-net.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package iot.agile.protocol.ble;

import iot.agile.Protocol;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tinyb.BluetoothDevice;
import tinyb.BluetoothException;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import tinyb.BluetoothManager;

/**
 * Agile Bluetooth Low Energy(BLE) Protocol implementation
 *
 * @author dagi
 *
 */
public class BLEProtocolImp implements Protocol {

  protected final Logger logger = LoggerFactory.getLogger(BLEProtocolImp.class);

  private final DBusConnection connection;

  /**
   * Bus name for AGILE BLE Protocol
   */
  private static final String AGILE_BLUETOOTH_BUS_NAME = "iot.agile.protocol.BLE";

  /**
   * Bus path for AGILE BLE Protocol
   */
  private static final String AGILE_BLUETOOTH_BUS_PATH = "/iot/agile/protocol/BLE";

  /**
   * Protocol name
   */
  private static final String PROTOCOL_NAME = "Bluetooth Low Energy";

  /**
   * Protocol driver name
   */
  private static final String DRIVER_NAME = "BLE";

  /**
   * The bluetooth manager
   */
  protected BluetoothManager bleManager;

  /**
   * Lists of device names TODO: Should return lists of devices in terms of Dbus
   * object
   */
  protected final List<String> deviceList = new ArrayList();

  protected String lastRead;

  /**
   * GATT Profile for TI SensorTag Temperature service
   *
   */
  private static final String TEMP_GATT_SERVICE = "TemperatureService";

  private static final String TEMP_VALUE_GATT_CHARACTERSTICS = "TemperatureValueCharacterstics";

  private static final String TEMP_CONFIGURATION_GATT_CHARACTERSTICS = "TemperatureConfigurationCharacterstics";

  private static final String SENSOR_NAME = "SensorName";

  private static final String TEMPERATURE = "Temperature";

  ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  protected final State state = new State();

  public class State {
    public boolean isDiscovering = false;
  }

  public static void main(String[] args) throws DBusException {
    Protocol bleProtocol = new BLEProtocolImp();
  }

  public BLEProtocolImp() throws DBusException {

    this.connection = DBusConnection.getConnection(DBusConnection.SESSION);

    connection.requestBusName(AGILE_BLUETOOTH_BUS_NAME);
    connection.exportObject(AGILE_BLUETOOTH_BUS_PATH, this);

    try {
      bleManager = BluetoothManager.getBluetoothManager();
    } catch (BluetoothException bex) {
      logger.error("No bluetooth adapter found on the system", bex);
    } catch (Exception e) {
      logger.error("Error getting BluetoothManager instance", e);
    }
    
    // ensure DBus object is unregistered
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          connection.releaseBusName(AGILE_BLUETOOTH_BUS_NAME);
        } catch (DBusException ex) {
          logger.error("Cannot release DBus name {}", AGILE_BLUETOOTH_BUS_NAME, ex);
        }
      }
    });

    logger.debug("BLE Protocol is running");

  }

  /**
   * Returns lists of devices
   */
  @Override
  public List<String> Devices() {
    return deviceList;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.Protocol#protocolStatus()
   */
  public void ProtocolStatus() {
    logger.debug("Protocol.ProtocolStatus not implemented");
  }

  /**
   *
   * @see iot.agile.protocol.ble.Protocol#driver()
   */
  @Override
  public String Driver() {
    return DRIVER_NAME;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.Protocol#name()
   */
  @Override
  public String Name() {
    return PROTOCOL_NAME;
  }

  /**
   * Connect BLE Device
   *
   * @param deviceAddress
   * @see iot.agile.protocol.ble.Protocol#initialize(java.lang.String)
   */
  @Override
  public boolean Connect(String deviceAddress) {
    logger.debug("Connecting to BLE device {}", deviceAddress);
    BluetoothDevice bleDevice;
    try {
      bleDevice = getDevice(deviceAddress);
      if (bleDevice.connect()) {
        return true;
      }
    } catch (InterruptedException e) {
      logger.error("Failed to connect: {}", deviceAddress, e);
    }
    return false;
  }

  /**
   *
   * Disconnect bluetooth device
   *
   * @return
   * @see iot.agile.protocol.ble.Protocol#destory(java.lang.String)
   */
  @Override
  public boolean Disconnect(String deviceAddress) {
    logger.debug("Disconntectin from BLE device {}", deviceAddress);
    BluetoothDevice bleDevice;
    try {
      bleDevice = getDevice(deviceAddress);
      if (bleDevice != null) {
        return bleDevice.disconnect();
      }
    } catch (InterruptedException e) {
      logger.error("Failed to disconnect {}", deviceAddress, e);
    }
    return false;
  }

  /**
   * Discover BLE devices
   */
  @Override
  public void Discover() {

    logger.debug("Started discovery of BLE devices");

    Runnable task = () -> {
      
      logger.debug("Checking for new devices");
      bleManager.startDiscovery();      

      int newDevices = 0;
      List<BluetoothDevice> list = bleManager.getDevices();
      for (BluetoothDevice device : list) {
        if (!deviceList.contains(device.getName())) {
          deviceList.add(device.getName());
          printDevice(device);
          newDevices++;
        }
      }

      if (newDevices > 0) {
        logger.debug("Found {} new device(s)", newDevices);
      }

    };

    ScheduledFuture future = executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
    try {
      future.get(10, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException ex) {
      logger.debug("Aborted execution scheduler: {}", ex.getMessage());
    } finally {
      logger.debug("Stopped BLE discovery");
    }

  }

  /**
   * @see iot.agile.protocol.ble.Protocol#protocolProfile()
   */
  public void ProtocolProfile() {
    logger.debug("Protocol.ProtocolProfile not implemented");
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.Protocol#Status()
   */
  @Override
  public String Status() {
    logger.debug("Protocol.Status not implemented");
    return null;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.Protocol#execute()
   */
  public void Execute(String... executeParams) {
    logger.debug("Protocol.Execute not implemented");

  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.Protocol#write()
   */
  @Override
  public void Write() {
    logger.debug("Protocol.Write not implemented");
  }

  /**
   *
   *
   * @param profile
   * @see iot.agile.protocol.ble.Protocol#read()
   */
  public String Read(String deviceAddress, Map<String, String> profile) throws DBusException {
    BluetoothDevice device;
    try {
      device = getDevice(deviceAddress);
      if (device == null) {
        throw new DBusException("Device not found");
      }
      if (device.getConnected() == false) {
        return "Device not connected";
      } else if (profile.get(SENSOR_NAME).equals(TEMPERATURE)) {

        BluetoothGattService tempService = getService(device, profile.get(TEMP_GATT_SERVICE));
        if (tempService == null) {
          throw new DBusException("This device does not have the temperature service");
        } else {
          BluetoothGattCharacteristic tempValue = getCharacteristic(tempService,
                  profile.get(TEMP_VALUE_GATT_CHARACTERSTICS));
          BluetoothGattCharacteristic tempConfig = getCharacteristic(tempService,
                  profile.get(TEMP_CONFIGURATION_GATT_CHARACTERSTICS));

          if (tempValue == null || tempConfig == null) {
            throw new DBusException("Could not find the correct characterstics");
          }

          /**
           * Turn on the temperature service by writing One in the configuration
           * characteristics
           */
          byte[] config = {0x01};
          tempConfig.writeValue(config);
          Thread.sleep(1000);
          tempConfig.writeValue(config);

          /**
           * Read the temperature value from value characteristics and convert
           * it to human readable format
           */
          byte[] tempRaw = tempValue.readValue();

          /*
						 * The temperature service returns the data in an
						 * encoded format which can be found in the wiki.
						 * Convert the raw temperature format to celsius and
						 * print it. Conversion for object temperature depends
						 * on ambient according to wiki, but assume result is
						 * good enough for our purposes without conversion.
           */
          int objectTempRaw = (tempRaw[0] & 0xff) | (tempRaw[1] << 8);
          int ambientTempRaw = (tempRaw[2] & 0xff) | (tempRaw[3] << 8);

          float objectTempCelsius = convertCelsius(objectTempRaw);
          float ambientTempCelsius = convertCelsius(ambientTempRaw);
          lastRead = String.format(" Temp: Object = %fC, Ambient = %fC", objectTempCelsius,
                  ambientTempCelsius);
          return lastRead;
        }

      }

    } catch (InterruptedException e) {
      logger.error("InterruptedException occured", e);
      throw new DBusException("Operation interrupted abnormally");
    }

    return null;
  }

  public void Receive(String args) throws DBusException {
    logger.debug("Protocol.Receive not implemented");
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.Protocol#subscribe(java.lang.String[])
   */
  @Override
  public void Subscribe(String... subscribeParams) {
    logger.debug("Protocol.Receive not implemented");
  }

  /**
   * @see iot.agile.protocol.ble.Protocol#DataStore()
   */
  @Override
  public String Data() {
    return lastRead;
  }

  public boolean isRemote() {
    return false;
  }

  // ==========Testing and Utility=============================
  // ================ Methods==================================
  /**
   * Disconnect the bus, and drop the dbus interface
   */
  public void DropBus() {
    connection.disconnect();
  }

  void printDevice(BluetoothDevice device) {
    logger.debug("Address = {}", device.getAddress());
    logger.debug("Name = {}", device.getName());
    logger.debug("Connected= {}", device.getConnected());
  }

  private BluetoothDevice getDevice(String address) throws InterruptedException {
    BluetoothDevice bleDevice = null;
    List<BluetoothDevice> list = bleManager.getDevices();
    for (BluetoothDevice device : list) {
      if (device.getAddress().equals(address)) {
        bleDevice = device;
      }
    }
    return bleDevice;
  }

  @Override
  public void finalize() {
    connection.disconnect();
  }

  /**
   * (non-Javadoc)
   *
   * @see iot.agile.protocol.ble.Protocol#StopDiscovery()
   */
  @Override
  public void StopDiscovery() {
    bleManager.stopDiscovery();
  }

  // =====================================UTILITY METHODS==============
  /**
   * Returns a Bluetooth GATT service from the given device based on the UUID of
   * the service
   *
   * @param device The device id
   * @param UUID service UUID
   * @return
   * @throws InterruptedException
   */
  private BluetoothGattService getService(BluetoothDevice device, String UUID) {
    BluetoothGattService service = null;
    List<BluetoothGattService> bluetoothServices = null;
    bluetoothServices = device.getServices();
    if (bluetoothServices == null) {
      return null;
    }

    for (BluetoothGattService gattservice : bluetoothServices) {
      if (gattservice.getUuid().equals(UUID)) {
        service = gattservice;
      }
    }
    return service;
  }

  /**
   * Returns a GATT characteristics from the given GATT service based on the
   * given UUID
   *
   * @param service The GATT Service
   *
   * @param UUID The required GATT characteristics UUID
   *
   * @return
   */
  private BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String UUID) {
    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
    if (characteristics == null) {
      return null;
    }

    for (BluetoothGattCharacteristic characteristic : characteristics) {
      if (characteristic.getUuid().equals(UUID)) {
        return characteristic;
      }
    }
    return null;
  }

  /**
   * Converts temperature into degree Celsius
   *
   * @param raw
   * @return
   */
  private float convertCelsius(int raw) {
    return raw / 128f;
  }

}
