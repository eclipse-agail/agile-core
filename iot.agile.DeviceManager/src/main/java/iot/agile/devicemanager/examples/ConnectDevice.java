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
 *         This program demonstrates how client programs connect to a BLE
 *         Device(i.e TI Sensor Tag) through DBus
 * 
 *         NOTE: A device should be discovered and registered before gets
 *         connected
 * 
 *
 */
public class ConnectDevice {

	protected final static Logger logger = LoggerFactory.getLogger(ConnectDevice.class);

	/**
	 * Bus name for AGILE BLE Device interface
	 *  Default ID Sensor tag
	 */
	private static String deviceAgileID = "iot.agile.device.TISensorTag";

	/**
	 * Bus path for AGILE BLE Device interface
	 * Default Path : Sensor tag
	 */
	private static  String deviceAgileBusPath = "/iot/agile/Device/TISensorTag";

	public static void main(String[] args) {

		if (args.length == 1) {
			if(isValidDeviceID(args[0])){
				deviceAgileID = args[0].trim();
				deviceAgileBusPath ="/iot/agile/Device/"+args[0].split(".")[3];
			}else {
				logger.info("Invalid device Agile ID, Using default value for TI-SensorTag:"+deviceAgileID);
			}
		}else{
			logger.info("Invalid argument size, Using default values");
		}

		try {
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			Device sensorTag = (Device) connection.getRemoteObject(deviceAgileID, deviceAgileBusPath,
					Device.class);
			if (sensorTag.Connect()) {
				logger.info("Device Connected: {}", sensorTag.Name());
			} else {
				logger.info("Falied to connect : {}", sensorTag.Name());
			}
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
