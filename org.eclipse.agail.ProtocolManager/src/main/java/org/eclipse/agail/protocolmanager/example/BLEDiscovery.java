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

package org.eclipse.agail.protocolmanager.example;

import java.util.List;

import org.freedesktop.DBus.Error.ServiceUnknown;
import org.freedesktop.dbus.DBusConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.ProtocolManager;
import org.eclipse.agail.object.DeviceOverview;

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
  private static final String AGILE_PROTOCOL_MANAGER_BUS_NAME = "org.eclipse.agail.ProtocolManager";
  /**
   * DBus bus path for the protocol manager
   */
  private static final String AGILE_PROTOCOL_MANAGER_BUS_PATH = "/org/eclipse/agail/ProtocolManager";

  public static void main(String[] args) {
    try {
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
      // Get Agile protocol manager interface from DBbus session bus
      ProtocolManager protocolManager = connection.getRemoteObject(AGILE_PROTOCOL_MANAGER_BUS_NAME,
          AGILE_PROTOCOL_MANAGER_BUS_PATH, ProtocolManager.class);

      protocolManager.StartDiscovery();
      logger.info("Discovering...");

      
      //Read devices from protocol manager
      List<DeviceOverview> deviceList = protocolManager.Devices();
      for (DeviceOverview device : deviceList) {
        logger.info("Device ID: {}  Device Name: {}  Protocol Support: {} Device Status{}", device.id, device.name,
            device.protocol.replaceAll("org.eclipse.agail.protocol.", ""), device.status);
      }
    } catch (ServiceUnknown e) {
      logger.error("Can not find the DBus object : {}", AGILE_PROTOCOL_MANAGER_BUS_PATH, e);
    } catch (org.freedesktop.DBus.Error.UnknownMethod UM) {
      logger.debug("Unkown method");

    } catch (Exception e) {
      logger.error("Error in discovering devices :", e);
    }
  }
}
