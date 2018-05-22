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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.Device;

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
	private static String agileDeviceObjName = "org.eclipse.agail.Device";

	/**
	 * Bus path for AGILE BLE Device interface
	 */
	private static String agileDeviceObjectPath = "/org/eclipse/agail/Device/ble";
	/**
	 * Sensor name
	 */
	private static String serviceO = "Optical";
	private static String serviceH = "Humidity";
	private static String serviceT = "Temperature";
	private static String serviceP = "Pressure";

	private static String address = "A0:E6:F8:B6:23:04";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String devicePath = agileDeviceObjectPath + address.replace(":", "");
		try {
			final DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			final Device sensorTag = (Device) connection.getRemoteObject(agileDeviceObjName, devicePath, Device.class);

			// Single read every one second
			while (true) {
				long startT = System.currentTimeMillis();
				try {
					new Thread(new Runnable() {
						@Override
						public void run() {
							logger.info("{}: {}", serviceO, sensorTag.Read(serviceO).getValue());
							logger.info("Read after: {} milliseconds", (System.currentTimeMillis() - startT));
						}
					}).start();

					new Thread(new Runnable() {
						@Override
						public void run() {
							logger.info("{}: {}", serviceO, sensorTag.Read(serviceO).getValue());
							logger.info("Read after: {} milliseconds", (System.currentTimeMillis() - startT));
						}
					}).start();
					Thread.sleep(100);
				} catch (Exception e) {
					logger.info("Failed after: {} seconds", (System.currentTimeMillis() - startT) / 1000);
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void checkInput(String[] input) {
		if (input.length == 2) {
			serviceP = input[0];
			if (isValidMACAddress(input[1])) {
				address = input[1];
			} else {
				logger.error("invalid device address, using default sensor tag address: {}", address);
			}
		} else {
			logger.error("Invalid input reading from default service {}", serviceP);

		}
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
}
