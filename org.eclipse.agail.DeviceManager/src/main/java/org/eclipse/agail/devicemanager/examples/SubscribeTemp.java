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
package org.eclipse.agail.devicemanager.examples;

import org.freedesktop.dbus.DBusConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.Device;

public class SubscribeTemp {
  	protected final static Logger logger = LoggerFactory.getLogger(SubscribeTemp.class);

	/**
	 * Bus name for AGILE BLE Device interface
	 */
	private static String agileDeviceObjName = "org.eclipse.agail.Device";

	/**
	 * Bus path for AGILE BLE Device interface
	 */
	private static String agileDeviceObjectPath = "/org/eclipse/agail/Device/ble_";
	/**
	 * Sensor name
	 */
	private static String service = "Temperature";
	
	private static String address ="C4:BE:84:70:69:09";

  	
	public static void main(String[] args) {
		  checkInput(args);
		  String devicePath = agileDeviceObjectPath + address.replace(":", "");
		  logger.info("subscring to {}", service);
		  try{
		      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
		      Device sensorTag = (Device) connection.getRemoteObject(agileDeviceObjName, devicePath, Device.class);
		      sensorTag.Subscribe(service);
		  }catch(Exception ex){
			  ex.printStackTrace();
		  }
	}
	 private static void checkInput(String[] input){
		   if(input.length ==2){
		     service = input[0];
		     if(isValidMACAddress(input[1])){
		       address = input[1];
		     }else{
		       logger.error("invalid device address, using default sensor tag address: {}",address);
		     }
		   }else{
		     logger.error("Invalid input reading from default service {}", service);
		      
		   }
		 }
		 
		 /**
		  * 
		  * @param address
		  * @return
		  */
		 private static boolean isValidMACAddress(String address) {
		   if (address.trim().toCharArray().length == 17) {
		     if (address.split(":").length == 6) {
		       return true;
		     }
		   }
		   return false;
		 }
}
