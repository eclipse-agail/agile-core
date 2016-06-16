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
package iot.agile.protocolmanager;

import java.util.ArrayList;
import java.util.List;

import org.freedesktop.DBus.Error.ServiceUnknown;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import iot.agile.protocolmanager.protocol.BLEProtocol;

 
/**
 * @author dagi
 * 
 * AGILE Protocol Manager Implementation 
 *
 */
public class ProtocolManagerImp implements ProtocolManager {
	/**
	 * DBus bus name for the protocol manager
	 */
	private static final String AGILE_PROTOCOL_MANAGER_BUS_NAME = "iot.agile.ProtocolManager";
	/**
	 * DBus bus path for the protocol manager
	 */
	private static final String AGILE_PROTOCOL_MANAGER_BUS_PATH = "/iot/agile/ProtocolManager";
	/**
	 * BLE Protocol imp DBus interface id
	 */
	private static final String BLE_PROTOCOL_ID = "iot.agile.protocol.BLE";
	/**
	 * BLE Protocol imp DBus interface path
	 */
	private static final String BLE_PROTOCOL_PATH = "/iot/agile/protocol/ble";
	/**
	 * DBus connection for the protocol manager
	 */
	private static DBusConnection protocolManagerConnection;
	/**
	 * DBus session bus connection
	 */
	private static DBusConnection connection;
	/**
	 * List of supported protocols
	 */
	private static List<String> protocols;
	/**
	 * List of discovered devices from all the protocols
	 */
	private static List<String> devices;


	static {
		if (connection == null) {
			try {
				connection = DBusConnection.getConnection(DBusConnection.SESSION);
			} catch (DBusException e) {
				e.printStackTrace();
			}
		}

		if (protocolManagerConnection == null) {
			try {
				protocolManagerConnection = DBusConnection.getConnection(DBusConnection.SESSION);
				protocolManagerConnection.requestBusName(AGILE_PROTOCOL_MANAGER_BUS_NAME);
				protocolManagerConnection.exportObject(AGILE_PROTOCOL_MANAGER_BUS_PATH, new ProtocolManagerImp());

			} catch (DBusException e) {
				e.printStackTrace();
			}
		}

		if (devices == null) {
			devices = new ArrayList<String>();
		}

		if (protocols == null) {
			protocols = new ArrayList<String>();
		}
		if (!protocols.isEmpty()) {
			if (!protocols.contains(BLE_PROTOCOL_ID)) {
				protocols.add(BLE_PROTOCOL_ID);
			}
		} else {
			protocols.add(BLE_PROTOCOL_ID);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Devices()
	 */
	public List<String> Devices() {
		return devices;
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Protocols()
	 */
	public List<String> Protocols() {
		return protocols;
	}

	/**
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Discover()
	 */
	public void Discover() {
		
		try {
			BLEProtocol ble = (BLEProtocol) connection.getRemoteObject(BLE_PROTOCOL_ID,BLE_PROTOCOL_PATH ,
					BLEProtocol.class);
			ble.Discover();
			for(String device : ble.Devices()){
				if(!devices.contains(device)){
					devices.add(device);
				}
			}
// 			ble.StopDiscovery();
		}catch (ServiceUnknown e) {
			   System.err.println("Can not find the DBus object "+BLE_PROTOCOL_PATH);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (DBusException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Add(java.lang.String)
	 */
	public void Add(String protocol) {
		// TODO 
	}

	/**
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Remove(java.lang.String)
	 */
	public void Remove(String protocol) {
		// TODO 
	}

	public boolean isRemote() {
		return false;
	}
	
	/**
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#DropBus()
	 */
	public void DropBus() {
		protocolManagerConnection.disconnect();
	}

}
