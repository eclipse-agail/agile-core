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

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;

import iot.agile.object.DeviceOverview;

/**
 * @author dagi AGILE Protocol Manager Interface
 */
public interface ProtocolManager extends DBusInterface {

  public static String AGILE_INTERFACE = "iot.agile.ProtocolManager";

  /**
   *
   * @return List of devices returned by each protocol discovery method
   */
  @org.freedesktop.DBus.Description("Returns List of devices returned by each protocol discovery method")
  public List<DeviceOverview> Devices();

  /**
   *
   * @return List of supported/Managed protocols
   */
  @org.freedesktop.DBus.Description("Returns List of supported/Managed protocols")
  public List<String> Protocols();

  /**
   * Starts device discovery on all managed protocols
   */
  @org.freedesktop.DBus.Description("Starts device discovery on all managed protocols")
  public void Discover();

  /**
   * Starts device discovery on all managed protocols (new name)
   */
  @org.freedesktop.DBus.Description("Starts device discovery on all managed protocols")
  public void StartDiscovery();

  /**
   * Stops device discovery on all managed protocols
   */
  @org.freedesktop.DBus.Description("Stops device discovery on all managed protocols")
  public void StopDiscovery();

  /**
   * Adds a new protocol to the managed protocols list
   *
   * @param protocol
   *          the protocol to be added
   */
  @org.freedesktop.DBus.Description("Adds a new protocol to the managed protocols list")
  public void Add(String protocol);

  /**
   * Removes a protocol from the managed protocol list
   *
   * @param protocol
   *          the protocol to be removed
   */
  @org.freedesktop.DBus.Description("Removes a protocol from the managed protocol list")
  public void Remove(String protocol);

  /**
   * Found new device signal
   * @author dagi
   *
   */
  public class FoundNewDeviceSignal extends DBusSignal {
    public final String path;
    public final DeviceOverview device;

    public FoundNewDeviceSignal(String path, DeviceOverview device) throws DBusException {
      super(path, device);
      this.path = path;
      this.device = device;
    }

  }

}
