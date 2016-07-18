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
package iot.agile.devicemanager.examples;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iot.agile.Device;

/**
 * @author dagi
 * 
 *         This program demonstrates how client programs read data from a BLE
 *         Device through DBus, this example specifically shows how to read
 *         temperature value from TI Sensor Tag
 * 
 *         NOTE: A device should be discovered, registered and connected before
 *         reading data
 * 
 *
 */
public class ReadData {
  	protected final static Logger logger = LoggerFactory.getLogger(ReadData.class);

	/**
	 * Bus name for AGILE BLE Device interface
	 */
	private static String agileDeviceObjName = "iot.agile.Device";

	/**
	 * Bus path for AGILE BLE Device interface
	 */
	private static String agileDeviceObjectPath = "/iot/agile/device/ble/";
	/**
	 * Sensor name
	 */
	private static String service = "Temperature";
	
	private static String address ="C4:BE:84:70:69:09";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	  checkInput(args);

	  String devicePath = agileDeviceObjectPath + address.replace(":", "");
	  logger.info("Reading {}", service);
	
	  while(true){
	    try {
	      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
	      Device sensorTag = (Device) connection.getRemoteObject(agileDeviceObjName, devicePath, Device.class);
	      String currentTemp = sensorTag.Read(service);
	      logger.info("Temperature: {}", currentTemp);
	    } catch (DBusException e) {
	      e.printStackTrace();
	    }  
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
