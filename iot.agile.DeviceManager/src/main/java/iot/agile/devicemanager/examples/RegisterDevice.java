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

import iot.agile.agile.interfaces.DeviceManager;

/**
 * @author dagi
 *
 */
public class RegisterDevice {

	/**
	 * DBus interface name for the device manager
	 */
	private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_NAME = "iot.agile.DeviceManger";
	/**
	 * DBus interface path for the device manager
	 */
	private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_PATH = "/iot/agile/DeviceManager";

	public static void main(String[] args) {
		// DBus connection
		try {
			//Get the device manager DBus interface
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			DeviceManager deviceManager = (DeviceManager) connection.getRemoteObject(
					AGILE_DEVICEMANAGER_MANAGER_BUS_NAME, AGILE_DEVICEMANAGER_MANAGER_BUS_PATH, DeviceManager.class);
			//Register device
			String deviceAgileID = deviceManager.Create("C4:BE:84:70:69:09", "TISensorTag", "BLE");
			System.out.println(deviceManager.devices().get("C4:BE:84:70:69:09"));
			System.out.println("Device ID:" + deviceAgileID);

		}  catch (ServiceUnknown e) {
			System.err.println("Can not find the DBus object " + AGILE_DEVICEMANAGER_MANAGER_BUS_NAME);
		}catch (DBusException e) {
			e.printStackTrace();
		}
	}

}
