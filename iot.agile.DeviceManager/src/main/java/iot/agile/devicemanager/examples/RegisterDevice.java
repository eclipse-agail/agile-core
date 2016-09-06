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
package iot.agile.devicemanager.examples;

import java.util.ArrayList;
import java.util.List;

import org.freedesktop.DBus.Error.ServiceUnknown;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.DeviceManager;
import iot.agile.object.DeviceComponet;
import iot.agile.object.DeviceDefinition;

/**
 * @author dagi
 *
 */
public class RegisterDevice {

  protected final static Logger logger = LoggerFactory.getLogger(RegisterDevice.class);
  /**
   * DBus interface name for the device manager
   */
  private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_NAME = "iot.agile.DeviceManager";
  /**
   * DBus interface path for the device manager
   */
  private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_PATH = "/iot/agile/DeviceManager";

  public static final String PROTOCOL_ID = "iot.agile.protocol.BLE";

  private static String deviceAddress = "C4:BE:84:70:69:09";

  private static String deviceName = "TISensorTag";

  public static void main(String[] args) {
    checkUserInput(args);
    try {
      // Get the device manager DBus interface
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
      DeviceManager deviceManager = (DeviceManager) connection.getRemoteObject(AGILE_DEVICEMANAGER_MANAGER_BUS_NAME,
          AGILE_DEVICEMANAGER_MANAGER_BUS_PATH, DeviceManager.class);

      // Register device
      String deviceAgileID = (deviceManager
          .Create(new DeviceDefinition("",deviceAddress, deviceName, "",PROTOCOL_ID,  "", getDeviceStreams()))).get("id");
      logger.info("Device ID: {}", deviceAgileID);
    } catch (org.freedesktop.DBus.Error.UnknownMethod UM) {
      logger.debug("Unkown method");
    } catch (ServiceUnknown e) {
      logger.error("Can not find the DBus object : {}", AGILE_DEVICEMANAGER_MANAGER_BUS_NAME, e);
    } catch (DBusException e) {
      logger.error("Error in registering device :", e);
    }

  }

  // Utility methods

  private static void checkUserInput(String[] args) {
    if (args.length == 1) {
      if (isValidMACAddress(args[0])) {
        deviceAddress = args[0];
      } else {
        logger.info("Invalid MAC Address, Using default value for TI-SensorTag:" + deviceAddress);
      }
    }
    // TODO Check other parameters
  }

  /**
   * 
   * @param address
   * @return
   */
  private static boolean isValidMACAddress(String address) {
    if (address.trim().toCharArray().length == 17) {
      if (address.split(":").length == 6) {
        return true;
      }
    }
    return false;
  }

  private static List<DeviceComponet> getDeviceStreams() {
    List<DeviceComponet> streamList = new ArrayList<DeviceComponet>();
    streamList.add(new DeviceComponet("Temperature", "Celsius"));
    streamList.add(new DeviceComponet("Accelerometer", "MeterPerSecond"));
    streamList.add(new DeviceComponet("Humidity", "Percent"));
    return streamList;
  }
}
