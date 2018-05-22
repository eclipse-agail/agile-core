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

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.Device;

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
	private static final String AGILE_DEVICE_BASE_ID = "org.eclipse.agail.device";
	protected static final String AGILE_DEVICE_BASE_BUS_PATH = "/org/eclipse/agail/Device/";
	protected final static Logger logger = LoggerFactory.getLogger(ConnectDevice.class);

	/**
	 * Bus name for AGILE BLE Device interface Default ID Sensor tag
	 */
	private static String deviceAgileID = "org.eclipse.agail.device.TISensorTag";

	/**
	 * Bus path for AGILE BLE Device interface Default Path : Sensor tag
	 */
	private static String deviceAgileBusPath = "/org/eclipse/agail/Device/TISensorTag";

	public static void main(String[] args) {

		if (args.length == 1) {
			if (isValidDeviceID(args[0])) {
				deviceAgileID = args[0].trim();
				deviceAgileBusPath = "/org/eclipse/agail/Device/" + args[0].split("\\.")[3];
			} else {
				logger.info("Invalid device Agile ID, Using default value for TI-SensorTag:" + deviceAgileID);
			}
		} else {
			logger.info("Invalid argument size, Using default values");
		}

		String devicePath = AGILE_DEVICE_BASE_BUS_PATH + "BLE" + "/" + "78:C5:E5:6E:E4:CF".replace(":", "");

		try {
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			Device sensorTag = (Device) connection.getRemoteObject("org.eclipse.agail.device",
					"/org/eclipse/agail/Device/BLE/78C5E56EE4CF", Device.class);
			sensorTag.Connect();
			logger.info("Device Connected: {}", sensorTag.Name());
		} catch (DBusException e) {
			logger.info("Falied to connect : ");
			e.printStackTrace();
		}

	}

	private static boolean isValidDeviceID(String deviceID) {
		String[] idParts = deviceID.split("\\.");

		if (idParts.length == 4) {
			if ((idParts[0].equals("iot")) && (idParts[1].equals("agile")) && (idParts[2].equals("device"))) {
				return true;
			}
		}
		return false;

	}
}
