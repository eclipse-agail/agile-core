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

import iot.agile.Device;

/**
 * @author dagi
 * 
 * This program demonstrates how client programs read data from a BLE Device through DBus, this example specifically shows how to read temperature value
 *  from TI Sensor Tag 
 * 
 * NOTE: A device should be discovered, registered and connected before reading data
 *    
 *
 */
public class ReadData {
	
	/**
	 * Bus name for AGILE BLE Device interface
	 */
	private static final String TI_SENSORTAG_AGILE_ID = "iot.agile.device.TISensorTag";

	/**
	 * Bus path for AGILE BLE Device interface
	 */
	private static final String TI_SENSORTAG_AGILE_BUS_PATH = "/iot/agile/Device/TISensorTag";
	/**
	 * Sensor name
	 */
	private static final String TEMPERATURE = "Temperature";
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);
			Device sensorTag = (Device) connection.getRemoteObject(TI_SENSORTAG_AGILE_ID, TI_SENSORTAG_AGILE_BUS_PATH, Device.class);
 			String currentTemp = sensorTag.Read(TEMPERATURE);
			
			 System.out.println("Temperature : "+currentTemp);
		} catch (DBusException e) {
			e.printStackTrace();
		}
		
		
	}

}
