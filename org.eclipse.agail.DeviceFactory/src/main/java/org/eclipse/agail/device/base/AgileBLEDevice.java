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

import java.util.Map;
import org.freedesktop.dbus.exceptions.DBusException;
import org.eclipse.agail.Device;
import org.eclipse.agail.Protocol;
import org.eclipse.agail.object.DeviceDefinition;
import org.eclipse.agail.object.DeviceOverview;
import org.eclipse.agail.object.DeviceStatusType;

public abstract class AgileBLEDevice extends DeviceImp implements Device {

	private static final String BLE = "ble";

	/**
	 * BLE Protocol imp DBus interface id
	 */
	private static final String BLE_PROTOCOL_ID = "org.eclipse.agail.protocol.BLE";
	/**
	 * BLE Protocol imp DBus interface path
	 */
	private static final String BLE_PROTOCOL_PATH = "/org/eclipse/agail/protocol/BLE";

	/**
	 * Protocol
	 */
	protected static final String BLUETOOTH_LOW_ENERGY = "org.eclipse.agail.protocol.BLE";
	protected static final String GATT_SERVICE = "GATT_SERVICE";
	protected static final String GATT_CHARACTERSTICS = "GATT_CHARACTERSTICS";


	public AgileBLEDevice(DeviceOverview deviceOverview) throws DBusException {
		super(deviceOverview);
		this.protocol = BLUETOOTH_LOW_ENERGY;
		String devicePath = AGILE_DEVICE_BASE_BUS_PATH + BLE + deviceOverview.id.replace(":", "");

		dbusConnect(deviceAgileID, devicePath, this);
		deviceProtocol = (Protocol) connection.getRemoteObject(BLE_PROTOCOL_ID, BLE_PROTOCOL_PATH, Protocol.class);
		logger.debug("Exposed device {} {}", deviceAgileID, devicePath);
	}

	public AgileBLEDevice(DeviceDefinition devicedefinition) throws DBusException {
		super(devicedefinition);
		this.protocol = BLUETOOTH_LOW_ENERGY;

		String devicePath = AGILE_DEVICE_BASE_BUS_PATH + BLE + devicedefinition.address.replace(":", "");

		dbusConnect(deviceAgileID, devicePath, this);
		deviceProtocol = (Protocol) connection.getRemoteObject(BLE_PROTOCOL_ID, BLE_PROTOCOL_PATH, Protocol.class);
		logger.debug("Exposed device {} {}", deviceAgileID, devicePath);
	}

	@Override
	public void Connect() throws DBusException {
		try {
			if (protocol.equals(BLUETOOTH_LOW_ENERGY) && deviceProtocol != null) {
				deviceProtocol.Connect(address);
				logger.info("Device connect {}", deviceID);
			} else {
				logger.debug("Protocol not supported: {}", protocol);
			}

		} catch (DBusException e) {
			logger.error("Failed to connect device {}", deviceID);
			throw new DBusException("Failed to connect device:" + deviceID);
		}
	}

	public void Disconnect() throws DBusException {
		try {
			if (protocol.equals(BLUETOOTH_LOW_ENERGY) && deviceProtocol != null) {
				deviceProtocol.Disconnect(address);
				logger.info("Device disconnected {}", deviceID);
			} else {
				logger.debug("Protocol not supported: {}", protocol);
			}
		} catch (DBusException e) {
			logger.error("Failed to disconnect device {}", deviceID);
			throw new DBusException("Failed to disconnect device:" + deviceID);
		}
	}
	
	/**
	 * Given the profile of the component returns the name of the sensor
	 * 
	 * @param uuid
	 * @return
	 */
	protected String getComponent(Map<String, String> profile) {
		return null;
	}

	@Override
	protected boolean isConnected() {
		if (Status().getStatus().equals(DeviceStatusType.CONNECTED.toString())) {
			return true;
		}
		return false;
	}

}
