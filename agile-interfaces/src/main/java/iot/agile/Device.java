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

import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

import iot.agile.object.DeviceDefinition;
import iot.agile.object.DeviceComponent;
import iot.agile.object.RecordObject;

/**
 * @author dagi
 *
 * Agile Device Interface
 *
 */
public interface Device extends DBusInterface {
  
  static String AGILE_INTERFACE = "iot.agile.Device";
  
  /**
   *
   * @return The unique device id on the gateway
   */
  @org.freedesktop.DBus.Description("Unique device ID in the gateway")
  public String Id();

  /**
   *
   * @return The device name
   */
  @org.freedesktop.DBus.Description("Name of the device")
  public String Name();

  /**
   *
   * @return Current Device status
   */
  @org.freedesktop.DBus.Description("Current Device status")
  public String Status();

  /**
   *
   * @return return User configuration storage (in terms of KeyValue)
   */
  @org.freedesktop.DBus.Description("returns User configuration storage (in terms of KeyValue)")
  public String Configuration();

  /**
   *
   * @return Profile is protocol specific information of the device
   *
   */
  @org.freedesktop.DBus.Description("returns the profile of the device")
  public List<DeviceComponent> Profile();

  /**
   *
   * @return Attributes of an object in as a DeviceDefinition
   *
   */
  @org.freedesktop.DBus.Description("returns the definition of the device")
  public DeviceDefinition Definition();

  /**
   *
   * @return A RecordObject with the last data update received from the component
   */
  @org.freedesktop.DBus.Description("returns the last data update received by the device")
  public RecordObject LastUpdate(String componentID);

  /**
   *
   * @return A list of RecordObjects with the last read value of all components
   */
  @org.freedesktop.DBus.Description("returns the last data updates received by the device")
  public List<RecordObject> LastUpdate();

  /**
   *
   * @return the most recent update of a sensor or data stream Received
   * asynchronously from subscribe all
   */
  @org.freedesktop.DBus.Description("returns the most recent update of a sensor")
  public RecordObject Data();

  /**
   *
   * @return Device specific communication protocol instance Available to access
   * protocol specific methods and properties
   */
  @org.freedesktop.DBus.Description("returns Device specific communication  protocol instance")
  public String Protocol();

  // Methods
  /**
   * Setup connection and initialize BLE connection for the given device
   *
   * TODO: Instead of deviceAddress this method should receive device profile,
   * and retrieve the id and other properties from it
   */
  @org.freedesktop.DBus.Description("Setup connection and initialize BLE connection for the given device")
  public void Connect()throws DBusException;

  /**
   *
   * Disconnect the BLE device
   *
   * TODO: Use device profile to disconnect the device
   *
   * @param deviceAddress
   */
  @org.freedesktop.DBus.Description("Safely disconnect the device from the BLE adapter")
  public void Disconnect() throws DBusException ;

  /**
   * Execute an operation on the device
   */
  @org.freedesktop.DBus.Description("Execute an operation on the device")
  public void Execute(String command, Map<String, Variant> args) throws DBusException;

  /**
   * Read data from all components
   */
  @org.freedesktop.DBus.Description("Read data from all components")
  public List<RecordObject> Read();

  /**
   * Read data from the device
   */
  @org.freedesktop.DBus.Description("Read data from the device")
  public RecordObject Read(String sensorName);

  /**
   * Write data on the device
   */
  @org.freedesktop.DBus.Description("Write data on the device")
  public void Write() throws DBusException;

  /**
   * Enable subscription
   */
  @org.freedesktop.DBus.Description("Enable subscription")
  public void Subscribe(String component) throws DBusException;
  
  /**
   * disable subscription
   */
  @org.freedesktop.DBus.Description("Enable subscription")
  public void Unsubscribe(String component) throws DBusException;
  
	/**
	 * New data reading signal for subscribe methods
	 * 
	 * @author dagi
	 *
	 */
	public class NewSubscribeValueSignal extends DBusSignal {
		public final RecordObject record;

		public NewSubscribeValueSignal(String path, RecordObject record) throws DBusException {
			super(path, record);
			this.record = record;
		}

	}
}
