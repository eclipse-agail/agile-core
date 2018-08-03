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
package org.eclipse.agail.devicemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.agail.Device;
import org.eclipse.agail.DeviceFactory;
import org.eclipse.agail.DeviceManager;
import org.eclipse.agail.devicemanager.jsondb.DeviceWithType;
import org.eclipse.agail.devicemanager.jsondb.JsonDB;
import org.eclipse.agail.exception.AgileDeviceNotFoundException;
import org.eclipse.agail.object.AbstractAgileObject;
import org.eclipse.agail.object.DeviceDefinition;
import org.eclipse.agail.object.DeviceOverview;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dagi
 *
 *         Agile Device manager implementation
 *
 */
public class DeviceManagerImp extends AbstractAgileObject implements DeviceManager {

	protected final Logger logger = LoggerFactory.getLogger(DeviceManagerImp.class);

	/**
	 * Bus name for the device manager
	 */
	private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_NAME = "org.eclipse.agail.DeviceManager";
	/**
	 * Bus path for the device manager
	 */
	private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_PATH = "/org/eclipse/agail/DeviceManager";

	/**
	 * registered devices
	 */
	protected final List<DeviceDefinition> devices = new ArrayList<DeviceDefinition>();

	//List of available device factories
	private static List<Map<String, String>> factoryList= new ArrayList<>();

	private JsonDB db;

	public static void main(String[] args) throws DBusException {
		DeviceManager deviceManager = new DeviceManagerImp();
	}

	public DeviceManagerImp() throws DBusException {

		dbusConnect(AGILE_DEVICEMANAGER_MANAGER_BUS_NAME, AGILE_DEVICEMANAGER_MANAGER_BUS_PATH, this);
		logger.debug("Started Device Manager");

		db = new JsonDB();
		//TODO : get  device factories from a properties file or ...
		Map<String,String> map = new HashMap<>();
		map.put("org.eclipse.agail.DeviceFactory","/org/eclipse/agail/DeviceFactory");
		AddFactoryToList(map);
	}

	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.devicemanager.DeviceManager#Find()
	 */
	@Override
	public String Find() {
		// TODO
		return null;
	}

	@Override
	public List<String> MatchingDeviceTypes(DeviceOverview deviceOverview) {
		List<String> ret = new ArrayList();
		try{
		    String objectName = "org.eclipse.agail.DeviceFactory";
      		    String objectPath = "/org/eclipse/agail/DeviceFactory";
      		    logger.info("Connection established: "+connection);
		    for (Map<String, String> m : factoryList){
			    for (Map.Entry<String, String> entry : m.entrySet()){
				      logger.debug("Getting device factory with name = " + entry.getKey() + ", and path = " + entry.getValue());
					    DeviceFactory factory = (DeviceFactory) connection.getRemoteObject(entry.getKey(), entry.getValue(),DeviceFactory.class);
					    ret=factory.MatchingDeviceTypes(deviceOverview);
			    }
		    }
        	}
		catch (Exception e) {
            	    logger.error("Can not connect to the DeviceFactory DBus object: {}", e.getMessage());
    	            e.printStackTrace();
    		}
		return ret;
	}


	@Override
	public DeviceDefinition Register(DeviceOverview deviceOverview, String deviceType) {
		Device device = getDevice(deviceOverview);
		DeviceDefinition registeredDev = null;
		if (device != null) {
			registeredDev = device.Definition();
			logger.info("Device already registered:  {}", device.Id());
		} else {
			try {
				logger.info("HEXIWEAR - Checking device type: " + deviceType + " and overview " + deviceOverview);

				String objectName = "org.eclipse.agail.DeviceFactory";
				String objectPath = "/org/eclipse/agail/DeviceFactory";
				logger.info("Connection established: " + connection);
				for (Map<String, String> m : factoryList){
					for (Map.Entry<String, String> entry : m.entrySet())
					{
						  logger.debug("Getting device factory with name = " + entry.getKey() + ", and path = " + entry.getValue());
							DeviceFactory factory = (DeviceFactory) connection.getRemoteObject(entry.getKey(), entry.getValue(),
									DeviceFactory.class);
							device = factory.getDevice(deviceType, deviceOverview);
							logger.info("Creating new device: {}", deviceType);
							if (device != null) {
								registeredDev = device.Definition();
								db.saveDevice(deviceOverview, deviceType);
								devices.add(registeredDev);
								logger.info("Created new device: {}", devices);
							}
					}
				}
			} catch (Exception e) {
				logger.error("Can not register device: {}", e.getMessage());
				e.printStackTrace();
			}
		}
		// connect device
		if (device != null) {
			final Device dev = device;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						dev.Connect();
						logger.info("Device connected");
					} catch (Exception e) {
						logger.error("Error encountered while attempting to connect: {}", e.getMessage());
					}
				}
			}).start();
		}
		return registeredDev;
	}

	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.devicemanager.DeviceManager#Read(java.lang.
	 *      String)
	 */
	@Override
	public DeviceDefinition Read(String id) {
		for (DeviceDefinition dd : devices) {
			if (dd.deviceId.trim().equals(id)) {
				return dd;
			}
		}
		throw new AgileDeviceNotFoundException("Device not found");
	}

	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.devicemanager.DeviceManager#Update(java.lang.
	 *      String, java.lang.String)
	 */
	@Override
	public void Update(String id, DeviceDefinition definition) {
		logger.debug("DeviceManager.Update not implemented");
	}

	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.devicemanager.DeviceManager#Devices()
	 */
	@Override
	public List<DeviceDefinition> Devices() {
		List<DeviceWithType> devs = db.readData();
		logger.info("******************** Devices {} fround in jsonDB.", devs.size());
		for (int i = 0; i < devs.size(); i++) {
			DeviceWithType dev = devs.get(i);
			Register(dev.getDeviceOverview(), dev.getDeviceType());
			// logger.info("Devices (id= {}, name= {}) with type ({}) fround in jsonDB.",
			// dev.getDeviceOverview().id, dev.getDeviceOverview().name,
			// dev.getDeviceType());
		}
		return devices;
	}

	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.devicemanager.DeviceManager#Delete(java.lang.
	 *      String, java.lang.String)
	 */
	@Override
	public void Delete(String id) {
		logger.info("Deleting device {}", id);
		DeviceDefinition devDefn = Read(id);
		logger.info("Device deleted: {}, {}, {}", devDefn.deviceId, devDefn.getAddress(), devDefn.name);
		if (devDefn != null) {
			Device device = getDevice(devDefn);
			if (device != null) {
				try {
					device.Stop();
					devices.remove(devDefn);
					db.deletDevice(devDefn.getAddress());
					logger.info("Device deleted: {}", id);
				} catch (Exception e) {
				    logger.error("Unable to delete device: {}", id);
				    e.printStackTrace();
				}
			}
		}
	}

	/**
	 *
	 *
	 * @see org.eclipse.agail.protocol.ble.devicemanager.DeviceManager#Batch(java.lang.
	 *      String, java.lang.String)
	 */
	@Override
	public void Batch(String operation, String arguments) {
		logger.debug("DeviceManager.Batch not implemented");
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.freedesktop.dbus.DBusInterface#isRemote()
	 */
	@Override
	public boolean isRemote() {
		return false;
	}

	/**
	 * Get device based on {@code DeviceDefinition}
	 *
	 * @param devDef
	 *            Device definition
	 * @return
	 */
	private Device getDevice(DeviceDefinition devDef) {
		String objectName = "org.eclipse.agail.Device";
    String objectPath = "/org/eclipse/agail/Device/"+devDef.getProtocol().replace("org.eclipse.agail.protocol.", "").toLowerCase() + devDef.address.replace(":", "");
     try {
			Device device = (Device) connection.getRemoteObject(objectName, objectPath);
			return device;
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Get device based on {@code DeviceDefinition}
	 *
	 * @param devDef
	 *            Device definition
	 * @return
	 */
	private Device getDevice(DeviceOverview devOverview) {
		String objectName = "org.eclipse.agail.Device";
		String objectPath = "/org/eclipse/agail/Device/"+devOverview.getProtocol().replace("org.eclipse.agail.protocol.", "").toLowerCase() + devOverview.id.replace(":", "");
 		try {
			Device device = (Device) connection.getRemoteObject(objectName, objectPath);
			return device;
		} catch (Exception e) {
			return null;
		}

	}


	/**
	 * Add a device factory to the list.
	 */
	private void AddFactoryToList(Map map) {

				logger.debug("Adding factory to the list");
				if (!factoryList.contains(map)) {
						factoryList.add(map);
						logger.debug("Added device factory : " + map);
					}
					else {
						logger.debug("Device factory "+ map +" already present");
					}
	}

}
