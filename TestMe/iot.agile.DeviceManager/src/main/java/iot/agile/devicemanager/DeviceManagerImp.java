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
package iot.agile.devicemanager;

import java.util.HashMap;
import java.util.Map;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import iot.agile.devicemanager.device.DeviceImp;

/**
 * @author dagi
 * 
 *         Agile Device manager implementation
 *
 */
public class DeviceManagerImp implements DeviceManager {

	/**
	 * Bus name for the device manager
	 */
	private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_NAME = "iot.agile.DeviceManger";
	/**
	 * Bus path for the device manager
	 */
	private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_PATH = "/iot/agile/DeviceManager";
	/**
	 * DBus connection to the device manager
	 */
	private static DBusConnection agileDeviceManagerConnection;

	/**
	 * registered devices
	 */
	private static Map<String, String> devices;

	static {
		if (agileDeviceManagerConnection == null) {
			try {
				agileDeviceManagerConnection = DBusConnection.getConnection(DBusConnection.SESSION);
				agileDeviceManagerConnection.requestBusName(AGILE_DEVICEMANAGER_MANAGER_BUS_NAME);
				agileDeviceManagerConnection.exportObject(AGILE_DEVICEMANAGER_MANAGER_BUS_PATH, new DeviceManagerImp());
			} catch (DBusException e) {
				e.printStackTrace();
			}
		}

		if (devices == null) {
			devices = new HashMap<String, String>();
		}

	}

	public static void main(String[] args) {
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Find()
	 */
	public String Find() {
		// TODO
		return null;
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Create()
	 */
	public String Create(String deviceID, String deviceName, String protocol) {
		// check if it not registered or not connected
		DeviceImp device = new DeviceImp(deviceID, deviceName, protocol);
		if (!isRegistered(device.getDeviceAgileID())) {
			devices.put(deviceID, device.getDeviceAgileID());
		}
		return device.getDeviceAgileID();
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Read(java.lang.
	 *      String)
	 */
	public void Read(String id) {
		// TODO
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Update(java.lang.
	 *      String, java.lang.String)
	 */
	public boolean Update(String id, String definition) {
		// TODO
		return false;
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.devicemanager.DeviceManager#devices()
	 */
	public Map<String, String> devices() {
		return devices;
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Delete(java.lang.
	 *      String, java.lang.String)
	 */
	public void Delete(String id, String definition) {
		// TODO
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Batch(java.lang.
	 *      String, java.lang.String)
	 */
	public boolean Batch(String operation, String arguments) {
		// TODO
		return false;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.freedesktop.dbus.DBusInterface#isRemote()
	 */
	public boolean isRemote() {
		// TODO
		return false;
	}

	// ====================Utility methods
	public void DropBus() {
		agileDeviceManagerConnection.disconnect();
	}

	private boolean isRegistered(String deviceAgileID) {
		if (devices.isEmpty() || devices == null) {
			return false;
		} else if (devices.containsKey(deviceAgileID)) {
			return true;
		}
		return false;
	}

}
