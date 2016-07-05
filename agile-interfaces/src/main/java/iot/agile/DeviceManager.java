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
package iot.agile;

import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * AGILE Device Manager Interface
 * 
 * @author dagi
 * 
 * 
 *
 */
public interface DeviceManager extends DBusInterface {

	/**
	 * Return all registered devices
	 * 
	 * @return
	 */
	@org.freedesktop.DBus.Description("Returns all registered devices")
	public Map<String, String> devices();

	/**
	 * search for devices based on specific criteria
	 * 
	 * @return
	 */
	@org.freedesktop.DBus.Description("Returns all registered devices")
	public String Find();

	/**
	 * Creates devices
	 */
	@org.freedesktop.DBus.Description("Returns all registered devices")
	public String Create(String deviceID, String deviceName, String protocol) throws DBusException;

	/**
	 * Load a device definition by its ID
	 * 
	 * @param id
	 */
	@org.freedesktop.DBus.Description("Load a device definition by its ID")
	public void Read(String id);

	/**
	 * UPdate a device definition by its ID
	 * 
	 * @param id
	 * @param definition
	 * @return
	 */
	@org.freedesktop.DBus.Description("UPdate a device definition by its ID")
	public boolean Update(String id, String definition);

	/**
	 * Delete a device definition by its ID. this will deactivate the Device
	 * DBus object
	 * 
	 * @param id
	 * @param definition
	 */
	@org.freedesktop.DBus.Description("Delete a device definition by its ID")
	public void Delete(String id, String definition);

	/**
	 * Perform a batch operation over a set of devices
	 * 
	 * @param operation
	 * @param arguments
	 * @return
	 */
	@org.freedesktop.DBus.Description("Perform a batch operation")
	public boolean Batch(String operation, String arguments);

}
