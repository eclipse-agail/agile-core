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

import iot.agile.Device;
import iot.agile.Protocol;
import java.util.HashMap;
import java.util.Map;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final String AGILE_DEVICE_BASE_BUS_PATH = "/iot/agile/Device/";
  /**
   * BLE Protocol imp DBus interface id
   */
  private static final String BLE_PROTOCOL_ID = "iot.agile.protocol.BLE";
  /**
   * BLE Protocol imp DBus interface path
   */
  private static final String BLE_PROTOCOL_PATH = "/iot/agile/protocol/ble";

  /**
   * Device status
   */
  private static final String CONNECTED = "Connected";

  private static final String DISCONNECTED = "Disconnected";
  /**
   * Protocol
   */
  private static final String BLUETOOTH_LOW_ENERGY = "BLE";
  /**
   * Device status TODO: Needs implementation Default : Disconnected
   */
  private static String deviceStatus = DISCONNECTED;
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
   * Agile specific device ID TODO:
   */
  private String deviceAgileID;
  /**
   * Device ID
   *
   */
  private String deviceName;

  private String deviceID;

  private String protocol;
  
  protected final DBusConnection connection;
  
  /**
   *
   * @param deviceName Device Name
   */
  public DeviceImp(String deviceID, String deviceName, String protocol) throws DBusException {
    
    this.deviceName = deviceName;
    this.deviceID = deviceID;
    this.deviceAgileID = AGILE_DEVICE_BASE_ID + deviceName.trim();
    this.protocol = protocol;
    
    String devicePath = AGILE_DEVICE_BASE_BUS_PATH + deviceName.trim();
    
    
    connection = DBusConnection.getConnection(DBusConnection.SESSION);
    connection.requestBusName(deviceAgileID);
    connection.exportObject(devicePath, this);

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
    // TODO
    return null;
  }

  /**
   * Returns the profile of the device
   */
  public String Profile() {
    // TODO
    return null;
  }

  /**
   * Returns the last update of value
   */
  public int LastUpdate() {
    // TODO
    return 0;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.device.IDevice#Data()
   */
  public String Data() {
    // TODO
    return null;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.device.IDevice#Protocol()
   */
  public String Protocol() {
    // TODO
    return protocol;
  }

  /*
	 * @see iot.agile.devicemanager.device.Device#Connect()
   */
  public boolean Connect() {
    try {
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
      if (protocol.equals(BLUETOOTH_LOW_ENERGY)) {
        Protocol ble = (Protocol) connection.getRemoteObject(
                BLE_PROTOCOL_ID, 
                BLE_PROTOCOL_PATH,
                Protocol.class);
        if (ble.Connect(deviceID)) {
          deviceStatus = CONNECTED;
          System.out.println("Device CONnected");
          return true;
        }
      } else {
        System.err.println("Protocol not supported:" + protocol);
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
    try {
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
      if (protocol.equals(BLUETOOTH_LOW_ENERGY)) {
        Protocol ble = (Protocol) connection.getRemoteObject(BLE_PROTOCOL_ID, BLE_PROTOCOL_PATH,
                Protocol.class);
        if (ble.Disconnect(deviceID)) {
          deviceStatus = DISCONNECTED;
          return true;
        }
      } else {
        System.err.println("Protocol not supported:" + protocol);
      }

    } catch (DBusException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.device.IDevice#Execute(java.lang.String)
   */
  public void Execute(String command) {
    // TODO

  }

  /**
   *
   * Reads data from the given sensor
   *
   *
   */
  public String Read(String sensorName) {
    if (protocol.equals(BLUETOOTH_LOW_ENERGY)) {
      if (sensorName.equals(TEMPERATURE)) {
        if (deviceStatus.equals(CONNECTED)) {

          try {
            DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
            Protocol ble = (Protocol) connection.getRemoteObject(BLE_PROTOCOL_ID, BLE_PROTOCOL_PATH,
                    Protocol.class);

            return ble.Read(deviceID, getTemperatureProfile());
          } catch (DBusException e) {
            e.printStackTrace();
          }

        } else {
          return "BLE Device not connected: " + deviceName;
        }
      } else {
        return "Sensor not supported: " + sensorName;
      }
    } else {
      return "Protocol not supported: " + protocol;
    }

    return null;
  }

  /**
   * @return the deviceAgileID
   */
  public String getDeviceAgileID() {
    return deviceAgileID;
  }

  /**
   * Writes data into the given sensor
   *
   * @see iot.agile.protocol.ble.device.IDevice#Write()
   */
  public void Write() {
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.device.IDevice#Subscribe()
   */
  public void Subscribe() {
    // TODO
  }

  /**
   *
   *
   * @see org.freedesktop.dbus.DBusInterface#isRemote()
   */
  public boolean isRemote() {
    // TODO
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

}
