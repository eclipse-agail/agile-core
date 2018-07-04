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
import org.eclipse.agail.object.DeviceDefinition;
import org.eclipse.agail.object.DeviceOverview;
import org.eclipse.agail.object.RecordObject;
import org.eclipse.agail.object.DeviceComponent;
import org.eclipse.agail.exception.AgileNoResultException;
import org.eclipse.agail.device.base.AgileBLEDevice;
import org.eclipse.agail.device.base.SensorUuid;

public class WeightDevice extends AgileBLEDevice implements Device {
  protected Logger logger = LoggerFactory.getLogger(WeightDevice.class);
  protected static final Map<String, SensorUuid> sensors = new HashMap<String, SensorUuid>();
	private static final String Weight = "Weight";
  private static final byte[] INIT_COMM = { (byte) 0xf6,0x01 };
  private float lastValue = (float) 0.0;

	{
		subscribedComponents.put(Weight, 1);
	}

 	{
		profile.add(new DeviceComponent(Weight, ""));

	}


 	static {
		sensors.put(Weight, new SensorUuid("0000ffe0-0000-1000-8000-00805f9b34fb", "0000ffe1-0000-1000-8000-00805f9b34fb", "0000ffe1-0000-1000-8000-00805f9b34fb", ""));
	}

	public static boolean Matches(DeviceOverview d) {
		return d.name.contains("BF800") ;
	}

	public static String deviceTypeName = "Weight Scales";

	public WeightDevice(DeviceOverview deviceOverview) throws DBusException {
		super(deviceOverview);
	}


	public WeightDevice(DeviceDefinition devicedefinition) throws DBusException {
		super(devicedefinition);
	}

	@Override
	public void Connect() throws DBusException {
		super.Connect();
    	//deviceProtocol.Write(address, getEnableSensorProfile(Weight), INIT_COMM);
		
		for (String componentName : subscribedComponents.keySet()) {
			if (subscribedComponents.get(componentName) > 0) {
				logger.info("Resubscribing to {}", componentName);
				//deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
                Unsubscribe(componentName);
                Subscribe(componentName);
			}
		}
		

	}


  @Override
  protected String DeviceRead(String componentName) {
    return Float.toString(lastValue);
    /*
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
            byte[] result = deviceProtocol.NotificationRead(address, getReadValueProfile(componentName));
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
    */
  }

 @Override
  public synchronized void Subscribe(String componentName) {
    logger.info("Subscribing to "+ componentName);
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
          logger.info("subscribe con "+ isConnected() );
        if (isSensorSupported(componentName.trim())) {
          try {
            logger.info("subscribe sensor supported");
            if (!hasOtherActiveSubscription(componentName)) {
              logger.info("subscribing");
              deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
              logger.info("adding signal handler");
              addNewRecordSignalHandler();
            }
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
    logger.info("Unsubscribing from "+ componentName);
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
            subscribedComponents.put(componentName, subscribedComponents.get(componentName) - 1);
            if (hasOtherActiveSubscription(componentName)) {
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
    try {
        logger.info("weight execute command");
   	    deviceProtocol.Write(address, getEnableSensorProfile(Weight), INIT_COMM);
    } catch (DBusException e) {
        logger.info (e.toString());
    }

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
      logger.info("subscribed componenets: " + component.toString() );
			if (subscribedComponents.get(component) > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	/*
	Reading the value is a two step process	
	*/
	protected String formatReading(String componentName, byte[] readData) {
		float result = 0;
		logger.info("formatReading: "+ byteArrayToHex(readData) + " length: " +readData.length  );
    if ((readData.length == 5)){ // && (readData[0] == 0xf7) && (readData[1] == 0x58)){
       float resultTemp =(float) (((readData[3] << 8) | (readData[4])) * 0.05);
		logger.info("temp result " + resultTemp);		
    } else if (readData.length == 13) {
      result = 0;
      final byte[] REQ_WEIGHT = { (byte) 0xf7, (byte) 0xf1,0x59,0x03,0x01};
      try {
        logger.info("getting final weight value");
        deviceProtocol.Write(address, getEnableSensorProfile(Weight), REQ_WEIGHT);
      }  catch (Exception e) {
        e.printStackTrace();
      }
    } else if (readData.length == 15) {
      result =(float) ((((readData[8] & 0xff) << 8) | (readData[9] & 0xff)) * 0.05);
    }
    return Float.toString(result);
	}

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void addNewRecordSignalHandler() {
    logger.info("addNewRecordHandler");
		try {
			if (newRecordSigHanlder == null && connection != null) {
 				newRecordSigHanlder = new DBusSigHandler<Protocol.NewRecordSignal>() {
					@Override
					public void handle(NewRecordSignal sig) {
						if (sig.address.equals(address)) {
 							for(String componentName : getComponentNames(sig.profile)){
 								String readVal = formatReading(componentName, sig.record);
                				logger.info("New record: " +  readVal);
 								if (Float.parseFloat(readVal) != 0.0) {
                  					lastValue = Float.parseFloat(readVal);
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
	private Map<String, String> getEnableSensorProfile(String sensorName) {
		Map<String, String> profile = new HashMap<String, String>();
		SensorUuid s = sensors.get(sensorName);
		if (s != null) {
			profile.put(GATT_SERVICE, s.serviceUuid);
			profile.put(GATT_CHARACTERSTICS, s.charConfigUuid);
		}
		return profile;
	}

	private String byteArrayToHex(byte[] a) {
	   StringBuilder sb = new StringBuilder(a.length * 2);
	   for(byte b: a)
		  sb.append(String.format("%02x", b));
	   return sb.toString();
	}
}
