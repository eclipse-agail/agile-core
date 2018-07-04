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
import java.util.Calendar;

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

public class BeuerBPM85 extends AgileBLEDevice implements Device {
  protected Logger logger = LoggerFactory.getLogger(BeuerBPM85.class);
  protected static final Map<String, SensorUuid> sensors = new HashMap<String, SensorUuid>();
	private static final String systolic = "Systolic";
	private static final String diastolic = "Diastolic";
	private static final String pulse = "Pulse";

    private static int lastSystolicValue = 0 ;
    private static int lastDiastolicValue = 0;
	private static int lastPulseValue =0 ;
	{
		subscribedComponents.put(systolic, 0);
		subscribedComponents.put(diastolic, 0);
		subscribedComponents.put(pulse,0);
	}

 	{
		profile.add(new DeviceComponent(systolic, ""));
		profile.add(new DeviceComponent(diastolic, ""));
		profile.add(new DeviceComponent(pulse, ""));
	}


 	static {
		sensors.put(systolic,
				new SensorUuid("00001810-0000-1000-8000-00805f9b34fb", "00002a35-0000-1000-8000-00805f9b34fb", "", ""));
		sensors.put(diastolic,
				new SensorUuid("00001810-0000-1000-8000-00805f9b34fb", "00002a35-0000-1000-8000-00805f9b34fb","", ""));
		sensors.put(pulse,
				new SensorUuid("00001810-0000-1000-8000-00805f9b34fb", "00002a35-0000-1000-8000-00805f9b34fb","", ""));
	}

	public static boolean Matches(DeviceOverview d) {
		return d.name.contains("Beurer BM85");
	}

	public static String deviceTypeName = "Beurer BM85";

	public BeuerBPM85(DeviceOverview deviceOverview) throws DBusException {
		super(deviceOverview);
	}


	public BeuerBPM85(DeviceDefinition devicedefinition) throws DBusException {
		super(devicedefinition);
	}

	@Override
	public void Connect() throws DBusException {
		super.Connect();
		logger.info("Beuer BPM Connect()");
		for (String componentName : subscribedComponents.keySet()) {
			//logger.info("Clear existing subscribed components" + componentName);
			//Unsubscribe(componentName);     		
			//logger.info("Beuer BPM subscribe() :"+componentName);
			//Subscribe(componentName);
  
			//if (subscribedComponents.get(componentName) > 0) {
			//	logger.info("Resubscribing to {}", componentName);
			//	deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
			//}
		}
	}


  @Override
  protected String DeviceRead(String componentName) {
	logger.info("Beuer BPM Device Read " + componentName);
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
    //  if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
         switch (componentName) {
      			case systolic:
                	return Integer.toString(lastSystolicValue);
      			case diastolic:
                	return Integer.toString(lastDiastolicValue);
				case pulse:
					return Integer.toString(lastPulseValue);
      			default:
              return "";  
		  }          
        } else {
          throw new AgileNoResultException("Sensor not supported:" + componentName);
        }
      //}  else {
        //throw new AgileNoResultException("BLE Device not connected: " + deviceName);
      //}
    } else {
      throw new AgileNoResultException("Protocol not supported: " + protocol);
    }
  }

 @Override
  public synchronized void Subscribe(String componentName) {
	logger.info ("Beuer BPM Subscribe "+componentName);
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
            if (!hasOtherActiveSubscription(componentName)) {
              deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
              addNewRecordSignalHandler();
				logger.info("Beuer BPM added Signal Handler");
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
	logger.info("Beuer BPM Unsubscribe: " +componentName);
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      //if (isConnected()) {
		if (true) {
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
		int result = 0;
			switch (componentName) {
			case systolic:
				result = readData[1] & 0xff;
				lastSystolicValue = result;
				break;
			case diastolic:
				result = readData[3] & 0xff;
				lastDiastolicValue = result;
				break;
			case pulse:
				 result=(readData[14] << 8) | readData[15] ;
				 lastPulseValue = result;
				break;
			default:
				break;
			}
		logger.info("Beuer BPM formatReading: "+componentName+" : " +result);
		return Integer.toString(result);
	}

	protected long formatReadingTime(String componentName, byte[] readData) {
		long result = 0;
		int year=((readData[8] & 0xff) << 8) | (readData[7] & 0xff);
		int month=readData[9];
		int day=readData[10];
		int hours=readData[11];
		int minutes=readData[12];
		Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hours, minutes,0);
		result = c.getTimeInMillis();
    logger.info("formatReadingTime {} {} {}", componentName, c.getTime(), result);
		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void addNewRecordSignalHandler() {
		try {
			if (newRecordSigHanlder == null && connection != null) {
 				newRecordSigHanlder = new DBusSigHandler<Protocol.NewRecordSignal>() {
					@Override
					public void handle(NewRecordSignal sig) {
						if (sig.address.equals(address)) {
 							for(String componentName : getComponentNames(sig.profile)){
 								String readVal = formatReading(componentName, sig.record);
 									RecordObject recObj = new RecordObject(deviceID, componentName,
 											formatReading(componentName, sig.record), getMeasurementUnit(componentName), "",
 											formatReadingTime(componentName, sig.record));
 									data = recObj;
 									logger.info("Beuer BPM85 Device notification component {} value {}", componentName, recObj.value);
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
		switch (sensor) {		
			case diastolic:
			case systolic:
				return "mmHg";
			default:		
 				return "";
		}
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
