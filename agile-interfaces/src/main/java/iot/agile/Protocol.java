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
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * Agile BLE Protocol interface
 *
 * @author dagi
 *
 */
public interface Protocol extends DBusInterface {

  public static String AGILE_INTERFACE = "iot.agile.protocol";
  
  /**
   * TODO: Return device status
   */
  @org.freedesktop.DBus.Description("enquiry the device status")
  public String Status();

  /**
   *
   * @return Driver unique code name example:
   */
  @org.freedesktop.DBus.Description("Returns the driver name")
  public String Driver();

  /**
   *
   * @return Driver name example bluetooth low energy
   */
  @org.freedesktop.DBus.Description("Returns the protocol name")
  public String Name();

  /**
   *
   * @return the last record received by the read or subscribe operation
   */
  @org.freedesktop.DBus.Description("Store the last record recived by read or subscribe")
  public String Data();

  /**
   * List of all devices discovered by the BLE protocol
   *
   * This list is updated by discover method
   *
   * TODO: Return list of devices TODO: The implementing class for this
   * interface should hold the list and return
   *
   * @see BLEDevice for sample device implementation
   */
  @org.freedesktop.DBus.Description("Returns list discovered protocol devices")
  public List<String> Devices();

  /**
   * Setup connection and initialize BLE connection for the given device
   *
   * TODO: Instead of deviceAddress this method should receive device profile,
   * and retrieve the id and other properties from it
   */
  @org.freedesktop.DBus.Description("Setup connection and initialize protocol connection for the given device")
  public boolean Connect(String deviceAddress) throws DBusException;

  /**
   *
   * Disconnect the BLE device
   *
   * TODO: Use device profile to disconnect the device
   *
   * @param deviceAddress
   */
  @org.freedesktop.DBus.Description("Safely disconnect the device from the protocol adapter")
  public boolean Disconnect(String deviceAddress);

  /**
   * List all discovered BLE devices
   *
   * TODO - return list of devices
   */
  @org.freedesktop.DBus.Description("Lists all discovered BLE devices")
  public void Discover() throws DBusException;

  public void StopDiscovery();

  /**
   * Send data over the protocol to the device
   *
   * TODO: Detail of this method should be discussed
   */
  public String Write(String deviceAddress, Map<String, String> profile) throws DBusException;

  /**
   * Read data over the Protocol, may be cached in the Data property depending
   * on implementation to save resources
   */
  public String Read(String deviceAddress, Map<String, String> profile) throws DBusException;

  /**
   * Subscribe to data update over the protocol
   *
   * @param subscribeParams
   */
  public void Subscribe(String... subscribeParams);

  public void DropBus();
}
