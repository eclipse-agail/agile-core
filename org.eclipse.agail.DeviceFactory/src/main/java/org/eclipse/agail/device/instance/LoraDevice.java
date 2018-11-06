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
package org.eclipse.agail.device.instance;

import java.util.HashMap;
import java.util.Map;

import java.nio.ByteBuffer;

import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.Device;
import org.eclipse.agail.Protocol;
import org.eclipse.agail.device.base.DeviceImp;
import org.eclipse.agail.exception.AgileNoResultException;
import org.eclipse.agail.object.DeviceComponent;
import org.eclipse.agail.object.DeviceOverview;
import org.eclipse.agail.object.DeviceStatusType;
import org.eclipse.agail.object.StatusType;
import java.util.List;

public class LoraDevice extends DeviceImp implements Device {
  protected Logger logger = LoggerFactory.getLogger(LoraDevice.class);

  public static final String deviceTypeName = "LoRa"; 

  /**
   * LoRa Protocol imp DBus interface id
   */
  private static final String LORA_PROTOCOL_ID = "org.eclipse.agail.protocol.LoRa";
  /**
   * LoRa Protocol imp DBus interface path
   */
  private static final String LORA_PROTOCOL_PATH = "/org/eclipse/agail/protocol/LoRa";
  
  private static final String TEMPERATURE = "Temperature";	
  private static final String HUMIDITY = "Relative_Humidity";
  private static final String LATITUDE = "Latitude";
  private static final String LONGITUDE = "Longitude";
  private static final String ALTITUDE = "Altitude";
  private static final String SNR = "SNR";
  private static final String RSSI = "RSSI";
	
  private DeviceStatusType deviceStatus = DeviceStatusType.DISCONNECTED;

  {
    subscribedComponents.put(TEMPERATURE, 0);		
    subscribedComponents.put(HUMIDITY, 0);
    subscribedComponents.put(LATITUDE, 0);
    subscribedComponents.put(LONGITUDE, 0);
    subscribedComponents.put(ALTITUDE, 0);
    subscribedComponents.put(SNR, 0);
    subscribedComponents.put(RSSI, 0);
  }

  {   
    profile.add(new DeviceComponent(TEMPERATURE, "Degree celsius (°C)"));		
    profile.add(new DeviceComponent(HUMIDITY, "Relative humidity (%RH)"));
    profile.add(new DeviceComponent(LATITUDE, "Decimal degrees (º)"));
    profile.add(new DeviceComponent(LONGITUDE, "Decimal degrees (º)"));
    profile.add(new DeviceComponent(ALTITUDE, "Meters (m)"));    
    profile.add(new DeviceComponent(SNR, "Decibel (db)"));
    profile.add(new DeviceComponent(RSSI, "Decibel-milliwatt (dBm)"));
  }

  public LoraDevice(DeviceOverview deviceOverview) throws DBusException {
    super(deviceOverview);
    this.protocol = LORA_PROTOCOL_ID;
    String devicePath = AGILE_DEVICE_BASE_BUS_PATH + "lora" + deviceOverview.id; 
    dbusConnect(deviceAgileID, devicePath, this);
    deviceProtocol = (Protocol) connection.getRemoteObject(LORA_PROTOCOL_ID, LORA_PROTOCOL_PATH, Protocol.class);
    logger.debug("Exposed device {} {}", deviceAgileID, devicePath);
  }

  public static boolean Matches(DeviceOverview d) {    
    return d.name.contains(deviceTypeName);
  }

  @Override
  protected String DeviceRead(String componentName) {
    if ((protocol.equals(LORA_PROTOCOL_ID)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {            
            HashMap<String, String> aux = new HashMap<String, String>();
            aux.put("id", componentName);            
            byte[] readData = deviceProtocol.Read(address, aux);
            return formatReading(componentName, readData);
          } catch (DBusException e) {
            e.printStackTrace();
          }
        } else {
          throw new AgileNoResultException("Component not supported:" + componentName);
        }
      } else {
        throw new AgileNoResultException("Device not connected: " + deviceName);
      }
    } else {
      throw new AgileNoResultException("Protocol not supported: " + protocol);
    }
    throw new AgileNoResultException("Unable to read " + componentName);
  }

  @Override
  public void Subscribe(String componentName) {
    if ((protocol.equals(LORA_PROTOCOL_ID)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
            if (!hasOtherActiveSubscription()) {
              addNewRecordSignalHandler();
            }
            if (!hasOtherActiveSubscription(componentName)) {
              HashMap<String, String> aux = new HashMap<String, String>();
              aux.put("id", componentName);
              deviceProtocol.Subscribe(address, aux);              
            }
            subscribedComponents.put(componentName, subscribedComponents.get(componentName) + 1);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          throw new AgileNoResultException("Component not supported:" + componentName);
        }
      } else {
        throw new AgileNoResultException("Device not connected: " + deviceName);
      }
    } else {
      throw new AgileNoResultException("Protocol not supported: " + protocol);
    }
    }

  @Override
  public synchronized void Unsubscribe(String componentName) throws DBusException {
    if ((protocol.equals(LORA_PROTOCOL_ID)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
            subscribedComponents.put(componentName, subscribedComponents.get(componentName) - 1);
            if (!hasOtherActiveSubscription(componentName)) {
              HashMap<String, String> aux = new HashMap<String, String>();
              aux.put("id", componentName);
              deviceProtocol.Unsubscribe(address, aux);              
             }
            if (!hasOtherActiveSubscription()) {
              removeNewRecordSignalHandler();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          throw new AgileNoResultException("Component not supported:" + componentName);
        }
      } else {
        throw new AgileNoResultException("Device not connected: " + deviceName);
      }
    } else {
      throw new AgileNoResultException("Protocol not supported: " + protocol);
    }
   }

  @Override
  public void Connect() throws DBusException {
    deviceStatus = DeviceStatusType.CONNECTED;
    logger.info("Device connected {}", deviceID);
  }

  @Override
  public void Disconnect() throws DBusException {
    deviceStatus = DeviceStatusType.DISCONNECTED;
    logger.info("Device disconnected {}", deviceID);
  }

  @Override
  public StatusType Status() {
    return new StatusType(deviceStatus.toString());
  }
  
//  @Override
//  public void Execute(String command, Map<String, Variant> args) {
//    if(command.equalsIgnoreCase(DeviceStatusType.ON.toString())){
//      deviceStatus = DeviceStatusType.ON;
//    }else if(command.equalsIgnoreCase(DeviceStatusType.OFF.toString())){
//      deviceStatus = DeviceStatusType.OFF;
//    }
//  }
//  
  protected boolean isConnected() {
    if (Status().getStatus().equals(DeviceStatusType.CONNECTED.toString()) || Status().getStatus().equals(DeviceStatusType.ON.toString())) {
      return true;
    }
    return false;
  }
  
  @Override
  protected boolean isSensorSupported(String sensorName) {
    return subscribedComponents.containsKey(sensorName);
  }
  
  @Override
  protected String formatReading(String sensorName, byte[] readData) { 
    return Float.toString(ByteBuffer.wrap(readData).getFloat());
  }
  
  @Override
  protected String getComponentName(Map<String, String> profile) {    
    return profile.get("id");

  }
  
  @Override
  public void Write(String componentName, String payload) {	
    logger.debug("Write function for LoRA not implemented yet");
	}

  @Override
  public void Execute(String command) {
    logger.debug("Device. Execute not implemented");
	}

  @Override
  public List<String> Commands(){
    logger.debug("Device. Commands not implemented");
    return null;
      }
}
