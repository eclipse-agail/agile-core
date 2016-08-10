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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.Protocol;
import iot.agile.ProtocolManager;
import iot.agile.object.AbstractAgileObject;
import iot.agile.object.DeviceOverview;
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
public class BLEProtocolImp extends AbstractAgileObject implements Protocol {

	protected final Logger logger = LoggerFactory.getLogger(BLEProtocolImp.class);

	/**
	 * Bus name for AGILE BLE Protocol
	 */
	private static final String AGILE_BLUETOOTH_BUS_NAME = "iot.agile.protocol.BLE";

	/**
	 * Bus path for AGILE BLE Protocol
	 */
	private static final String AGILE_BLUETOOTH_BUS_PATH = "/iot/agile/protocol/BLE";

	/**
	 * DBus bus path for the protocol manager
	 */
	private static final String AGILE_PROTOCOL_MANAGER_BUS_PATH = "/iot/agile/ProtocolManager";

	/**
	 * Protocol name
	 */
	private static final String PROTOCOL_NAME = "Bluetooth Low Energy";

	/**
	 * Protocol driver name
	 */
	private static final String DRIVER_NAME = "BLE";

	// Device status
	public static final String CONNECTED = "CONNECTED";

	public static final String DISCONNECTED = "DISCONNECTED";
	
	public static final String AVAILABLE = "AVAILABLE";
	
	public static final String UNAVAILABLE = "AVAILABLE";

	private static final String SENSOR_NAME = "SENSOR_NAME";

	private static final String GATT_SERVICE = "GATT_SERVICE";

	private static final String GATT_CHARACTERSTICS_VALUE = "VALUE_CHAR";

	private static final String GATT_CHARACTERSTICS_CONFIG = "CONFIGURATION_CHAR";

	private static final String SENSOR_TURN_ON = "SENSOR_TURN_ON";
	
	private static final String SENSOR_TURN_OFF = "SENSOR_TURN_OFF";
	/**
	 * Lists of device names TODO: Should return lists of devices in terms of
	 * Dbus object
	 */
	protected List<DeviceOverview> deviceList = new ArrayList<DeviceOverview>();

	/**
	 * The bluetooth manager
	 */
	protected BluetoothManager bleManager;
	
	protected String lastRead;
	
	private BluetoothGattCharacteristic sensorValue;
	private BluetoothGattCharacteristic sensorConfig ;
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	private ScheduledFuture future;

	protected final State state = new State();

	public class State {
		public boolean isDiscovering = false;
	}

	public static void main(String[] args) throws DBusException {
		Protocol bleProtocol = new BLEProtocolImp();
	}

	public BLEProtocolImp() throws DBusException {
		dbusConnect(AGILE_BLUETOOTH_BUS_NAME, AGILE_BLUETOOTH_BUS_PATH, this);
		try {
			bleManager = BluetoothManager.getBluetoothManager();
		} catch (BluetoothException bex) {
			logger.error(" Failed to start BLE Protocol, no bluetooth adapter found on the system", bex);
		} catch (Exception e) {
			logger.error("Error in getting BluetoothManager instance", e);
		}
		logger.debug("Started BLE Protocol");
	}

	/**
	 * Returns lists of devices
	 */
	@Override
	public List<DeviceOverview> Devices() {
		return deviceList;
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.Protocol#protocolStatus()
	 */
	public void ProtocolStatus() {
		logger.debug("Protocol.ProtocolStatus not implemented");
	}

	/**
	 *
	 * @see iot.agile.protocol.ble.Protocol#driver()
	 */
	@Override
	public String Driver() {
		return DRIVER_NAME;
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.Protocol#name()
	 */
	@Override
	public String Name() {
		return PROTOCOL_NAME;
	}

	/**
	 * Connect BLE Device
	 *
	 * @param deviceAddress
	 * @see iot.agile.protocol.ble.Protocol#initialize(java.lang.String)
	 */
	@Override
	public boolean Connect(String deviceAddress) {
		logger.debug("Connecting to BLE device {}", deviceAddress);
		BluetoothDevice bleDevice;
		try {
			bleDevice = getDevice(deviceAddress);
			if (bleDevice.getConnected()) {
				return true;
			}
			if (bleDevice.connect()) {
				return true;
			}
		} catch (InterruptedException e) {
			logger.error("Failed to connect: {}", deviceAddress, e);
		}
		return false;
	}

	/**
	 *
	 * Disconnect bluetooth device
	 *
	 * @return
	 * @see iot.agile.protocol.ble.Protocol#destory(java.lang.String)
	 */
	@Override
	public boolean Disconnect(String deviceAddress) {
		logger.info("Disconnecting from BLE device {}", deviceAddress);
		BluetoothDevice bleDevice;
		try {
			bleDevice = getDevice(deviceAddress);
			if (bleDevice != null) {
				return bleDevice.disconnect();
			}
		} catch (InterruptedException e) {
			logger.error("Failed to disconnect {}", deviceAddress, e);
		}
		return false;
	}

	/**
	 * Discover BLE devices, towards a more descriptive name
	 */
	@Override
	public void StartDiscovery() {
		Discover();
	}

	/**
	 * Discover BLE devices
	 */
	@Override
	public void Discover() {
		logger.info("Started discovery of BLE devices");

		bleManager.startDiscovery();

		/* TODO: would be better reactive: lister to bluez/TinyB signals */
		Runnable task = () -> {

			logger.debug("Checking for new devices");

			int newDevices = 0;
			List<BluetoothDevice> list = bleManager.getDevices();
			for (BluetoothDevice device : list) {
				if (device.getRSSI() != 0) {
					DeviceOverview deviceOverview = new DeviceOverview(device.getAddress(), AGILE_BLUETOOTH_BUS_NAME,
							device.getName(), AVAILABLE);
					if (isNewDevice(deviceOverview)) {
						deviceList.add(deviceOverview);
						try {
							ProtocolManager.FoundNewDeviceSignal foundNewDevSig = new ProtocolManager.FoundNewDeviceSignal(
									AGILE_PROTOCOL_MANAGER_BUS_PATH, deviceOverview);
							connection.sendSignal(foundNewDevSig);
						} catch (DBusException e) {
							e.printStackTrace();
						}
						printDevice(device);
						newDevices++;
					}
				}
			}

			if (newDevices > 0) {
				logger.info("Found {} new device(s)", newDevices);
			}
		};

		future = executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
		// try {
		// future.get(10, TimeUnit.SECONDS);
		// } catch (InterruptedException | ExecutionException | TimeoutException
		// ex) {
		// logger.debug("Aborted execution scheduler: {}", ex.getMessage());
		// } finally {
		// logger.debug("Stopped BLE discovery");
		// }

	}

	/**
	 * @see iot.agile.protocol.ble.Protocol#protocolProfile()
	 */
	public void ProtocolProfile() {
		logger.debug("Protocol.ProtocolProfile not implemented");
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.Protocol#Status()
	 */
	@Override
	public String Status() {
		logger.debug("Protocol.Status not implemented");
		return null;
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.Protocol#execute()
	 */
	public void Execute(String... executeParams) {
		logger.debug("Protocol.Execute not implemented");

	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.Protocol#write()
	 */
	@Override
	public String Write(String deviceAddress, Map<String, String> profile) {
		String sensorName = profile.get(SENSOR_NAME);
		BluetoothDevice device;
		try {
			device = getDevice(deviceAddress);
			if (device == null) {
				logger.error("Device not found: {}", deviceAddress);
				return "Device not found";
			}
			if (!device.getConnected()) {
				logger.error("Device not connected: {}", deviceAddress);
				return "Device not connected";
			} else {
				BluetoothGattService sensorService = getService(device, profile.get(GATT_SERVICE));
				if (sensorService == null) {
					logger.error("The device does not have {} service: {}", sensorName, deviceAddress);
					return sensorName + " service not found";
				} else {
					sensorValue = getCharacteristic(sensorService, profile.get(GATT_CHARACTERSTICS_VALUE));
					  sensorConfig = getCharacteristic(sensorService,
							profile.get(GATT_CHARACTERSTICS_CONFIG));

					if (sensorValue == null || sensorConfig == null) {
						logger.error("Could not find the correct characterstics");
						return "Incorrect characterstics";
					}
					byte[] configValue = profile.get(SENSOR_TURN_ON).getBytes();
					sensorConfig.writeValue(configValue);
					return "Done";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Error on sensor config";
	}

	/**
	 *
	 *
	 * @param profile
	 * @see iot.agile.protocol.ble.Protocol#read()
	 */
	public String Read(String deviceAddress, Map<String, String> profile) throws DBusException {
		BluetoothDevice device;
		try {
			device = getDevice(deviceAddress);
			if (device == null) {
				logger.error("Device not found: {}", deviceAddress);
				return "Device not found";
			}
			if (!device.getConnected()) {
				logger.error("Device not connected: {}", deviceAddress);
				return "Device not connected";
			} else {
				/**
				 * Read the service value from value characteristics
				 */
				byte[] readValue = sensorValue.readValue();
				
				//Extract Turn off sensor command value from the profile
				byte[] turnoffCMD = profile.get(SENSOR_TURN_OFF).getBytes();
				int intermediateValue = turnoffCMD[0]-1;
				byte[] value = {(byte) intermediateValue};
  				sensorConfig.writeValue(value);
				return new String(readValue, StandardCharsets.ISO_8859_1);
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException occured", e);
			throw new DBusException("Operation interrupted abnormally");
		}
	}

 

	public void Receive(String args) throws DBusException {
		logger.debug("Protocol.Receive not implemented");
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.Protocol#subscribe(java.lang.String[])
	 */
	@Override
	public void Subscribe(String... subscribeParams) {
		logger.debug("Protocol.Receive not implemented");
	}

	/**
	 * @see iot.agile.protocol.ble.Protocol#DataStore()
	 */
	@Override
	public String Data() {
		return lastRead;
	}

	public boolean isRemote() {
		return false;
	}

	// =========================UTILITY METHODS==============

	void printDevice(BluetoothDevice device) {
		logger.info("Name = {}", device.getName());
		logger.info("Address = {}", device.getAddress());
		logger.info("Connected= {}", device.getConnected());
	}

	private BluetoothDevice getDevice(String address) throws InterruptedException {
		BluetoothDevice bleDevice = null;
		List<BluetoothDevice> list = bleManager.getDevices();
		for (BluetoothDevice device : list) {
			if (device.getAddress().equals(address)) {
				bleDevice = device;
			}
		}
		return bleDevice;
	}

	@Override
	public void finalize() {
		connection.disconnect();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see iot.agile.protocol.ble.Protocol#StopDiscovery()
	 */
	@Override
	public void StopDiscovery() {
		bleManager.stopDiscovery();
		if (future != null) {
			future.cancel(true);
		}
	}

	private boolean isNewDevice(DeviceOverview device) {
		for (DeviceOverview dev : deviceList) {
			if (dev.getId().equals(device.getId())) {
				return false;
			}
		}
		return true;
	}

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
		if (bluetoothServices == null) {
			return null;
		}

		for (BluetoothGattService gattservice : bluetoothServices) {
			if (gattservice.getUUID().equals(UUID)) {
				service = gattservice;
			}
		}
		return service;
	}

	/**
	 * Returns a GATT characteristics from the given GATT service based on the
	 * given UUID
	 *
	 * @param service
	 *            The GATT Service
	 *
	 * @param UUID
	 *            The required GATT characteristics UUID
	 *
	 * @return
	 */
	private BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service, String UUID) {
		List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
		if (characteristics == null) {
			return null;
		}
		for (BluetoothGattCharacteristic characteristic : characteristics) {
			if (characteristic.getUUID().equals(UUID)) {
				return characteristic;
			}
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
