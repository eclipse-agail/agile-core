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
package iot.agile.protocolmanager.example;

import org.freedesktop.DBus.Error.ServiceUnknown;
import org.freedesktop.dbus.DBusConnection;

import iot.agile.agile.interfaces.ProtocolManager;

/**
 * 
 * @author dagi
 * 
 *         This program demonstrates how client programs discoverBLE devices
 *         through DBus using {@code ProtocolManager},
 * 
 * 
 *
 */

public class BLEDiscovery {
	/**
	 * DBus bus name for the protocol manager
	 */
	private static final String AGILE_PROTOCOL_MANAGER_BUS_NAME = "iot.agile.ProtocolManager";
	/**
	 * DBus bus path for the protocol manager
	 */
	private static final String AGILE_PROTOCOL_MANAGER_BUS_PATH = "/iot/agile/ProtocolManager";

	public static void main(String[] args) {
		try {
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			// Get Agile protocol manger interfaces from DBbus
			ProtocolManager protocolManager = connection.getRemoteObject(AGILE_PROTOCOL_MANAGER_BUS_NAME,
					AGILE_PROTOCOL_MANAGER_BUS_PATH, ProtocolManager.class);
			// Start device discovery
			protocolManager.Discover();
			// Print discovered devices
			for (String device : protocolManager.Devices()) {
				System.out.println(device);
			}

		} catch (ServiceUnknown e) {
			System.err.println("Can not find the DBus object " + AGILE_PROTOCOL_MANAGER_BUS_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
