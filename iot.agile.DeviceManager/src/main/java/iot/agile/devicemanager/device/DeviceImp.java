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
package iot.agile.devicemanager.device;

import java.util.HashMap;
import java.util.Map;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.Device;
import iot.agile.Protocol;

/**
 * @author dagi
 *
 * Agile Device implementation
 *
 */
public class DeviceImp implements Device {

  protected final Logger logger = LoggerFactory.getLogger(DeviceImp.class);

  /**
   * Bus name for AGILE BLE Device interface
   */
  private static final String AGILE_DEVICE_BASE_ID = "iot.agile.device.";

  /**
   * Bus path for AGILE BLE Device interface
   */
  protected static final String AGILE_DEVICE_BASE_BUS_PATH = "/iot/agile/Device/";
  /**
   * BLE Protocol imp DBus interface id
   */
  private static final String BLE_PROTOCOL_ID = "iot.agile.protocol.BLE";
  /**
   * BLE Protocol imp DBus interface path
   */
  private static final String BLE_PROTOCOL_PATH = "/iot/agile/protocol/BLE";

  /**
   * Device status
   */
  protected static final String CONNECTED = "Connected";

  protected static final String DISCONNECTED = "Disconnected";
  /**
   * Protocol
   */
  protected static final String BLUETOOTH_LOW_ENERGY = "BLE";
  /**
   * Device status TODO: Needs implementation Default : Disconnected
   */
  protected static String deviceStatus = DISCONNECTED;
  /**
   * GATT service and characteristics for TI Sensor Tag temperature service
   */
  private static final String TEMPERATURE = "Temperature";

  private static final String SENSOR_NAME = "SensorName";

  private static final String TEMP_GATT_SERVICE = "TemperatureService";

  private static final String TEMP_GATT_SERVICE_UUID = "f000aa00-0451-4000-b000-000000000000";

  private static final String TEMP_VALUE_GATT_CHARACTERSTICS = "TemperatureValueCharacterstics";

  private static final String TEMP_VALUE_GATT_CHARACTERSTICS_UUID = "f000aa01-0451-4000-b000-000000000000";

  private static final String TEMP_CONFIGURATION_GATT_CHARACTERSTICS = "TemperatureConfigurationCharacterstics";

  private static final String TEMP_CONFIGURATION_GATT_CHARACTERSTICS_UUID = "f000aa02-0451-4000-b000-000000000000";

  /**
   * Agile specific device ID
   */
  protected String deviceAgileID;
  /**
   * Device name
   */
  protected String deviceName;
  /**
   * Device ID
   *
   */
  protected String deviceID;
  /**
   * The protocol the device supports
   */
  protected String protocol;

  /**
   * The device protocol interface
   */
  protected Protocol deviceProtocol;
  /**
   * DBus connection
   */
  protected DBusConnection connection;

  protected String devicePath;
  
  /**
   *
   * @param deviceID the device address (MAC in BLE case)
   * @param deviceName discovered named of the device
   * @param protocol the protocol the device supports
   *
   * @throws DBusException
   */
  public DeviceImp(String deviceID, String deviceName, String protocol) throws DBusException {
    this.deviceName = deviceName;
    this.deviceID = deviceID;
    this.deviceAgileID = AGILE_DEVICE_BASE_ID + deviceName.trim();
    this.protocol = protocol;
    
    this.devicePath = AGILE_DEVICE_BASE_BUS_PATH + deviceName.trim();
    
    connection = dbusConnect();

    if (protocol.equals(BLUETOOTH_LOW_ENERGY)) {
      deviceProtocol = (Protocol) connection.getRemoteObject(BLE_PROTOCOL_ID, BLE_PROTOCOL_PATH,
              Protocol.class);
    }

    logger.debug("Exposed device {} {}", deviceAgileID, devicePath);

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  }

  /**
   *
   * @see iot.agile.protocol.ble.device.Device#Id()
   */
  public String Id() {
    return deviceAgileID;
  }

  /**
   * returns the name of the device
   */
  public String Name() {
    return deviceName;
  }

  /**
   * returns the status of the device
   */
  public String Status() {
    return deviceStatus;
  }

  /**
   * Returns the configuration of the devices
   */
  public String Configuration() {
    logger.debug("Device. Subscribe not implemented");
    return null;
  }

  /**
   * Returns the profile of the device
   */
  public String Profile() {
    logger.debug("Device. Subscribe not implemented");
    return null;
  }

  /**
   * Returns the last update of value
   */
  public int LastUpdate() {
    logger.debug("Device. LastUpdate not implemented");
    return 0;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.device.IDevice#Data()
   */
  public String Data() {
    logger.debug("Device. Data not implemented");
    return null;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.device.IDevice#Protocol()
   */
  public String Protocol() {
    return protocol;
  }

  /*
	 * @see iot.agile.devicemanager.device.Device#Connect()
   */
  public boolean Connect() {
    try {
      if (protocol.equals(BLUETOOTH_LOW_ENERGY) && deviceProtocol != null) {
        if (deviceProtocol.Connect(deviceID)) {
          deviceStatus = CONNECTED;
          logger.info("Device Connected {}", deviceID);
          return true;
        }
      } else {
        logger.debug("Protocol not supported: {}", protocol);
      }

    } catch (DBusException e) {
      e.printStackTrace();
    }

    return false;
  }

  /*
	 * @see iot.agile.devicemanager.device.Device#Disconnect()
   */
  public boolean Disconnect() {
    if (protocol.equals(BLUETOOTH_LOW_ENERGY) && deviceProtocol != null) {
      if (deviceProtocol.Disconnect(deviceID)) {
        deviceStatus = DISCONNECTED;
        logger.info("Device disconnected {}", deviceID);
        return true;
      }
    } else {
      logger.debug("Protocol not supported: {}", protocol);
    }

    return false;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.device.IDevice#Execute(java.lang.String)
   */
  public void Execute(String command) {
    logger.debug("Device. Execute not implemented");
  }

  /**
   *
   * Reads data from the given sensor
   *
   *
   */
  public String Read(String sensorName) {
    if (protocol.equals(BLUETOOTH_LOW_ENERGY) && deviceProtocol != null) {
      if (sensorName.equals(TEMPERATURE)) {
        if (deviceStatus.equals(CONNECTED)) {

          try {
            return deviceProtocol.Read(deviceID, getTemperatureProfile());
          } catch (DBusException e) {
            e.printStackTrace();
          }

        } else {
          logger.debug("BLE Device not connected: {}", deviceName);
          return "BLE Device not connected: " + deviceName;
        }
      } else {
        logger.debug("Sensor not supported: {}", sensorName);
        return "Sensor not supported: " + sensorName;
      }
    } else {
      logger.debug("Protocol not supported:: {}", protocol);
      return "Protocol not supported: " + protocol;
    }

    return null;
  }

  /**
   * Writes data into the given sensor
   *
   * @see iot.agile.protocol.ble.device.IDevice#Write()
   */
  public void Write() {
    logger.debug("Device. Write not implemented");
  }

  /**
   * @return the deviceAgileID
   */
  public String getDeviceAgileID() {
    return deviceAgileID;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.device.IDevice#Subscribe()
   */
  public void Subscribe() {
    logger.debug("Device. Subscribe not implemented");
  }

  /**
   *
   *
   * @see org.freedesktop.dbus.DBusInterface#isRemote()
   */
  public boolean isRemote() {
    return false;
  }

  /**
   *
   * *
   *
   * @see iot.agile.protocol.ble.device.IDevice#DropBus()
   */
  public void DropBus() {
  }

  private Map<String, String> getTemperatureProfile() {
    Map<String, String> profile = new HashMap<String, String>();
    profile.put(SENSOR_NAME, TEMPERATURE);
    profile.put(TEMP_GATT_SERVICE, TEMP_GATT_SERVICE_UUID);
    profile.put(TEMP_VALUE_GATT_CHARACTERSTICS, TEMP_VALUE_GATT_CHARACTERSTICS_UUID);
    profile.put(TEMP_CONFIGURATION_GATT_CHARACTERSTICS, TEMP_CONFIGURATION_GATT_CHARACTERSTICS_UUID);

    return profile;
  }

  @Override
  public DBusConnection dbusConnect() throws DBusException {

    DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);

    connection.requestBusName(deviceAgileID);
    connection.exportObject(devicePath, this);

    return connection;
  }

  @Override
  public void dbusDisconnect() {
    connection.disconnect();
  }

}
