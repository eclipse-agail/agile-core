/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/

package org.eclipse.agail.devicemanager.examples;

import java.util.ArrayList;
import java.util.List;

import org.freedesktop.DBus.Error.ServiceUnknown;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.DeviceManager;
import org.eclipse.agail.object.DeviceComponent;
import org.eclipse.agail.object.DeviceDefinition;
import org.eclipse.agail.object.DeviceOverview;

/**
 * @author dagi
 *
 */
public class RegisterDevice {

  protected final static Logger logger = LoggerFactory.getLogger(RegisterDevice.class);
  /**
   * DBus interface name for the device manager
   */
  private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_NAME = "org.eclipse.agail.DeviceManager";
  /**
   * DBus interface path for the device manager
   */
  private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_PATH = "/org/eclipse/agail/DeviceManager";

  public static final String PROTOCOL_ID = "org.eclipse.agail.protocol.BLE";

  private static String deviceAddress = "C4:BE:84:70:69:09";

  private static String deviceName = "MedicalDevice";

  public static void main(String[] args) {
    checkUserInput(args);
    try {
      // Get the device manager DBus interface
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
      DeviceManager deviceManager = (DeviceManager) connection.getRemoteObject(AGILE_DEVICEMANAGER_MANAGER_BUS_NAME,
          AGILE_DEVICEMANAGER_MANAGER_BUS_PATH, DeviceManager.class);
      
      // Register device
      DeviceDefinition dev = (deviceManager
          .Register(new DeviceOverview(deviceAddress, PROTOCOL_ID, deviceName, ""), "MedicalDevice"));
      logger.info("Device ID: {}", dev.deviceId);
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

  private static List<DeviceComponent> getDeviceStreams() {
    List<DeviceComponent> streamList = new ArrayList<DeviceComponent>();
    streamList.add(new DeviceComponent("Temperature", "Celsius"));
    streamList.add(new DeviceComponent("Accelerometer", "MeterPerSecond"));
    streamList.add(new DeviceComponent("Humidity", "Percent"));
    return streamList;
  }
}
