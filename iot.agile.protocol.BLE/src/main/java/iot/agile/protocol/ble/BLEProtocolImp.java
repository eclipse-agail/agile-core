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

package iot.agile.protocol.ble;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import tinyb.BluetoothDevice;
import tinyb.BluetoothException;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import tinyb.BluetoothManager;

/**
 * Agile Bluetooth Low Energy(BLE) Protocol implementation
 * 
 * @author dagi
 *
 */
public class BLEProtocolImp implements BLEProtocol {

	/**
	 * Bus name for AGILE BLE Protocol
	 */
	private static final String AGILE_BLUETOOTH_BUS_NAME = "iot.agile.protocol.BLE";

	/**
	 * Bus path for AGILE BLE Protocol
	 */
	private static final String AGILE_BLUETOOTH_BUS_PATH = "/iot/agile/protocol/ble";

	/**
	 * Protocol name
	 */
	private static final String PROTOCOL_NAME = "Bluetooth Low Energy";

	/**
	 * Protocol driver name
	 */
	private static final String DRIVER_NAME = "BLE";

	/**
	 * DBus connection for agile bluetooth
	 */
	private static DBusConnection agileBluetoothConn;

	/**
	 * The bluetooth manager
	 */
	public static BluetoothManager bleManager;

	/**
	 * Lists of device names TODO: Should return lists of devices in terms of
	 * Dbus object
	 */
	private static List<String> deviceList;

	private static String lastRead;
	/**
	 * GATT Profile for TI SensorTag Temperature service
	 * 
	 */
	private static final String TEMP_GATT_SERVICE = "TemperatureService";

	private static final String TEMP_VALUE_GATT_CHARACTERSTICS = "TemperatureValueCharacterstics";

	private static final String TEMP_CONFIGURATION_GATT_CHARACTERSTICS = "TemperatureConfigurationCharacterstics";

	private static final String SENSOR_NAME = "SensorName";

	private static final String TEMPERATURE = "Temperature";

	/**
	 * Setup the DBus interface for the protocol and TinyB protocol manager if
	 * it is not initialized before
	 */
	static {
		try {
			if (agileBluetoothConn == null) {
				agileBluetoothConn = DBusConnection.getConnection(DBusConnection.SESSION);
				agileBluetoothConn.requestBusName(AGILE_BLUETOOTH_BUS_NAME);
				agileBluetoothConn.exportObject(AGILE_BLUETOOTH_BUS_PATH, new BLEProtocolImp());
			}
		} catch (DBusException e) {
			e.printStackTrace();
		}

		if (bleManager == null) {
			try {
				bleManager = BluetoothManager.getBluetoothManager();
			} catch (BluetoothException bex) {
				System.err.println("AgileBLE: No bluetooth adapter found on the system");
				bex.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
	}

	/**
	 * Returns lists of devices
	 */
	public List<String> Devices() {
		return deviceList;
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#protocolStatus()
	 */
	public void ProtocolStatus() {
		// TODO Implement
	}

	/**
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#driver()
	 */
	public String Driver() {
		return DRIVER_NAME;
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#name()
	 */
	public String Name() {
		return PROTOCOL_NAME;
	}

	/**
	 * Connect BLE Device
	 * 
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#initialize(java.lang.String)
	 */
	public boolean Connect(String deviceAddress) {
		BluetoothDevice bleDevice;
		try {
			bleDevice = getDevice(deviceAddress);
			if (bleDevice.connect()) {
				return true;
			}
		} catch (InterruptedException e) {
			System.err.println("Failed to connect: " + deviceAddress);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * Disconnect bluetooth device
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#destory(java.lang.String)
	 */
	public boolean Disconnect(String deviceAddress) {
		BluetoothDevice bleDevice;
		try {
			bleDevice = getDevice(deviceAddress);
			if (bleDevice != null) {
				return bleDevice.disconnect();
			}
		} catch (InterruptedException e) {
			System.err.println("Failed to disconnect: " + deviceAddress);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Discover BLE devices
	 */
	public void Discover() {
		if (deviceList == null) {
			deviceList = new ArrayList<String>();
		}
		if (bleManager.startDiscovery()) {
			List<BluetoothDevice> list = bleManager.getDevices();
			for (BluetoothDevice device : list) {
				if (!deviceList.contains(device.getName())) {
					deviceList.add(device.getName());
					printDevice(device);
				}
			}
		}
	}

	/**
	 * @see iot.agile.protocol.ble.BLEProtocol#protocolProfile()
	 */
	public void ProtocolProfile() {
		// TODO
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#Status()
	 */
	public String Status() {
		return null;
		// TODO
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#execute()
	 */
	public void Execute(String... executeParams) {
		// TODO

	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#write()
	 */
	public void Write() {
		// TODO
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#read()
	 */
	public String Read(String deviceAddress, Map<String, String> profile) {
		BluetoothDevice device;
		try {
			device = getDevice(deviceAddress);
			if (device == null)
				return "Device not found";
			if (device.getConnected() == false) {
				return "Device not connected";
			} else {
				if (profile.get(SENSOR_NAME).equals(TEMPERATURE)) {

					BluetoothGattService tempService = getService(device, profile.get(TEMP_GATT_SERVICE));
					if (tempService == null) {
						return "This device does not have the temperature service";
					} else {
						BluetoothGattCharacteristic tempValue = getCharacteristic(tempService,
								profile.get(TEMP_VALUE_GATT_CHARACTERSTICS));
						BluetoothGattCharacteristic tempConfig = getCharacteristic(tempService,
								profile.get(TEMP_CONFIGURATION_GATT_CHARACTERSTICS));

						if (tempValue == null || tempConfig == null) {
							return "Could not find the correct characterstics";
						}

						/**
						 * Turn on the temperature service by writing One in the
						 * configuration characteristics
						 */
						byte[] config = { 0x01 };
						tempConfig.writeValue(config);
						Thread.sleep(1000);
						tempConfig.writeValue(config);

						/**
						 * Read the temperature value from value characteristics
						 * and convert it to human readable format
						 */
						byte[] tempRaw = tempValue.readValue();

						/*
						 * The temperature service returns the data in an
						 * encoded format which can be found in the wiki.
						 * Convert the raw temperature format to celsius and
						 * print it. Conversion for object temperature depends
						 * on ambient according to wiki, but assume result is
						 * good enough for our purposes without conversion.
						 */
						int objectTempRaw = (tempRaw[0] & 0xff) | (tempRaw[1] << 8);
						int ambientTempRaw = (tempRaw[2] & 0xff) | (tempRaw[3] << 8);

						float objectTempCelsius = convertCelsius(objectTempRaw);
						float ambientTempCelsius = convertCelsius(ambientTempRaw);
						lastRead = String.format(" Temp: Object = %fC, Ambient = %fC", objectTempCelsius,
								ambientTempCelsius);
						return lastRead;
					}

				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void Receive(String args) {
		// TODO
	}

	/**
	 * 
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#subscribe(java.lang.String[])
	 */
	public void Subscribe(String... subscribeParams) {
		// TODO
	}

	/**
	 * @see iot.agile.protocol.ble.BLEProtocol#DataStore()
	 */
	public String Data() {
		return lastRead;
	}

	public boolean isRemote() {
		return false;
	}

	// ==========Testing and Utility=============================
	// ================ Methods==================================
	/**
	 * Disconnect the bus, and drop the dbus interface
	 */
	public void DropBus() {
		agileBluetoothConn.disconnect();
	}

	void printDevice(BluetoothDevice device) {
		System.out.print("Address = " + device.getAddress());
		System.out.print(" Name = " + device.getName());
		System.out.print(" Connected = " + device.getConnected());
		System.out.println();
	}

	private BluetoothDevice getDevice(String address) throws InterruptedException {
		BluetoothDevice bleDevice = null;
		List<BluetoothDevice> list = bleManager.getDevices();
		for (BluetoothDevice device : list) {
			if (device.getAddress().equals(address))
				bleDevice = device;
		}
		return bleDevice;
	}

	public void finalize() {
		agileBluetoothConn.disconnect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see iot.agile.protocol.ble.BLEProtocol#StopDiscovery()
	 */
	public void StopDiscovery() {
		bleManager.stopDiscovery();
	}

	// =====================================UTILITY METHODS==============
	/**
	 * Returns a Bluetooth GATT service from the given device based on the UUID
	 * of the service
	 * 
	 * @param device
	 *            The device id
	 * @param UUID
	 *            service UUID
	 * @return
	 * @throws InterruptedException
	 */

	private BluetoothGattService getService(BluetoothDevice device, String UUID) {
		BluetoothGattService service = null;
		List<BluetoothGattService> bluetoothServices = null;
		bluetoothServices = device.getServices();
		if (bluetoothServices == null)
			return null;

		for (BluetoothGattService gattservice : bluetoothServices) {
			if (gattservice.getUuid().equals(UUID))
				service = gattservice;
		}
		return service;
	}

	/*
	 * Returns a GATT characteristics from the given GATT service based on the
	 * given UUID
	 * 
	 * @param service The GATT Service
	 * 
	 * @param UUID The required GATT characteristics UUID
	 * 
	 * @return
	 */
	private BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String UUID) {
		List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
		if (characteristics == null)
			return null;

		for (BluetoothGattCharacteristic characteristic : characteristics) {
			if (characteristic.getUuid().equals(UUID))
				return characteristic;
		}
		return null;
	}

	/**
	 * Converts temperature into degree Celsius
	 * 
	 * @param raw
	 * @return
	 */
	private float convertCelsius(int raw) {
		return raw / 128f;
	}

}
