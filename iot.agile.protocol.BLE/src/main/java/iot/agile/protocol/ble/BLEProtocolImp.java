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
import iot.agile.object.RecordObject;
import tinyb.BluetoothDevice;
import tinyb.BluetoothException;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import tinyb.BluetoothManager;
import tinyb.BluetoothNotification;
import tinyb.BluetoothType;

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
	 * DBus bus path for found new device signal
	 */
	private static final String AGILE_NEW_DEVICE_SIGNAL_PATH = "/iot/agile/NewDevice";

	/**
	 * DBus bus path for for new record/data reading
	 */
	private static final String AGILE_NEW_RECORD_SIGNAL_PATH = "/iot/agile/NewRecord";

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

	private static final String UNIT = "UNIT";

	private static final String GATT_SERVICE = "GATT_SERVICE";
	
	private static final String GATT_CHARACTERSTICS = "GATT_CHARACTERSTICS";

	private static final String PAYLOAD = "PAYLOAD";

	/**
	 * Device list
	 */
	protected List<DeviceOverview> deviceList = new ArrayList<DeviceOverview>();

	/**
	 * The bluetooth manager
	 */
	protected BluetoothManager bleManager;

	protected RecordObject lastRecord;

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
	 *
	 *
	 * @see iot.agile.protocol.ble.Protocol#name()
	 */
	@Override
	public String Name() {
		return PROTOCOL_NAME;
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
	 * @see iot.agile.protocol.ble.Protocol#Status()
	 */
	@Override
	public String Status() {
		logger.debug("Protocol.Status not implemented");
		return null;
	}

	/**
	 * Returns lists of devices
	 */
	@Override
	public List<DeviceOverview> Devices() {
		return deviceList;
	}

	/**
	 * @see iot.agile.protocol.ble.Protocol#DataStore()
	 */
	@Override
	public RecordObject Data() {
		return lastRecord;
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
			bleDevice = (BluetoothDevice) bleManager.find(BluetoothType.DEVICE, null, deviceAddress, null);
			if (bleDevice.getConnected()) {
				return true;
			}
			if (bleDevice.connect()) {
				return true;
			}
		} catch (Exception e) {
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
			bleDevice = (BluetoothDevice) bleManager.find(BluetoothType.DEVICE, null, deviceAddress, null);
			if (bleDevice != null) {
				return bleDevice.disconnect();
			}
		} catch (Exception e) {
			logger.error("Failed to disconnect {}", deviceAddress, e);
		}
		return false;
	}

	/**
	 * Discover BLE devices, towards a more descriptive name
	 */
	@Override
	public void StartDiscovery() {
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
									AGILE_NEW_DEVICE_SIGNAL_PATH, deviceOverview);
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

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.Protocol#write()
	 */
	@Override
	// TODO return boolean
	public String Write(String deviceAddress, Map<String, String> profile) {
		BluetoothDevice device;
		try {
			device = (BluetoothDevice) bleManager.find(BluetoothType.DEVICE, null, deviceAddress, null);
			if (device == null) {
				logger.error("Device not found: {}", deviceAddress);
				return "Device not found";
			} else {
				if (!device.getConnected()) {
					logger.error("Device not connected: {}", deviceAddress);
					return "Device not connected";
				} else { 
					BluetoothGattService gattService = device.find(profile.get(GATT_SERVICE));
					if (gattService == null) {
						logger.error("The device does not have {} service: {}", profile.get(GATT_SERVICE),
								deviceAddress);
						return profile.get(SENSOR_NAME) + " service not found";
					} else {
						BluetoothGattCharacteristic gattChar = gattService.find(profile.get(GATT_CHARACTERSTICS));
						if (gattChar == null) {
							logger.error("Could not find the correct characterstics");
							return "Incorrect characterstics";
						} else {
							byte[] value = profile.get(PAYLOAD).getBytes();
							gattChar.writeValue(value);
							return "Done";
						}
					}
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
	// TODO return ByteArray
	public RecordObject Read(String deviceAddress, Map<String, String> profile) throws DBusException {
		BluetoothDevice device;
		try {
			device = (BluetoothDevice) bleManager.find(BluetoothType.DEVICE, null, deviceAddress, null);
			if (device == null) {
				logger.error("Device not found: {}", deviceAddress);
				return null;
			} else {
				if (!device.getConnected()) {
					logger.error("Device not connected: {}", deviceAddress);
					return null;
				} else {
					BluetoothGattService gattService = device.find(profile.get(GATT_SERVICE));

					if (gattService == null) {
						logger.error("The device does not have {} service: {}", profile.get(SENSOR_NAME),
								deviceAddress);
						return null;
					} else {
						BluetoothGattCharacteristic gattChar = gattService.find(profile.get(GATT_CHARACTERSTICS));
						if(gattChar == null){
							logger.error("Could not find the correct characterstics");
							return null;					
						}else{
							/**
							 * Read the service value from value characteristics
							 */
							byte[] readValue = gattChar.readValue();
							if(readValue == null){
								logger.error("Error on reading value");
								return null;
							}else {
								lastRecord = new RecordObject(deviceAddress, profile.get(SENSOR_NAME),
									new String(readValue, StandardCharsets.ISO_8859_1), profile.get(UNIT), "String",
									System.currentTimeMillis());
 							}
						}
					}
					return lastRecord;
				}
			}

		} catch (Exception e) {
			logger.error("InterruptedException occured", e);
			throw new DBusException("Operation interrupted abnormally");
		}
	}
 
 
	/**
	 * 
	 */
	@Override
	public void Subscribe(String deviceAddress, Map<String, String> profile) {
//		BluetoothDevice device;
//		try {
//			device = getDevice(deviceAddress);
//			if (device == null) {
//				logger.error("Device not found: {}", deviceAddress);
//			}
//			if (!device.getConnected()) {
//				logger.error("Device not connected: {}", deviceAddress);
//			} else {
//				BluetoothGattService sensorService = getService(device, profile.get(GATT_SERVICE));
//				if (sensorService == null) {
//					logger.error("The device does not have {} service: {}", profile.get(SENSOR_NAME), deviceAddress);
//				} else {
//					BluetoothGattCharacteristic sensorValue = getCharacteristic(sensorService,
//							profile.get(GATT_CHARACTERSTICS_VALUE));
//					BluetoothGattCharacteristic sensorConfig = getCharacteristic(sensorService,
//							profile.get(GATT_CHARACTERSTICS_CONFIG));
//					BluetoothGattCharacteristic sensorPeriod = getCharacteristic(sensorService,
//							profile.get(GATT_CHARACTERSTICS_FREQ));
//					if (sensorValue == null || sensorConfig == null || sensorPeriod == null) {
//						logger.error("Could not find the correct characterstics");
//					} else {
//						byte[] config = profile.get(SENSOR_TURN_ON).getBytes();
//						byte[] period = profile.get(SENSOR_FREQUENCY).getBytes();
//						sensorConfig.writeValue(config);
//						sensorPeriod.writeValue(period);
//						sensorValue.enableValueNotifications(new NewRecordNotification());
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void Unsubscribe(String deviceAddress, Map<String, String> profile) {
//		
//		BluetoothDevice device;
//		try {
//			device = getDevice(deviceAddress);
//			if (device == null) {
//				logger.error("Device not found: {}", deviceAddress);
//			}
//			if (!device.getConnected()) {
//				logger.error("Device not connected: {}", deviceAddress);
//			} else {
//
//				BluetoothGattService sensorService = getService(device, profile.get(GATT_SERVICE));
//				if (sensorService == null) {
//					logger.error("The device does not have {} service: {}", profile.get(SENSOR_NAME), deviceAddress);
//				} else {
//
//					BluetoothGattCharacteristic sensorValue = getCharacteristic(sensorService,
//							profile.get(GATT_CHARACTERSTICS_VALUE));
//					if (sensorValue == null) {
//						logger.error("Could not find the correct characterstics");
//					} else {
//						sensorValue.disableValueNotifications();
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * 
	 * @author dagi
	 * 
	 *         New record signal for Subscription
	 */
//	private class NewRecordNotification implements BluetoothNotification<byte[]> {
//		@Override
//		public void run(byte[] record) {
//			lastRecord = new RecordObject("", "", new String(record, StandardCharsets.ISO_8859_1), "", "String",
//					System.currentTimeMillis());
//			try {
//				Protocol.NewRecordSignal newRecordSignal = new Protocol.NewRecordSignal(AGILE_NEW_RECORD_SIGNAL_PATH,
//						lastRecord);
//				connection.sendSignal(newRecordSignal);
//			} catch (DBusException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	public boolean isRemote() {
		return false;
	}

	// =========================UTILITY ==============

	void printDevice(BluetoothDevice device) {
		logger.info("Name = {}", device.getName());
		logger.info("Address = {}", device.getAddress());
		logger.info("Connected= {}", device.getConnected());
	}
	 

	@Override
	public void finalize() {
		connection.disconnect();
	}

	/**
	 * Check if the device is newly discovered device
	 * 
	 * @param device
	 * @return
	 */
	private boolean isNewDevice(DeviceOverview device) {
		for (DeviceOverview dev : deviceList) {
			if (dev.getId().equals(device.getId())) {
				return false;
			}
		}
		return true;
	}
 
}
