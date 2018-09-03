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

package org.eclipse.agail.http.ws;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.exceptions.DBusException;

import org.eclipse.agail.Device;
import org.eclipse.agail.Device.NewSubscribeValueSignal;
import org.eclipse.agail.object.StatusType;
import org.eclipse.agail.object.DeviceStatusType;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Csaba Kiraly <kiraly@fbk.eu>
 */
@WebSocket
public class AgileWebSocketAdapter extends WebSocketAdapter {

  private Session session;
  private String deviceID = null;
  private String sensorName = null;
  private DBusSigHandler sigHandler;
  private boolean subscribed = false;

  private boolean websocketOpen = false;

  private ObjectMapper mapper = new ObjectMapper();

  protected Logger logger = LoggerFactory.getLogger(AgileWebSocketAdapter.class);

  @Override
  public void onWebSocketConnect(Session sess) {
    websocketOpen = true;
    session = sess;
    String uripath = sess.getUpgradeRequest().getRequestURI().getPath();
    logger.info("New websocket connection from {} for {}", sess.getRemoteAddress(), uripath);

    Pattern p = Pattern.compile("^/ws/device/([^/]+)/([^/]+)/subscribe");
    Matcher m = p.matcher(uripath);

    try {
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);

      if (m.matches()) {
        deviceID = m.group(1);
        sensorName = m.group(2);

        String busname = Device.AGILE_INTERFACE;
        String path = "/" + Device.AGILE_INTERFACE.replace(".", "/")  + "/" + deviceID;
        Device device = connection.getRemoteObject(busname, path, Device.class);

        logger.info(device.Status().getStatus());
        if (device.Status().getStatus().equals(DeviceStatusType.CONNECTED.toString())) {
          device.Subscribe(sensorName);
          subscribed = true;

          sigHandler = new DBusSigHandler<Device.NewSubscribeValueSignal>() {
            @Override
            public void handle(NewSubscribeValueSignal sig) {
              if (sig.record.getDeviceID().equals(deviceID) && sig.record.getComponentID().equals(sensorName)) {
                logger.debug("http: New value {}%n", sig.record);
                try {
                 if (websocketOpen) 
                  session.getRemote().sendString(mapper.writeValueAsString(sig.record));
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            }
          };
          connection.addSigHandler(Device.NewSubscribeValueSignal.class, sigHandler);
        } else {
          logger.info("Device {} not connected", deviceID);
          sess.close(404, "Device not connected");
        }

      } else {

        sigHandler = new DBusSigHandler<Device.NewSubscribeValueSignal>() {
          @Override
          public void handle(NewSubscribeValueSignal sig) {
            try {
              if (websocketOpen)  
                  session.getRemote().sendString(mapper.writeValueAsString(sig.record));
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        };
        connection.addSigHandler(Device.NewSubscribeValueSignal.class, sigHandler);
      }
    } catch (org.freedesktop.DBus.Error.ServiceUnknown e) {
      sess.close(404, "Device not found");
    } catch (org.freedesktop.DBus.Error.UnknownObject e) {
      sess.close(404, "Device not found");
    } catch (DBusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    websocketOpen=false;
    try {
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);

      if (sigHandler != null) {
        connection.removeSigHandler(Device.NewSubscribeValueSignal.class, sigHandler);
        sigHandler = null;
      }

      if (deviceID != null) {
        logger.info("closing {}/{} reason:{}/{}", deviceID, sensorName, statusCode, reason);

        if (subscribed) {
          String busname = Device.AGILE_INTERFACE;
          String path = "/" + Device.AGILE_INTERFACE.replace(".", "/")  + "/" + deviceID;
          Device device = connection.getRemoteObject(busname, path, Device.class);
          device.Unsubscribe(sensorName);
          subscribed = false;
        }
      } else {
        logger.info("closing reason:{}/{}", deviceID, sensorName, statusCode, reason);
      }
    } catch (DBusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
