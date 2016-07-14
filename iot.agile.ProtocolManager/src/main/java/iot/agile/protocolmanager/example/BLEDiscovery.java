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
package iot.agile.protocolmanager.example;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.freedesktop.DBus.Error.ServiceUnknown;
import org.freedesktop.dbus.DBusConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.ProtocolManager;
import iot.agile.object.DeviceOverview;

/**
 * 
 * @author dagi
 * 
 *         This program demonstrates how client programs discoverBLE devices
 *         through DBus using {@code ProtocolManager},
 * 
 * 
 *
 */
public class BLEDiscovery {
  protected final static Logger logger = LoggerFactory.getLogger(BLEDiscovery.class);

  /**
   * DBus bus name for the protocol manager
   */
  private static final String AGILE_PROTOCOL_MANAGER_BUS_NAME = "iot.agile.ProtocolManager";
  /**
   * DBus bus path for the protocol manager
   */
  private static final String AGILE_PROTOCOL_MANAGER_BUS_PATH = "/iot/agile/ProtocolManager";
 
  public static void main(String[] args) {
    try {
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
      // Get Agile protocol manger interface from DBbus session bus
      ProtocolManager protocolManager = connection.getRemoteObject(AGILE_PROTOCOL_MANAGER_BUS_NAME,
          AGILE_PROTOCOL_MANAGER_BUS_PATH, ProtocolManager.class);
      logger.info("Discovering...1");

      protocolManager.Discover();
      logger.info("Discovering...");
     
        List<DeviceOverview> deviceList = protocolManager.Devices();
        for (DeviceOverview device : deviceList) {
          logger.info("Device ID: {}  Device Name: {}  Protocol Support: {} Device Status{}", device.id, device.name,
              device.protocol.replaceAll("iot.agile.protocol.", ""), device.status);
        }
  
      //

    } catch (ServiceUnknown e) {
      logger.error("Can not find the DBus object : {}", AGILE_PROTOCOL_MANAGER_BUS_PATH, e);
    } catch (Exception e) {
      logger.error("Error in discovering devices :", e);
    }
  }
}
