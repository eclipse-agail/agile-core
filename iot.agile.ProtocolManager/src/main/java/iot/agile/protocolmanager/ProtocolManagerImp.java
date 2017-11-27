/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/
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
import java.util.Map;

import org.freedesktop.DBus.Error.ServiceUnknown;
import org.freedesktop.DBus.Properties;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.Protocol;
import iot.agile.ProtocolManager;
import iot.agile.object.AbstractAgileObject;
import iot.agile.object.DeviceOverview;
import iot.agile.object.DiscoveryStatus;
import iot.agile.object.ProtocolOverview;

/**
 * @author dagi
 *
 *         AGILE Protocol Manager Implementation
 *
 */
public class ProtocolManagerImp extends AbstractAgileObject implements ProtocolManager, Properties {

	protected final Logger logger = LoggerFactory.getLogger(ProtocolManagerImp.class);

	/**
	 * DBus bus name for the protocol manager
	 */
	private static final String AGILE_PROTOCOL_MANAGER_BUS_NAME = "iot.agile.ProtocolManager";
	/**
	 * DBus bus path for the protocol manager
	 */
	private static final String AGILE_PROTOCOL_MANAGER_BUS_PATH = "/iot/agile/ProtocolManager";

	/**
	 * BLE Protocol Agile ID
	 */
	public static final String BLE_PROTOCOL_ID = "iot.agile.protocol.BLE";

	/**
	 * ZB Protocol Agile ID
	 */
	public static final String ZB_PROTOCOL_ID = "iot.agile.protocol.ZB";

	/**
	 * Dummy Protocol Agile ID 
	 */
	public static final String DUMMY_PROTOCOL_ID = "iot.agile.protocol.Dummy";
	
	/**
	 * List of supported protocols
	 */
	final private List<ProtocolOverview> protocols = new ArrayList<ProtocolOverview>();

	/**
	 * List of discovered devices from all the protocols
	 */
	final private List<DeviceOverview> devices = new ArrayList<DeviceOverview>();

	public static void main(String[] args) throws DBusException {
		ProtocolManager protocolManager = new ProtocolManagerImp();

		// for demo purposes
		protocolManager.Add(BLE_PROTOCOL_ID);
		protocolManager.Add(DUMMY_PROTOCOL_ID);
 	}

	public ProtocolManagerImp() throws DBusException {
		dbusConnect(AGILE_PROTOCOL_MANAGER_BUS_NAME, AGILE_PROTOCOL_MANAGER_BUS_PATH, this);

		connection.addSigHandler(ProtocolManager.FoundNewDeviceSignal.class,
				new DBusSigHandler<ProtocolManager.FoundNewDeviceSignal>() {

					@Override
					public void handle(FoundNewDeviceSignal signal) {
						devices.add(signal.device);
						logger.info("Found new device signal received");
					}

				});
		logger.debug("ProtocolManager is running");
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Devices()
	 */
	public List<DeviceOverview> Devices() {
		return devices;
	}

	/**
	 *
	 *
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Protocols()
	 */
	public List<ProtocolOverview> Protocols() {
		return protocols;
	}

	/**
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#DiscoveryStatus()
	 */
	public List<DiscoveryStatus> DiscoveryStatus() {
		logger.info("discovery status");

		List<DiscoveryStatus> ret = new ArrayList<DiscoveryStatus>();

		for (ProtocolOverview protocol : protocols) {
			String objectPath = "/" + protocol.getDbusInterface().replace(".", "/");

			Protocol protocolInstance;
			try {

				protocolInstance = connection.getRemoteObject(protocol.getDbusInterface(), objectPath, Protocol.class);
				String status = protocolInstance.DiscoveryStatus();
				ret.add(new DiscoveryStatus(protocol.getDbusInterface(), status));
			} catch(ServiceUnknown ex){
				logger.info("{} protocol is not supported", protocol.name); 
				ret.add(new DiscoveryStatus(protocol.getDbusInterface(), "FAILURE"));
			} catch (DBusException ex) {
				logger.error("DBus exception on protocol {}", protocol, ex);
				ret.add(new DiscoveryStatus(protocol.getDbusInterface(), "FAILURE"));
			}
		}
		return ret;
	}

	/**
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#StartDiscovery()
	 */
	public void StartDiscovery() {
		logger.info("Initializing discovery");

		for (ProtocolOverview protocol : protocols) {
			String objectPath = "/" + protocol.getDbusInterface().replace(".", "/");
			logger.info("Discovery for protocol {} : {}", protocol, objectPath);

			Protocol protocolInstance;
			try {
				protocolInstance = connection.getRemoteObject(protocol.getDbusInterface(), objectPath, Protocol.class);
 				protocolInstance.StartDiscovery();
 			}catch(ServiceUnknown ex){
				logger.info("{} protocol is not supported", protocol.name); 
 			}catch (DBusException ex) {
				logger.error("DBus exception on protocol {}", protocol, ex);
			}

		}
	}

	/**
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#StopDiscovery()
	 */
	public void StopDiscovery() {
		for (ProtocolOverview protocol : protocols) {
			String objectPath = "/" + protocol.getDbusInterface().replace(".", "/");
			logger.info("StopDiscovery for protocol {} : {}", protocol, objectPath);

			Protocol protocolInstance;
			try {
				protocolInstance = connection.getRemoteObject(protocol.getDbusInterface(), objectPath, Protocol.class);
				protocolInstance.StopDiscovery();
			}catch(ServiceUnknown ex){
				logger.info("{} protocol is not supported", protocol.name); 
			} catch (DBusException ex) {
				logger.error("DBus exception on protocol {}", protocol, ex);
			}
		}
	}

	/**
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Add(java.lang.String)
	 */
	public void Add(String protocol) {
		addProtocol(protocol);
	}

	/**
	 * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Remove(java.lang.String)
	 */
	public void Remove(String protocol) {
		removeProtocol(protocol);
	}

	public boolean isRemote() {
		return false;
	}

	protected void addProtocol(String protocolId) {
		if (!protocols.contains(protocolId)) {
			switch (protocolId) {
				case BLE_PROTOCOL_ID:
					protocols.add(new ProtocolOverview("BLE", "Bluetooth LE", protocolId, "Avaliable"));
					break;
				case ZB_PROTOCOL_ID:
					protocols.add(new ProtocolOverview("ZB", "ZigBee", protocolId, "Avaliable"));
					break;
				case DUMMY_PROTOCOL_ID:
					protocols.add(new ProtocolOverview("Dummy", "Dummy", protocolId, "Avaliable"));
				break;
			}
		}
	}

	protected void removeProtocol(String protocolId) {
		if (protocols.contains(protocolId)) {
			protocols.remove(protocolId);
		}
	}

	@Override
	public <A> A Get(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Variant> GetAll(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> void Set(String arg0, String arg1, A arg2) {
		// TODO Auto-generated method stub

	}

}
