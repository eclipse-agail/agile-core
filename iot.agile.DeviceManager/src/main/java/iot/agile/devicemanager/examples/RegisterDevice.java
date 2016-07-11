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

import org.freedesktop.DBus.Error.ServiceUnknown;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.DeviceManager;

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

	private static String deviceMACAddress = "78:C5:E5:6E:E4:CF";

	private static String deviceName = "TISensorTag";

	public static void main(String[] args) {
		if (args.length == 1) {
			if (isValidMACAddress(args[0])) {
				deviceMACAddress = args[0];
			} else {
				logger.info("Invalid MAC Address, Using default value for TI-SensorTag:" + deviceMACAddress);
			}
		} else if (args.length == 2) {
			if (isValidMACAddress(args[0])) {
				deviceMACAddress = args[0];
			} else {
				logger.info("Invalid MAC Address, Using default value for TI-SensorTag:" + deviceMACAddress);
			}
			if (isValidDeviceName(args[1])) {
				deviceName = args[1];
			} else {
				logger.info("Invalid device name, Using default value:" + deviceName);
			}
		} else {
			logger.info("Invalid argument size, Using default values");
		}
		// DBus connection
		try {
			// Get the device manager DBus interface
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			DeviceManager deviceManager = (DeviceManager) connection.getRemoteObject(
					AGILE_DEVICEMANAGER_MANAGER_BUS_NAME, AGILE_DEVICEMANAGER_MANAGER_BUS_PATH, DeviceManager.class);
			// Register device
			String deviceAgileID = deviceManager.Create(deviceMACAddress, deviceName, "BLE");
			logger.info(deviceManager.devices().get(deviceMACAddress));
			logger.info("Device ID: {}", deviceAgileID);

		} catch (ServiceUnknown e) {
			logger.error("Can not find the DBus object : {}", AGILE_DEVICEMANAGER_MANAGER_BUS_NAME, e);
		} catch (DBusException e) {
			logger.error("Error in registering device :", e);
		}
	}

	private static boolean isValidMACAddress(String address) {
		if (address.trim().length() == 12) {
			if (address.split(":").length == 6) {
				return true;
			}
		}
		return false;
	}

	private static boolean isValidDeviceName(String deviceName) {
		if ((deviceName != null) && (deviceName.trim().length() != 0)) {
			return true;
		} else {
			return false;
		}
	}
}
