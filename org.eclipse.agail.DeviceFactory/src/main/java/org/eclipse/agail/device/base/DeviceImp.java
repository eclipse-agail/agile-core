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

package org.eclipse.agail.device.base;

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
import org.eclipse.agail.exception.AgileNoResultException;
import org.eclipse.agail.Device;
import org.eclipse.agail.Protocol;
import org.eclipse.agail.Protocol.NewRecordSignal;
import org.eclipse.agail.object.AbstractAgileObject;
import org.eclipse.agail.object.DeviceComponent;
import org.eclipse.agail.object.DeviceDefinition;
import org.eclipse.agail.object.DeviceOverview;
import org.eclipse.agail.object.RecordObject;
import org.eclipse.agail.object.StatusType;

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
	private static final String AGILE_DEVICE_BASE_ID = "org.eclipse.agail.Device";

	/**
	 * Bus path for AGILE BLE Device interface
	 */
	protected static final String AGILE_DEVICE_BASE_BUS_PATH = "/org/eclipse/agail/Device/";

	/**
	 * DBus bus path for for new subscribe record
	 * 
	 */
	protected static final String AGILE_NEW_RECORD_SUBSCRIBE_SIGNAL_PATH = "/org/eclipse/agail/NewRecord/Subscribe";

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
	/**
	 * Last seen 
	 */
	protected long lastSeen;
	/**
	 * 	
	 */
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
		this.deviceID = deviceOverview.getProtocol().replace("org.eclipse.agail.protocol.", "").toLowerCase() + deviceOverview.id.replace(":", "");
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
 		this.profile = devicedefinition.streams;
 		this.deviceAgileID = AGILE_DEVICE_BASE_ID;
	}

	public DeviceDefinition Definition() {
 		return new DeviceDefinition(deviceID, address, deviceName, "", protocol,
				AGILE_DEVICE_BASE_BUS_PATH + protocol.replace("org.eclipse.agail.protocol.", "").toLowerCase() + address.replace(":", ""), profile);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 *
	 * @see org.eclipse.agail.protocol.ble.device.Device#Id()
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
	  logger.debug("Device Profile is not implemented");
		throw new AgileNoResultException("Device Profile is not implemented");
	}

	/**
	 * Returns the last update of value
	 */
	public RecordObject LastUpdate(String componentID) {
		RecordObject lastUpdate = lastReadStore.get(componentID);
		if(lastUpdate == null){
			throw new AgileNoResultException("No lastUpdate value found");
		}
		return lastUpdate;
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
	 * @see org.eclipse.agail.protocol.ble.device.IDevice#Data()
	 */
	public RecordObject Data() {
	  if(data == null){
      throw new AgileNoResultException("No data available");
    }
	  return data;
		}

  public long LastSeen() {
    if(data == null){
      throw new AgileNoResultException("Device not seen yet");
    }
    return data.lastUpdate;
  }
	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.device.IDevice#Protocol()
	 */
	public String Protocol() {
		return protocol;
	}

	public abstract void Connect() throws DBusException;

	public abstract void Disconnect() throws DBusException;

	/**
	 *
	 *
     * @param commandId
	 * @see org.eclipse.agail.protocol.ble.device.IDevice#Execute(java.lang.String)
	 */
	public abstract void Execute(String commandId);

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
				try {
					recObjs.add(Read(component.id));
				} catch (Exception e) {
					e.printStackTrace();
				}
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
 			throw new AgileNoResultException("Unable to read value "+componentName );
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
     * @param componentName
     * @param payload
	 * @see org.eclipse.agail.protocol.ble.device.IDevice#Write()
	 */
	public abstract void Write(String componentName, String payload);

	/**
	 * @return the deviceAgileID
	 */
	public String getDeviceAgileID() {
		return deviceAgileID;
	}
        
        public abstract List<String> Commands();

	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.device.IDevice#Subscribe()
	 */
	@Override
	public void Subscribe(String component) {
	}

	@Override
	public void Unsubscribe(String component) throws DBusException {
	}

	@Override
	public void Stop() throws DBusException {
		try {
			if (isConnected()) {
				for(String component : subscribedComponents.keySet()){
					if(subscribedComponents.get(component) >0){
						Unsubscribe(component);
					}
				}
				Disconnect();
			}
		} catch (Exception e) {
			logger.error("Failed to stop device ", e);
		}
		dbusDisconnect();
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
	protected boolean hasOtherActiveSubscription(String componentName) {
		return (subscribedComponents.get(componentName) > 0);
	}

	/**
	 * Checks if there is another active subscription on any component of
	 * the device
	 * 
	 * @return
	 */
	protected boolean hasOtherActiveSubscription() {
		for (String componentName : subscribedComponents.keySet()) {
			if (subscribedComponents.get(componentName) > 0) {
				return true;
			}
		}
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
