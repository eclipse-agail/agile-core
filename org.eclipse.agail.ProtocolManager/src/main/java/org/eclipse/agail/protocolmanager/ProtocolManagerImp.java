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
import org.eclipse.agail.protocolmanager.persistence.PersistenceDB;
import org.eclipse.agail.protocolmanager.persistence.ProtocolWithConfig;
import org.eclipse.agail.protocols.config.ProtocolConfig;

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
	
	private PersistenceDB persistenceDB;
	
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
		
		persistenceDB = new PersistenceDB();
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
        List<ProtocolWithConfig> protocolFromDB = persistenceDB.readData();
		logger.info("Protocols {} fround in persistenceDB.", protocolFromDB.size());
		for (int i = 0; i < protocolFromDB.size(); i++) {
			ProtocolWithConfig p = protocolFromDB.get(i);
			logger.info("ProtocolWithConfig {}", p);
			ProtocolOverview _protocolOverview = getProtocolOverview(p.getProtocolId());
			logger.info("ProtocolOverview {}", _protocolOverview);
			if(_protocolOverview != null && !protocols.isEmpty() && !protocols.contains(_protocolOverview)) {
				protocols.add(_protocolOverview);
			}
			LoadProtocolConfigurations(p.getProtocolId());
		}
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
				logger.debug("ProtocolOverview: {}", protocolInstance);
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
	
	ProtocolOverview getProtocolOverview(String protocolId) {
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
           logger.info(protocolIDFullpath +" ? "+prot.getId() +" : "+ prot.getName() +" : "+ prot.getDbusInterface());
            
            if (prot.getDbusInterface().equals(protocolIDFullpath)){
                logger.info("already exists => "+ protocolId);
                return null;
            }
        }

        ProtocolOverview _protocolOverview;
		switch (protocolIDFullpath) {
			case BLE_PROTOCOL_ID:
				_protocolOverview = new ProtocolOverview("BLE", "Bluetooth LE", protocolIDFullpath, "Avaliable");
				break;
			case ZB_PROTOCOL_ID:
				_protocolOverview = new ProtocolOverview("ZB", "Zigbee", protocolIDFullpath, "Avaliable");
				break;
			case DUMMY_PROTOCOL_ID:
				_protocolOverview = new ProtocolOverview("Dummy", "Dummy", protocolIDFullpath, "Avaliable");
			    break;
            default:
              // TODO check classpath?
            	_protocolOverview = new ProtocolOverview(protocolId,protocolId,protocolIDFullpath, "Avaliable");
		}
		
		return _protocolOverview;
	}

	protected void addProtocol(String protocolId) {
        logger.info("add protocolID "+ protocolId);
        //Parse both full path + ID type parameters
        
        ProtocolOverview _protocolOverview = getProtocolOverview(protocolId);
		if(_protocolOverview != null) {
			protocols.add(_protocolOverview);
		}
		
	}

	protected void removeProtocol(String protocolId) {
       logger.info("remove protocol " + protocolId +":" + protocols.toString() +"contains: "+protocols.contains(protocolId));
        for (ProtocolOverview prot : protocols){
            logger.info(prot.getId().toString());
            if (prot.getId().equals(protocolId)){
                protocols.remove(prot);
                persistenceDB.deletprotocol(protocolId);
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
	
	void LoadProtocolConfigurations(String protocolId) {
		String objectPath = getObjectPath(protocolId);
		if(objectPath != null) {
			List<ProtocolConfig> configs = GetProtocolConfigurations(protocolId);
			
			if(configs != null && configs.size() > 0) {
				Protocol protocolInstance = null;
				try {
					protocolInstance = connection.getRemoteObject(protocolId, objectPath, Protocol.class);
					protocolInstance.SetConfiguration(configs);
					} catch(ServiceUnknown ex) {
						logger.info("{} protocol is not supported", protocolInstance.Name()); 
					} catch(DBusException ex) {
						logger.error("DBus exception on protocol {}", objectPath, ex);
				}
			}
		}
	}

	@Override
	public List<ProtocolConfig> GetProtocolConfigurations(String protocolId) {
		ProtocolWithConfig protocolWithConfig = persistenceDB.getProtocolWithConfig(protocolId);
		List<ProtocolConfig> configs = new ArrayList<ProtocolConfig>();
		
		if(protocolWithConfig != null && protocolWithConfig.getConfigurations().size() > 0) {
			configs = protocolWithConfig.getConfigurations();
		}
		
		logger.debug("GetProtocolConfig: {}", configs);
		
		return configs;
	}

	@Override
	public void SetProtocolConfigurations(String protocolId, List<ProtocolConfig> protocolConfigs) {
		
		if(protocolConfigs != null && protocolConfigs.size() > 0) {
			logger.debug("{} Procotol Configurations: {}", protocolId, protocolConfigs);
			persistenceDB.saveProtocol(protocolId, protocolConfigs);
		}
	}
	
	private String getObjectPath(String protocolId) {
		String objectPath = "";
		for (ProtocolOverview protocol : protocols) {
			logger.info("ObjectPath for protocol {} : {}", protocolId, protocol.getDbusInterface());
			if(protocol.getDbusInterface().equals(protocolId)) {
				objectPath = "/" + protocol.getDbusInterface().replace(".", "/");
				logger.info("ObjectPath for protocol {} : {}", protocol, objectPath);
			}
		}
		
		logger.info("Final *************************************************** {}", objectPath);
		return objectPath;
	}

}
