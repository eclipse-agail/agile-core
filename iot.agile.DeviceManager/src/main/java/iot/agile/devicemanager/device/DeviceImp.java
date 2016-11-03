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
package iot.agile.devicemanager.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.Device;
import iot.agile.Protocol;
import iot.agile.Protocol.NewRecordSignal;
import iot.agile.object.AbstractAgileObject;
import iot.agile.object.DeviceComponent;
import iot.agile.object.DeviceDefinition;
import iot.agile.object.DeviceOverview;
import iot.agile.object.RecordObject;
import iot.agile.object.StatusType;

/**
 * @author dagi
 *
 *         Agile Device implementation
 *
 */
public abstract class DeviceImp extends AbstractAgileObject implements Device {

	protected final Logger logger = LoggerFactory.getLogger(DeviceImp.class);

	/**
	 * Bus name for AGILE BLE Device interface
	 */
	private static final String AGILE_DEVICE_BASE_ID = "iot.agile.Device";

	/**
	 * Bus path for AGILE BLE Device interface
	 */
	protected static final String AGILE_DEVICE_BASE_BUS_PATH = "/iot/agile/Device/";

	/**
	 * DBus bus path for for new subscribe record
	 * 
	 */
	protected static final String AGILE_NEW_RECORD_SUBSCRIBE_SIGNAL_PATH = "/iot/agile/NewRecord/Subscribe";

	/**
	 * Agile specific device ID
	 */
	protected String deviceAgileID;
	/**
	 * Device name
	 */
	protected String deviceName;
	/**
	 * Device ID
	 *
	 */
	protected String deviceID;
	/**
	 * The protocol the device supports
	 */
	protected String protocol;

	/**
	 * Protocol specific address
	 */
	protected String address;

	protected List<DeviceComponent> profile = new ArrayList<DeviceComponent>();

	/**
	 * The device protocol interface
	 */
	protected Protocol deviceProtocol;

	/**
	 * Data
	 */
	protected RecordObject data;

	/**
	 * Map to store the last reads of each components of the device
	 */
	protected Map<String, RecordObject> lastReadStore = new HashMap<String, RecordObject>();
	/**
	 * Tracks the number of active subscriptions for each components of the
	 * device
	 */
	protected Map<String, Integer> subscribedComponents = new HashMap<String, Integer>();

	private Map<String, CountDownLatch> ongoingReads = new HashMap<String, CountDownLatch>();

	@SuppressWarnings("rawtypes")
	protected DBusSigHandler newRecordSigHanlder;

	protected boolean hasNewRecordSignalHandler = false;

	public DeviceImp(DeviceOverview deviceOverview) throws DBusException {
		this.deviceName = deviceOverview.name;
		this.deviceID = "ble" + deviceOverview.id.replace(":", "");
		this.address = deviceOverview.id;
		this.deviceAgileID = AGILE_DEVICE_BASE_ID;
	}

	/**
	 * 
	 * @param deviceID
	 *            the device address (MAC in BLE case)
	 * @param deviceName
	 *            discovered named of the device
	 * @param protocol
	 *            the protocol the device supports
	 * 
	 * @throws DBusException
	 */
	public DeviceImp(DeviceDefinition devicedefinition) throws DBusException {
		this.deviceName = devicedefinition.name;
		this.deviceID = devicedefinition.deviceId;
		this.address = devicedefinition.address;
		// this.protocol = BLUETOOTH_LOW_ENERGY;
		this.profile = devicedefinition.streams;
		// this.protocol =devicedefinition.protocol;
		this.deviceAgileID = AGILE_DEVICE_BASE_ID;
	}

	public DeviceDefinition Definition() {
		return new DeviceDefinition(deviceID, address, deviceName, "", protocol,
				AGILE_DEVICE_BASE_BUS_PATH + "ble" + address.replace(":", ""), profile);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 *
	 * @see iot.agile.protocol.ble.device.Device#Id()
	 */
	public String Id() {
		return deviceID;
	}

	/**
	 * returns the name of the device
	 */
	public String Name() {
		return deviceName;
	}

	/**
	 * returns the status of the device
	 */
	public StatusType Status() {
		return deviceProtocol.DeviceStatus(address);
	}

	/**
	 * Returns the configuration of the devices
	 */
	public String Configuration() {
		logger.debug("Device. Configuration not implemented");
		return "";
	}

	/**
	 * Returns the profile of the device
	 */
	public List<DeviceComponent> Profile() {
		return null;
	}

	/**
	 * Returns the last update of value
	 */
	public RecordObject LastUpdate(String componentID) {
		return lastReadStore.get(componentID);
	}

	/**
	 * Returns the last update of value
	 */
	public List<RecordObject> LastUpdateAll() {
		return new ArrayList<RecordObject>(lastReadStore.values());
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.device.IDevice#Data()
	 */
	public RecordObject Data() {
		return data;
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.device.IDevice#Protocol()
	 */
	public String Protocol() {
		return protocol;
	}

	public abstract void Connect() throws DBusException;

	public abstract void Disconnect() throws DBusException;

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.device.IDevice#Execute(java.lang.String)
	 */
	public void Execute(String command, Map<String, Variant> args) {
		logger.debug("Device. Execute not implemented");
	}

	/**
	 *
	 * Reads data from all sensors
	 *
	 *
	 */
	@Override
	public List<RecordObject> ReadAll() {
		List<RecordObject> recObjs = new ArrayList<RecordObject>();
		final CountDownLatch latch = new CountDownLatch(profile.size());
		for (DeviceComponent component : profile) {
			if(isSensorSupported(component.id)){
			new Thread(()->{
					recObjs.add(Read(component.id));
					latch.countDown();
				}).start();				
			}
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
 			e.printStackTrace();
		}
		return recObjs;
	}

	/**
	 *
	 * Reads data from the given sensor
	 *
	 *
	 */
	@Override
	public RecordObject Read(String componentName) {
		RecordObject lastRead = lastReadStore.get(componentName);
		if (isRecentRead(lastRead)) {
			logger.info("Cached read....{}", lastRead);
			return lastRead;
		} else {
			if (isReadOngoing(componentName)) {
				try {
					ongoingReads.get(componentName).await();
					logger.info("Read from ongoing read {}", lastReadStore.get(componentName));
					return lastReadStore.get(componentName);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				synchronized (componentName) {
					ongoingReads.put(componentName, new CountDownLatch(1));
					try {
						data = new RecordObject(deviceID, componentName, DeviceRead(componentName),
								getMeasurementUnit(componentName), "", System.currentTimeMillis());
						lastReadStore.put(componentName, data);
						ongoingReads.get(componentName).countDown();
						synchronized (ongoingReads) {
							ongoingReads.remove(componentName);
						}
						logger.info("New read....{}", data);
						return data;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Check if the last component read was made recently
	 * 
	 * @param record
	 * @return
	 */
	private boolean isRecentRead(RecordObject record) {
		if (record != null) {
			long currentTime = System.currentTimeMillis();
			long recordTime = record.getLastUpdate();
			if (((currentTime - recordTime) / 1000) < 2) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if there is an ongoing read on this component
	 * 
	 * @param component
	 * @return
	 */
	private boolean isReadOngoing(String component) {
		synchronized (ongoingReads) {
			return ongoingReads.containsKey(component);
		}
	}

	/**
	 * Read Method to be implemented by sub-class
	 * 
	 * @param componentName
	 * @return
	 */
	protected abstract String DeviceRead(String componentName);

	/**
	 * Get measurement unit method to be implemented by child class
	 * 
	 * @param sensor
	 * @return
	 */
	protected String getMeasurementUnit(String sensor) {
		for (DeviceComponent component : profile) {
			if (component.id.equals(sensor)) {
				return component.unit;
			}
		}
		return null;
	}

	/**
	 * Writes data into the given sensor
	 *
	 * @see iot.agile.protocol.ble.device.IDevice#Write()
	 */
	public void Write() {
		logger.debug("Device. Write not implemented");
	}

	/**
	 * @return the deviceAgileID
	 */
	public String getDeviceAgileID() {
		return deviceAgileID;
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.device.IDevice#Subscribe()
	 */
	@Override
	public void Subscribe(String component) {
	}

	@Override
	public void Unsubscribe(String component) throws DBusException {
	}

	/**
	 * Utility methods
	 */

	/**
	 *
	 *
	 * @see org.freedesktop.dbus.DBusInterface#isRemote()
	 */
	public boolean isRemote() {
		return false;
	}

	/**
	 * Adds signal handler for a new record value
	 */
	protected void addNewRecordSignalHandler() {
		try {
			if (newRecordSigHanlder == null && connection != null) {
				newRecordSigHanlder = new DBusSigHandler<Protocol.NewRecordSignal>() {
					@Override
					public void handle(NewRecordSignal sig) {
						if (address.equals(sig.address)) {
							String componentName = getComponentName(sig.profile);
							RecordObject recObj = new RecordObject(deviceID, componentName,
									formatReading(componentName, sig.record), getMeasurementUnit(componentName), "",
									System.currentTimeMillis());
							data = recObj;
							logger.info("Device notification component {} value {}", componentName, recObj.value);
							lastReadStore.put(componentName, recObj);
							try {
								Device.NewSubscribeValueSignal newRecordSignal = new Device.NewSubscribeValueSignal(
										AGILE_NEW_RECORD_SUBSCRIBE_SIGNAL_PATH, recObj);
								connection.sendSignal(newRecordSignal);
							} catch (DBusException e) {
								e.printStackTrace();
							}
						}
					}
				};
				connection.addSigHandler(Protocol.NewRecordSignal.class, newRecordSigHanlder);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	protected void removeNewRecordSignalHandler() {
		if (connection != null && newRecordSigHanlder != null) {
			try {
				connection.removeSigHandler(Protocol.NewRecordSignal.class, newRecordSigHanlder);
				newRecordSigHanlder = null;
			} catch (DBusException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Checks if the requested sensor is supported by the device
	 * 
	 * @param sensorName
	 *            Sensor name
	 * @return true if the sensor is supported false otherwise
	 */
	protected boolean isSensorSupported(String sensorName) {
		return true;
	}

	/**
	 * Check if this device is connected at protocol level
	 * 
	 * This method is intended to be override by each protocol device
	 * implementation
	 * 
	 * @return True if the device is connected False otherwise
	 */
	protected boolean isConnected() {
		return false;
	}

	/**
	 * Override by child classes
	 * 
	 * @param sensorName
	 * @param readData
	 * @return
	 */
	protected String formatReading(String sensorName, byte[] readData) {
		return "";
	}

	/**
	 * Checks if there is another active subscription on the given component of
	 * the device
	 * 
	 * @param componentName
	 * @return
	 */
	protected boolean hasotherActiveSubscription(String componentName) {
		return false;
	}

	/**
	 * Given the profile of the component returns the name of the sensor
	 * 
	 * @param uuid
	 * @return
	 */
	protected String getComponentName(Map<String, String> profile) {
		return null;
	}

}
