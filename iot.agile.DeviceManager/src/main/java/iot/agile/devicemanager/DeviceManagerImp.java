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
package iot.agile.devicemanager;

import java.util.HashMap;
import java.util.Map;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.Device;
import iot.agile.DeviceManager;
import iot.agile.devicemanager.device.DeviceImp;
import iot.agile.devicemanager.device.TISensorTag;

/**
 * @author dagi
 *
 * Agile Device manager implementation
 *
 */
public class DeviceManagerImp implements DeviceManager {
  
  protected final Logger logger = LoggerFactory.getLogger(DeviceManagerImp.class);
  
  /**
   * Bus name for the device manager
   */
  private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_NAME = "iot.agile.DeviceManager";
  /**
   * Bus path for the device manager
   */
  private static final String AGILE_DEVICEMANAGER_MANAGER_BUS_PATH = "/iot/agile/DeviceManager";

  private static final String AGILE_DEVICE_BASE_ID = "iot.agile.device.";

  /**
   * DBus connection to the device manager
   */
  protected final DBusConnection connection;

  /**
   * registered devices
   */
  protected final Map<String, String> devices = new HashMap<String, String>();

  public static void main(String[] args) throws DBusException {
    DeviceManager deviceManager = new DeviceManagerImp();
  }

  public DeviceManagerImp() throws DBusException {
    
    connection = dbusConnect();

    // ensure DBus object is unregistered
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          connection.releaseBusName(AGILE_DEVICEMANAGER_MANAGER_BUS_NAME);
        } catch (DBusException ex) {
          logger.error("Cannot release DBus name {}", AGILE_DEVICEMANAGER_MANAGER_BUS_NAME, ex);
        }
      }
    });
    
    
    logger.debug("Started Device Manager");
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Find()
   */
  @Override
  public String Find() {
    // TODO
    return null;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Create()
   */
  @Override
  public String Create(String deviceID, String deviceName, String protocol) throws DBusException {
	    logger.debug("Creating new device id: {} name: {} protocol: {}", deviceID, deviceName, protocol);
	    
	    // check if it not registered or not connected
	    Device device = new TISensorTag(deviceID, deviceName, protocol);
	    if (!isRegistered(device.Id())) {
	      devices.put(deviceID, device.Id());
	    }

	    return device.Id();
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Read(java.lang.
   * String)
   */
  @Override
  public void Read(String id) {
    logger.debug("DeviceManager.Read not implemented");
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Update(java.lang.
   * String, java.lang.String)
   */
  @Override
  public boolean Update(String id, String definition) {
    logger.debug("DeviceManager.Update not implemented");
    return false;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.devicemanager.DeviceManager#devices()
   */
  @Override
  public Map<String, String> devices() {
    return devices;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Delete(java.lang.
   * String, java.lang.String)
   */
  @Override
  public void Delete(String id, String definition) {
    logger.debug("DeviceManager.Delete not implemented");
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.devicemanager.DeviceManager#Batch(java.lang.
   * String, java.lang.String)
   */
  @Override
  public boolean Batch(String operation, String arguments) {
    logger.debug("DeviceManager.Batch not implemented");
    return false;
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

  // ====================Utility methods
  @Override
  public void DropBus() {
    connection.disconnect();
  }

  private boolean isRegistered(String deviceAgileID) {
    if (devices.isEmpty()) {
      return false;
    }
    return devices.containsKey(deviceAgileID);
  }

  @Override
  public DBusConnection dbusConnect() throws DBusException {
    
    DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
    
    connection.requestBusName(AGILE_DEVICEMANAGER_MANAGER_BUS_NAME);
    connection.exportObject(AGILE_DEVICEMANAGER_MANAGER_BUS_PATH, this);
    
    return connection;
  }

  @Override
  public void dbusDisconnect() {
    connection.disconnect();
  }

}
