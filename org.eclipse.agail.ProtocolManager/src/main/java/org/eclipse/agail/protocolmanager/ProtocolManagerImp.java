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
package org.eclipse.agail.protocolmanager;

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

import org.eclipse.agail.Protocol;
import org.eclipse.agail.ProtocolManager;
import org.eclipse.agail.object.AbstractAgileObject;
import org.eclipse.agail.object.DeviceOverview;
import org.eclipse.agail.object.DiscoveryStatus;
import org.eclipse.agail.object.ProtocolOverview;

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
	private static final String AGILE_PROTOCOL_MANAGER_BUS_NAME = "org.eclipse.agail.ProtocolManager";
	/**
	 * DBus bus path for the protocol manager
	 */
	private static final String AGILE_PROTOCOL_MANAGER_BUS_PATH = "/org/eclipse/agail/ProtocolManager";

	/**
	 * BLE Protocol Agile ID
	 */
	public static final String BLE_PROTOCOL_ID = "org.eclipse.agail.protocol.BLE";

	/**
	 * ZB Protocol Agile ID
	 */
	public static final String ZB_PROTOCOL_ID = "org.eclipse.agail.protocol.ZB";

	/**
	 * Dummy Protocol Agile ID 
	 */
	public static final String DUMMY_PROTOCOL_ID = "org.eclipse.agail.protocol.Dummy";
	
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
                        logger.info("device "+signal.device.getId() +"@"+System.currentTimeMillis());
                        for (int i =0; i<devices.size() ; i++) {
                            DeviceOverview dev=devices.get(i);    
			                if (dev.getId().equals(signal.device.getId())) {
                                DeviceOverview devNew=new DeviceOverview(signal.device.getId(),signal.device.getProtocol(),signal.device.getName(), signal.device.getStatus() + ":" + String.valueOf(System.currentTimeMillis()));    
                                logger.info(devNew.toString());          
                                devices.set(i,devNew);
                                //logger.info(devices.toString());
				            return;
			                }
		                } 

                        DeviceOverview devNew=new DeviceOverview(signal.device.getId(),signal.device.getProtocol(),signal.device.getName(), signal.device.getStatus() + ":" + String.valueOf(System.currentTimeMillis()));
                        devices.add(devNew);
						logger.info("Found new device signal received");
					}

				});

		logger.debug("ProtocolManager is running");
	}

	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.protocolmanager.ProtocolManager#Devices()
	 */
	public List<DeviceOverview> Devices() {
        return devices;
	}


	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.protocolmanager.ProtocolManager#Protocols()
	 */
	public List<ProtocolOverview> Protocols() {
        logger.info("protocols " + protocols.toString());
		return protocols;
	}

	/**
	 * @see org.eclipse.agail.protocol.ble.protocolmanager.ProtocolManager#DiscoveryStatus()
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
	 * @see org.eclipse.agail.protocol.ble.protocolmanager.ProtocolManager#StartDiscovery()
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
	 * @see org.eclipse.agail.protocol.ble.protocolmanager.ProtocolManager#StopDiscovery()
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
	 * @see org.eclipse.agail.protocol.ble.protocolmanager.ProtocolManager#Add(java.lang.String)
	 */
	public void Add(String protocol) {
		addProtocol(protocol);
	}

	/**
	 * @see org.eclipse.agail.protocol.ble.protocolmanager.ProtocolManager#Remove(java.lang.String)
	 */
	public void Remove(String protocol) {
		removeProtocol(protocol);
	}

	public boolean isRemote() {
		return false;
	}

	protected void addProtocol(String protocolId) {
        logger.info("add protocolID "+ protocolId);
        //Parse both full path + ID type parameters

        String protocolIDFullpath = protocolId;
        if (!protocolId.contains(".")) {
            protocolIDFullpath = "org.eclipse.agail.protocol." +protocolId; 
        } else {
            protocolIDFullpath  = protocolId;
            String[] protComp= protocolId.split(".");
            if (protComp.length >0) {    
                protocolId =protComp[protComp.length-1];
            }
        }

        for (ProtocolOverview prot : protocols){
           logger.info(protocolIDFullpath +"?"+prot.getId() +" : "+ prot.getName() +" : "+ prot.getDbusInterface());
            
            if (prot.getDbusInterface().equals(protocolIDFullpath)){
                logger.info("already exists"+ protocolId);
                return;
            }
        }


		switch (protocolIDFullpath) {
			case BLE_PROTOCOL_ID:
				protocols.add(new ProtocolOverview("BLE", "Bluetooth LE", protocolIDFullpath, "Avaliable"));
				break;
			case ZB_PROTOCOL_ID:
				protocols.add(new ProtocolOverview("ZB", "Zigbee", protocolIDFullpath, "Avaliable"));
				break;
			case DUMMY_PROTOCOL_ID:
				protocols.add(new ProtocolOverview("Dummy", "Dummy", protocolIDFullpath, "Avaliable"));
			    break;
            default:
              // TODO check classpath?
                protocols.add(new ProtocolOverview(protocolId,protocolId,protocolIDFullpath, "Avaliable"));
                
		}
	}

	protected void removeProtocol(String protocolId) {
       logger.info("remove protocol " + protocolId +":" + protocols.toString() +"contains: "+protocols.contains(protocolId));
        for (ProtocolOverview prot : protocols){
            logger.info(prot.getId().toString());
            if (prot.getId().equals(protocolId)){
                protocols.remove(prot);
                logger.info(prot.toString() + "removed");
                return;
            }
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
