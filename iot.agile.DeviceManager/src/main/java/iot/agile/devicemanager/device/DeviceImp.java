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

import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.Device;
import iot.agile.Protocol;
import iot.agile.object.AbstractAgileObject;
import iot.agile.object.DeviceComponent;
import iot.agile.object.DeviceDefinition;
import iot.agile.object.RecordObject;

/**
 * @author dagi
 *
 *         Agile Device implementation
 *
 */
public class DeviceImp extends AbstractAgileObject implements Device {

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
	 * BLE Protocol imp DBus interface id
	 */
	private static final String BLE_PROTOCOL_ID = "iot.agile.protocol.BLE";
	/**
	 * BLE Protocol imp DBus interface path
	 */
	private static final String BLE_PROTOCOL_PATH = "/iot/agile/protocol/BLE";

	/**
	 * Device status
	 */
	protected static final String CONNECTED = "Connected";

	protected static final String DISCONNECTED = "Disconnected";
	/**
	 * Protocol
	 */
	protected static final String BLUETOOTH_LOW_ENERGY = "iot.agile.protocol.BLE";
	/**
	 * Device status TODO: Needs implementation Default : Disconnected
	 */
	protected static String deviceStatus = DISCONNECTED;

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

	protected List<DeviceComponent> profile;
	/**
	 * The device protocol interface
	 */
	protected Protocol deviceProtocol;

	/**
	 * Data
	 */
	protected RecordObject data;

	/**
	 * UNIX time stamp to indicate the last updated received by the device
	 */
	protected long lastUpdate;

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
		this.deviceID = devicedefinition.address;
		this.protocol = BLUETOOTH_LOW_ENERGY;
		this.profile = devicedefinition.streams;
		// this.protocol =devicedefinition.protocol;
		this.deviceAgileID = AGILE_DEVICE_BASE_ID;

		String devicePath = AGILE_DEVICE_BASE_BUS_PATH + "ble" + devicedefinition.address.replace(":", "");
		;

		dbusConnect(deviceAgileID, devicePath, this);

		// if (protocol.equals(BLUETOOTH_LOW_ENERGY)) {
		deviceProtocol = (Protocol) connection.getRemoteObject(BLE_PROTOCOL_ID, BLE_PROTOCOL_PATH, Protocol.class);
		// }

		logger.debug("Exposed device {} {}", deviceAgileID, devicePath);

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
	public String Status() {
		return deviceStatus;
	}

	/**
	 * Returns the configuration of the devices
	 */
	public String Configuration() {
		logger.debug("Device. Subscribe not implemented");
		return null;
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
	public long LastUpdate() {
		return lastUpdate;
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

	/*
	 * @see iot.agile.devicemanager.device.Device#Connect()
	 */
	public void Connect() throws DBusException {
		try {
			if (protocol.equals(BLUETOOTH_LOW_ENERGY) && deviceProtocol != null) {
				deviceProtocol.Connect(deviceID);
				deviceStatus = CONNECTED;
				logger.info("Device Connected {}", deviceID);
			} else {
				logger.debug("Protocol not supported: {}", protocol);
			}
		} catch (DBusException e) {
			logger.error("Failed to connect device {}", deviceID);
			throw new DBusException("Failed to connect device:"+ deviceID);
		}
	}

	/*
	 * @see iot.agile.devicemanager.device.Device#Disconnect()
	 */
	public void Disconnect() throws DBusException {
		try {
			if (protocol.equals(BLUETOOTH_LOW_ENERGY) && deviceProtocol != null) {
				deviceProtocol.Disconnect(deviceID);
				deviceStatus = DISCONNECTED;
				logger.info("Device disconnected {}", deviceID);
			} else {
				logger.debug("Protocol not supported: {}", protocol);
			}

		} catch (DBusException e) {
			logger.error("Failed to disconnect device {}", deviceID);
			throw new DBusException("Failed to disconnect device:"+deviceID);
		}

	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.device.IDevice#Execute(java.lang.String)
	 */
	public void Execute(String command, Map<String,Variant> args) {
		logger.debug("Device. Execute not implemented");
	}

	/**
	 *
	 * Reads data from the given sensor
	 *
	 *
	 */
	@Override
	public RecordObject Read(String sensorName) {
		return null;
	}

	/**
	 * Writes data into the given sensor
	 *
	 * @see iot.agile.protocol.ble.device.IDevice#Write()
	 */
	public void Write() {
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
	public void Subscribe(String component) {
 	}

	/**
	 *
	 *
	 * @see org.freedesktop.dbus.DBusInterface#isRemote()
	 */
	public boolean isRemote() {
		return false;
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

	@Override
	public void Unsubscribe(String component) throws DBusException {
	}

}
