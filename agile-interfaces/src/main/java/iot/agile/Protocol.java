/*
 * Copyright 2016 CREATE-NET
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
package iot.agile;

import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;

import iot.agile.object.DeviceOverview;

/**
 * Agile BLE Protocol interface
 *
 * @author dagi
 *
 */
public interface Protocol extends DBusInterface {

	public static String AGILE_INTERFACE = "iot.agile.protocol";

	/**
	 * TODO: Return device status
	 */
	@org.freedesktop.DBus.Description("enquiry the device status")
	public String Status();

	/**
	 *
	 * @return Driver unique code name example:
	 */
	@org.freedesktop.DBus.Description("Returns the driver name")
	public String Driver();

	/**
	 *
	 * @return Driver name example bluetooth low energy
	 */
	@org.freedesktop.DBus.Description("Returns the protocol name")
	public String Name();

	/**
	 *
	 * @return the last record received by the read or subscribe operation
	 */
	@org.freedesktop.DBus.Description("Store the last record recived by read or subscribe")
	public byte[] Data();

	/**
	 * List of all devices discovered by the BLE protocol
	 *
	 * This list is updated by discover method
	 *
	 * TODO: Return list of devices TODO: The implementing class for this
	 * interface should hold the list and return
	 *
	 * @see BLEDevice for sample device implementation
	 */
	@org.freedesktop.DBus.Description("Returns list discovered protocol devices")
	public List<DeviceOverview> Devices();

	/**
	 * Setup connection and initialize BLE connection for the given device
	 *
	 * TODO: Instead of deviceAddress this method should receive device profile,
	 * and retrieve the id and other properties from it
	 *
	 * @return true if successfully connected, or if it was already connected
	 */
	@org.freedesktop.DBus.Description("Setup connection and initialize protocol connection for the given device")
	public void Connect(String deviceAddress) throws DBusException;

	/**
	 *
	 * Disconnect the BLE device
	 *
	 * TODO: Use device profile to disconnect the device
	 *
	 * @param deviceAddress
	 */
	@org.freedesktop.DBus.Description("Safely disconnect the device from the protocol adapter")
	public void Disconnect(String deviceAddress) throws DBusException;

	/**
	 * Get discovery status
	 */
	@org.freedesktop.DBus.Description("Get discovery status")
	public String DiscoveryStatus() throws DBusException;

	/**
	 * List all discovered BLE devices
	 *
	 * TODO - return list of devices
	 */
	@org.freedesktop.DBus.Description("Start device discovery")
	public void StartDiscovery() throws DBusException;

	/**
	 * Stop device discovery
	 */
	@org.freedesktop.DBus.Description("Stop device discovery")
	public void StopDiscovery();

	/**
	 * Send data over the protocol to the device
	 *
	 * TODO: Detail of this method should be discussed
	 */
	public void Write(String deviceAddress, Map<String, String> profile) throws DBusException;

	/**
	 * Read data over the Protocol, may be cached in the Data property depending
	 * on implementation to save resources
	 */
	public byte[] Read(String deviceAddress, Map<String, String> profile) throws DBusException;

	/**
	 * Subscribe to data update over the protocol
	 *
	 * @param subscribeParams
	 * @throws DBusException 
	 */
	public void Subscribe(String deviceAddress, Map<String, String> profile) throws DBusException;

	/**
	 * unsubscribe to data update over the protocol
	 *
	 * @param subscribeParams
	 */
	public void Unsubscribe(String deviceAddress, Map<String, String> profile)throws DBusException;

	/**
	 * New data reading signal for subscribe methods
	 * 
	 * @author dagi
	 *
	 */
	public class NewRecordSignal extends DBusSignal {
 		public final byte[] record;

		public NewRecordSignal(String path, byte[] record) throws DBusException {
			super(path, record);
 			this.record = record;
		}

	}
}
