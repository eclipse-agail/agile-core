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

package org.eclipse.agail.http.service;

import org.eclipse.agail.Device;
import org.eclipse.agail.DeviceManager;
import org.eclipse.agail.Protocol;
import org.eclipse.agail.ProtocolManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.agail.object.AgileObjectInterface;

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

  protected synchronized DBusInterface getObject(String objectBusname, String objectPath, Class<? extends DBusInterface> clazz) throws DBusException {
    
    String key = objectBusname + ":" + objectPath;
    if(instances.containsKey(key)) {
      logger.debug("Load cached object {}:{}", objectBusname, objectPath);
      return instances.get(key);
    }
    
    try {
      DBusInterface obj = connection.getRemoteObject(objectBusname, objectPath, clazz);
      instances.put(key, obj);
      return obj;
    } catch (DBusException e) {
      logger.error("Failed to load object {}:{}", objectBusname, objectPath, e);
      throw e;
    }
  }
  
  public Device getDevice(String id) throws DBusException {
    String busname = Device.AGILE_INTERFACE;
    String path = "/" + Device.AGILE_INTERFACE.replace(".", "/")  + "/" + id;
    return (Device) getObject(busname, path, Device.class);
  }

  public Device getDevice(String busname, String id) throws DBusException {
    String path = "/" + busname.replace(".", "/")  + "/" + id;
    return (Device) getObject(busname, path, Device.class);
  }
  
  public Protocol getProtocol(String id) throws DBusException {
    String iface = Protocol.AGILE_INTERFACE;
    String path = "/" + Protocol.AGILE_INTERFACE.replace(".", "/") + "/" + id;
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
