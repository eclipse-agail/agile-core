/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/
package iot.agile.devicemanager.device.factory;

import iot.agile.Device;
import iot.agile.devicemanager.device.DummyDevice;
import iot.agile.devicemanager.device.MedicalDevice;
import iot.agile.devicemanager.device.TISensorTag;
import iot.agile.devicemanager.device.HexiwearDevice;
import iot.agile.object.DeviceOverview;

/**
 * Agile device factory
 * 
 * @author dagi
 *
 */
public class DeviceFactory {
  /**
   * Get device based on device type
   * 
   * @param deviceType
   *          device type
   * @param deviceOverview
   *          device overview
   * @return
   * @throws Exception
   */
  public static Device getDevice(String deviceType, DeviceOverview deviceOverview) throws Exception {
    Device device = null;
    // TODO Enum based device type, not string
    switch (deviceType) {
    case "TI SensorTag":
      device = new TISensorTag(deviceOverview);
      break;
    case "Oximeter":
      device = new MedicalDevice(deviceOverview);
      break;
    case "HEXIWEAR":
      device = new HexiwearDevice(deviceOverview);
      break;
    case "Dummy":
      device = new DummyDevice(deviceOverview);
      break;
    default:
      throw new Exception("Device: " + deviceType + " does not have a matching type ");
    }
    return device;
  }
}
