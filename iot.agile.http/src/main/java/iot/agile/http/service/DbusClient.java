/*
 * Copyright 2016 CREATE-NET
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
package iot.agile.http.service;

import iot.agile.Device;
import iot.agile.DeviceManager;
import iot.agile.Protocol;
import iot.agile.ProtocolManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import iot.agile.object.AgileObjectInterface;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class DbusClient {

  protected Logger logger = LoggerFactory.getLogger(DbusClient.class);

  private ConcurrentMap<String, DBusInterface> instances = new ConcurrentHashMap();

  DBusConnection connection;

  public DbusClient() throws DBusException {
    connection = DBusConnection.getConnection(AgileObjectInterface.DEFAULT_DBUS_CONNECTION);
  }

  protected DBusInterface getObject(String objectInterface, String objectPath, Class<? extends DBusInterface> clazz) throws DBusException {
    
    String key = objectInterface + ":" + objectPath;
    if(instances.containsKey(key)) {
      logger.debug("Load cached object {}:{}", objectInterface, objectPath);
      return instances.get(key);
    }
    
    try {
      DBusInterface obj = connection.getRemoteObject(objectInterface, objectPath, clazz);
      instances.put(key, obj);
      return obj;
    } catch (DBusException e) {
      logger.error("Failed to load object {}:{}", objectInterface, objectPath, e);
      throw e;
    }
  }
  
  public Device getDevice(String id) throws DBusException {
    String iface = Device.AGILE_INTERFACE + "." + id;
    String path = "/" + Device.AGILE_INTERFACE.replace(".", "/");
    return (Device) getObject(iface, path, Device.class);
  }
  
  public Protocol getProtocol(String id) throws DBusException {
    String iface = Protocol.AGILE_INTERFACE + "." + id;
    String path = "/" + Protocol.AGILE_INTERFACE.replace(".", "/");
    return (Protocol) getObject(iface, path, Protocol.class);
  }
  
  public ProtocolManager getProtocolManager() throws DBusException {
    String iface = ProtocolManager.AGILE_INTERFACE;
    String path = "/" + ProtocolManager.AGILE_INTERFACE.replace(".", "/");
    return (ProtocolManager) getObject(iface, path, ProtocolManager.class);
  }
  
  public DeviceManager getDeviceManager() throws DBusException {
    String iface = DeviceManager.AGILE_INTERFACE;
    String path = "/" + DeviceManager.AGILE_INTERFACE.replace(".", "/");
    return (DeviceManager) getObject(iface, path, DeviceManager.class);
  }

}
