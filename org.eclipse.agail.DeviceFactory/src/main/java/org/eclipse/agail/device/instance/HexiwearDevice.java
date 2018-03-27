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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.exceptions.DBusException;
import org.eclipse.agail.Device;
import org.eclipse.agail.Protocol;
import org.eclipse.agail.Protocol.NewRecordSignal;
import org.eclipse.agail.device.base.AgileBLEDevice;
import org.eclipse.agail.device.base.SensorUuid;
import org.eclipse.agail.object.DeviceDefinition;
import org.eclipse.agail.object.DeviceOverview;
import org.eclipse.agail.object.RecordObject;
import org.eclipse.agail.object.DeviceComponent;
import org.eclipse.agail.exception.AgileNoResultException;

public abstract class HexiwearDevice extends AgileBLEDevice implements Device {
  protected Logger logger = LoggerFactory.getLogger(HexiwearDevice.class);
  protected static final Map<String, SensorUuid> sensors = new HashMap<String, SensorUuid>();
  private static final String Acc = "Accelerometer";
  private static final String Gyro = "Gyroscope";
  private static final String Magnetometer = "Magnetometer";

  private static final String AmbientLight = "Ambient Light";
  private static final String Temperature = "Temperature";
  private static final String Humidity = "Humidity";
  private static final String Pressure = "Atmospheric Pressure";

  private static final String Heartrate = "Heart Rate";
  private static final String Steps = "Steps";
  private static final String Calories = "Calories";


	{
		subscribedComponents.put(Acc, 0);
		subscribedComponents.put(Gyro, 0);
		subscribedComponents.put(Magnetometer, 0);

		subscribedComponents.put(AmbientLight, 0);
		subscribedComponents.put(Temperature, 0);
		subscribedComponents.put(Humidity, 0);
		subscribedComponents.put(Pressure, 0);

		subscribedComponents.put(Heartrate, 0);
		subscribedComponents.put(Steps, 0);
		subscribedComponents.put(Calories, 0);
	}

 	{
		profile.add(new DeviceComponent(Acc, ""));
		profile.add(new DeviceComponent(Gyro, ""));
		profile.add(new DeviceComponent(Magnetometer, ""));

		profile.add(new DeviceComponent(AmbientLight, ""));
		profile.add(new DeviceComponent(Temperature, ""));
		profile.add(new DeviceComponent(Humidity, ""));
		profile.add(new DeviceComponent(Pressure, ""));

		profile.add(new DeviceComponent(Heartrate, ""));
		profile.add(new DeviceComponent(Steps, ""));
		profile.add(new DeviceComponent(Calories, ""));
	}
 

 	static {
		sensors.put(Acc, new SensorUuid("00002000-0000-1000-8000-00805f9b34fb", "00002001-0000-1000-8000-00805f9b34fb", "", ""));
		sensors.put(Gyro, new SensorUuid("00002000-0000-1000-8000-00805f9b34fb", "00002002-0000-1000-8000-00805f9b34fb", "", ""));
		sensors.put(Magnetometer, new SensorUuid("00002000-0000-1000-8000-00805f9b34fb", "00002003-0000-1000-8000-00805f9b34fb", "", ""));

		sensors.put(AmbientLight, new SensorUuid("00002010-0000-1000-8000-00805f9b34fb", "00002011-0000-1000-8000-00805f9b34fb", "", ""));
		sensors.put(Temperature, new SensorUuid("00002010-0000-1000-8000-00805f9b34fb", "00002012-0000-1000-8000-00805f9b34fb", "", ""));
		sensors.put(Humidity, new SensorUuid("00002010-0000-1000-8000-00805f9b34fb", "00002013-0000-1000-8000-00805f9b34fb", "", ""));
		sensors.put(Pressure, new SensorUuid("00002010-0000-1000-8000-00805f9b34fb", "00002014-0000-1000-8000-00805f9b34fb", "", ""));

		sensors.put(Heartrate, new SensorUuid("00002020-0000-1000-8000-00805f9b34fb", "00002021-0000-1000-8000-00805f9b34fb", "", ""));
		sensors.put(Steps, new SensorUuid("00002020-0000-1000-8000-00805f9b34fb", "00002022-0000-1000-8000-00805f9b34fb", "", ""));
		sensors.put(Calories, new SensorUuid("00002020-0000-1000-8000-00805f9b34fb", "00002023-0000-1000-8000-00805f9b34fb", "", ""));
	}

	public static boolean Matches(DeviceOverview d) {
		return d.name.contains("HEXIWEAR");
	}

	public static String deviceTypeName = "HEXIWEAR";

	public HexiwearDevice(DeviceOverview deviceOverview) throws DBusException {
		super(deviceOverview);
	}


	public HexiwearDevice(DeviceDefinition devicedefinition) throws DBusException {
		super(devicedefinition);
	}

	@Override
	public void Connect() throws DBusException {
		super.Connect();
		for (String componentName : subscribedComponents.keySet()) {
			logger.info("Hexiwear Connect: " + componentName);
			//DeviceRead(componentName);
			if (subscribedComponents.get(componentName) > 0) {
				logger.info("Resubscribing to {}", componentName);
				deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
			}
		}
	}


  @Override
  protected String DeviceRead(String componentName) {
	logger.info("Hexiwear DeviceRead: "+ componentName);
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
            byte[] result = deviceProtocol.Read(address, getReadValueProfile(componentName));
            return formatReading(componentName, result);
          } catch (DBusException e) {
            e.printStackTrace();
          }
        } else {
          throw new AgileNoResultException("Sensor not supported:" + componentName);
        }
      } else {
        throw new AgileNoResultException("BLE Device not connected: " + deviceName);
      }
    } else {
      throw new AgileNoResultException("Protocol not supported: " + protocol);
    }
    throw new AgileNoResultException("Unable to read "+componentName);
  }

	
 @Override
  public synchronized void Subscribe(String componentName) {
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
            if (!hasOtherActiveSubscription(componentName)) {
              deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
              addNewRecordSignalHandler();
            }
	    logger.info("Hexiwear Subscribe");
            subscribedComponents.put(componentName, subscribedComponents.get(componentName) + 1);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          throw new AgileNoResultException("Sensor not supported:" + componentName);
        }
      } else {
        throw new AgileNoResultException("BLE Device not connected: " + deviceName);
      }
    } else {
      throw new AgileNoResultException("Protocol not supported: " + protocol);
    }
  }

 @Override
  public synchronized void Unsubscribe(String componentName) throws DBusException {
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
            subscribedComponents.put(componentName, subscribedComponents.get(componentName) - 1);
            if (!hasOtherActiveSubscription(componentName)) {
              deviceProtocol.Unsubscribe(address, getReadValueProfile(componentName));
              removeNewRecordSignalHandler();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          throw new AgileNoResultException("Sensor not supported:" + componentName);
        }
      } else {
        throw new AgileNoResultException("BLE Device not connected: " + deviceName);
      }
    } else {
      throw new AgileNoResultException("Protocol not supported: " + protocol);
    }
  }
  
@Override
  public void Write(String componentName, String payload) {
            logger.debug("Device. Write not implemented");
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

	// =======================Utility methods===========================

	private Map<String, String> getReadValueProfile(String sensorName) {
		Map<String, String> profile = new HashMap<String, String>();
		SensorUuid s = sensors.get(sensorName);
		if (s != null) {
			profile.put(GATT_SERVICE, s.serviceUuid);
			profile.put(GATT_CHARACTERSTICS, s.charValueUuid);
			logger.info("Hexiwear Gatt Service: "+s.serviceUuid);
			logger.info("Hexiwear Gatt Characteristic: "+s.charValueUuid);
		}
		return profile;
	}

	@Override
	protected boolean isSensorSupported(String sensorName) {
		return sensors.containsKey(sensorName);
	}



	/**
	 * Checks if there is another active subscription on the given component of
	 * the device
	 * 
	 * @param componentName
	 * @return
	 */
	@Override
	protected boolean hasOtherActiveSubscription(String componentName) {
		for (String component : subscribedComponents.keySet()) {
			if (subscribedComponents.get(component) > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String formatReading(String componentName, byte[] readData) {
			int resultX = 0;
			int resultY = 0;
			int resultZ = 0;
			String value = "";
		switch (componentName) {
   		   case Acc:
			resultX =   (readData[1] << 8) | readData[0];
			resultY =   (readData[3] << 8) | readData[2];
			resultZ =   (readData[5] << 8) | readData[4];
			value = "["+Integer.toString(resultX)+","+Integer.toString(resultY)+","+Integer.toString(resultZ)+"]";
			return value;
		   case Gyro:
			resultX =   (readData[1] << 8) | readData[0];
			resultY =   (readData[3] << 8) | readData[2];
			resultZ =   (readData[5] << 8) | readData[4];
			value = "["+Integer.toString(resultX)+","+Integer.toString(resultY)+","+Integer.toString(resultZ)+"]";
			return value;
		   case Magnetometer:
			resultX =  (readData[1] << 8) | readData[0];
			resultY =  (readData[3] << 8) | readData[2];
			resultZ =  (readData[5] << 8) | readData[4];
			value = "["+Integer.toString(resultX)+","+Integer.toString(resultY)+","+Integer.toString(resultZ)+"]";
			return value;
		   case AmbientLight:
			value = Integer.toString(readData[0]);
			return value;
		   case Temperature:
		   case Humidity:
		   case Pressure:	
			value = Integer.toString((readData[1] << 8 | readData[0]) /100);
			return value;
		   case Heartrate:
		   case Steps: 
		   case Calories:
			value = Integer.toString(readData[0]);
			return value;	
		}
		return "0";
	}

	/**

	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void addNewRecordSignalHandler() {
		logger.info("Hexiwear addNewRecordSignalHandler");	
		try {
			if (newRecordSigHanlder == null && connection != null) {
 				newRecordSigHanlder = new DBusSigHandler<Protocol.NewRecordSignal>() {
					@Override
					public void handle(NewRecordSignal sig) {
						if (sig.address.equals(address)) {
 							for(String componentName : getComponentNames(sig.profile)){
 								String readVal = formatReading(componentName, sig.record);
 								if (Float.parseFloat(readVal) != 0.0) {
 									RecordObject recObj = new RecordObject(deviceID, componentName,
 											formatReading(componentName, sig.record), getMeasurementUnit(componentName), "",
 											System.currentTimeMillis());
 									data = recObj;
 									logger.info("Device notification component {} value {}", componentName, recObj.value);
 									lastReadStore.put(componentName, recObj);
 									try {
 										Device.NewSubscribeValueSignal newRecordSignal = new Device.NewSubscribeValueSignal(
 												AGILE_NEW_RECORD_SUBSCRIBE_SIGNAL_PATH, recObj);
 										connection.sendSignal(newRecordSignal);
 									} catch (DBusException e) {
 										e.printStackTrace();
 									}
 								}
 							}
						}
					}
				};
				connection.addSigHandler(Protocol.NewRecordSignal.class, newRecordSigHanlder);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Given the profile of the component returns the list of component names
	 * 
	 * @param uuid
	 * @return
	 */
 	protected List<String> getComponentNames(Map<String, String> profile) {
		List<String> ret = new ArrayList<String>();
		String serviceUUID = profile.get(GATT_SERVICE);
		String charValueUuid = profile.get(GATT_CHARACTERSTICS);
		for (Entry<String, SensorUuid> su : sensors.entrySet()) {
			if (su.getValue().serviceUuid.equals(serviceUUID) && su.getValue().charValueUuid.equals(charValueUuid)) {
				ret.add(su.getKey());
			}
		}
		return ret;
	}
 	
 	@Override
	protected String getMeasurementUnit(String sensor) {
 		return "";
 	}

  /**
   * Given the profile of the component returns the name of the sensor
   * 
   * @param uuid
   * @return
   */
  @Override
  protected String getComponentName(Map<String, String> profile) {
    String serviceUUID = profile.get(GATT_SERVICE);
    String charValueUuid = profile.get(GATT_CHARACTERSTICS);
    for (Entry<String, SensorUuid> su : sensors.entrySet()) {
      if (su.getValue().serviceUuid.equals(serviceUUID) && su.getValue().charValueUuid.equals(charValueUuid)) {
        return su.getKey();
      }
    }
    return null;
  }
}
