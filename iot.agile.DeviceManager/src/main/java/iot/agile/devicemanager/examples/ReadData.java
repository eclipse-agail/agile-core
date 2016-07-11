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

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.Device;

/**
 * @author dagi
 * 
 *         This program demonstrates how client programs read data from a BLE
 *         Device through DBus, this example specifically shows how to read
 *         temperature value from TI Sensor Tag
 * 
 *         NOTE: A device should be discovered, registered and connected before
 *         reading data
 * 
 *
 */
public class ReadData {
	protected final static Logger logger = LoggerFactory.getLogger(ReadData.class);

	/**
	 * Bus name for AGILE BLE Device interface
	 */
	private static String deviceAgileID = "iot.agile.device.TISensorTag";

	/**
	 * Bus path for AGILE BLE Device interface
	 */
	private static String deviceAgileBusPath = "/iot/agile/Device/TISensorTag";
	/**
	 * Sensor name
	 */
	private static String service = "Temperature";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			if (isValidDeviceID(args[0])) {
				deviceAgileID = args[0];
				deviceAgileBusPath = "/iot/agile/Device/" + args[0].split(".")[3];
			} else {
				logger.info("Invalid device ID, Using default value for TI-Sensor Tag:"+deviceAgileID);
			}
			service = args[1];
		} else {
			logger.info("Invalid argument size, Using default values");
		}

		try {
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			Device sensorTag = (Device) connection.getRemoteObject(deviceAgileID, deviceAgileBusPath, Device.class);
			String currentTemp = sensorTag.Read(service);
			logger.info("Temperature: {}", currentTemp);
		} catch (DBusException e) {
			e.printStackTrace();
		}

	}

	private static boolean isValidDeviceID(String deviceID) {
		String[] idParts = deviceID.split(".");
		if (idParts.length == 4) {
			if ((idParts[0].equals("iot")) && (idParts[2].equals("agile")) && (idParts[3].equals("device"))) {
				return true;
			}
		}
		return false;

	}
}
