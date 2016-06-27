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

import iot.agile.agile.interfaces.Protocol;
import iot.agile.agile.interfaces.ProtocolManager;
import java.util.ArrayList;
import java.util.List;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dagi
 *
 * AGILE Protocol Manager Implementation
 *
 */
public class ProtocolManagerImp implements ProtocolManager {

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
   * List of supported protocols
   */
  final private List<String> protocols = new ArrayList();
  /**
   * List of discovered devices from all the protocols
   */
  final private List<String> devices = new ArrayList();

  private final DBusConnection connection;

  public static final String BLE_PROTOCOL_ID = "iot.agile.protocol.BLE";

  public static void main(String[] args) throws DBusException {
    ProtocolManager protocolManager = new ProtocolManagerImp();

    // for demo purposes
    protocolManager.Add(BLE_PROTOCOL_ID);
  }

  public ProtocolManagerImp() throws DBusException {

    connection = DBusConnection.getConnection(DBusConnection.SESSION);

    connection.requestBusName(AGILE_PROTOCOL_MANAGER_BUS_NAME);
    connection.exportObject(AGILE_PROTOCOL_MANAGER_BUS_PATH, this);

    // ensure DBus object is unregistered
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          connection.releaseBusName(AGILE_PROTOCOL_MANAGER_BUS_NAME);
        } catch (DBusException ex) {
          logger.error("Cannot release DBus name {}", AGILE_PROTOCOL_MANAGER_BUS_NAME, ex);
        }
      }
    });

    logger.debug("ProtocolManager is running");
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Devices()
   */
  public List<String> Devices() {
    return devices;
  }

  /**
   *
   *
   * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Protocols()
   */
  public List<String> Protocols() {
    return protocols;
  }

  /**
   * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#Discover()
   */
  public void Discover() {

    logger.debug("Initialized discovery");

    for (String protocol : protocols) {

      String objectPath = "/" + protocol.replace(".", "/");
      logger.debug("Discovery for protocol {} : {}", protocol, objectPath);

      Protocol protocolInstance;
      try {

        protocolInstance = connection.getRemoteObject(protocol, objectPath, Protocol.class);
        protocolInstance.Discover();

        for (String device : protocolInstance.Devices()) {
          if (!devices.contains(device)) {
            devices.add(device);
          }
        }

        protocolInstance.StopDiscovery();

      } catch (DBusException ex) {
        logger.error("DBus exception on protocol {}", protocol, ex);
      }

    }

  }

  /**
   * @see
   * iot.agile.protocol.ble.protocolmanager.ProtocolManager#Add(java.lang.String)
   */
  public void Add(String protocol) {
    addProtocol(protocol);
  }

  /**
   * @see
   * iot.agile.protocol.ble.protocolmanager.ProtocolManager#Remove(java.lang.String)
   */
  public void Remove(String protocol) {
    removeProtocol(protocol);
  }

  public boolean isRemote() {
    return false;
  }

  /**
   * @see iot.agile.protocol.ble.protocolmanager.ProtocolManager#DropBus()
   */
  public void DropBus() {
    connection.disconnect();
  }

  public void addDevice(String deviceId) {
    if (!devices.contains(deviceId)) {
      devices.add(deviceId);
    }
  }

  public void removeDevice(String deviceId) {
    if (devices.contains(deviceId)) {
      devices.remove(deviceId);
    }
  }

  protected void addProtocol(String protocolId) {
    if (!protocols.contains(protocolId)) {
      protocols.add(protocolId);
    }
  }

  protected void removeProtocol(String protocolId) {
    if (protocols.contains(protocolId)) {
      protocols.remove(protocolId);
    }
  }

}
