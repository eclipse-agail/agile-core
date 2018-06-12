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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;
import java.text.ParseException;

public class ActivityDeviceX10Pro extends AgileBLEDevice implements Device {
  protected Logger logger = LoggerFactory.getLogger(ActivityDeviceX10Pro.class);
  protected static final Map<String, SensorUuid> sensors = new HashMap<String, SensorUuid>();
	private static final String Steps = "step_count_day";
  private static final String HeartRate = "heart_rate";
  private static final String StepsStored = "step_count_history";
  private static final String SleepStored = "sleep_history";

  private static final String Setup = "setup";

  private static int lastStepValue = 0 ;
  private static int lastHeartRateValue = 0;

  private long trackerHistoryStartTime = 0;

  private static final byte[] SEND_STORED_STEPS_CMD = { (byte) 0x53 };
	{
		subscribedComponents.put(Steps, 0);
    subscribedComponents.put(HeartRate, 0);
    subscribedComponents.put(StepsStored, 0);
    //subscribedComponents.put(SleepStored, 0);
	}

 	{
		profile.add(new DeviceComponent(Steps, ""));
    profile.add(new DeviceComponent(HeartRate, ""));
    profile.add(new DeviceComponent(StepsStored, ""));
    profile.add(new DeviceComponent(SleepStored, ""));
	}


 	static {
		sensors.put(Steps, new SensorUuid("6e400001-b5a3-f393-e0a9-e50e24dcca9e", "6e400003-b5a3-f393-e0a9-e50e24dcca9e", "", ""));
    sensors.put(HeartRate, new SensorUuid("6e400001-b5a3-f393-e0a9-e50e24dcca9e", "6e400003-b5a3-f393-e0a9-e50e24dcca9e", "", ""));
    sensors.put(StepsStored, new SensorUuid("6e400001-b5a3-f393-e0a9-e50e24dcca9e", "6e400003-b5a3-f393-e0a9-e50e24dcca9e", "6e400002-b5a3-f393-e0a9-e50e24dcca9e", ""));
    sensors.put(SleepStored, new SensorUuid("6e400001-b5a3-f393-e0a9-e50e24dcca9e", "6e400003-b5a3-f393-e0a9-e50e24dcca9e", "6e400002-b5a3-f393-e0a9-e50e24dcca9e", ""));

    sensors.put(Setup, new SensorUuid("6e400001-b5a3-f393-e0a9-e50e24dcca9e", "6e400003-b5a3-f393-e0a9-e50e24dcca9e", "6e400002-b5a3-f393-e0a9-e50e24dcca9e", ""));

	}

	public static boolean Matches(DeviceOverview d) {
		return d.name.contains("X10Pro") ;
	}

	public static String deviceTypeName = "Activity Device (X10Pro)";

	public ActivityDeviceX10Pro(DeviceOverview deviceOverview) throws DBusException {
		super(deviceOverview);
	}


	public ActivityDeviceX10Pro(DeviceDefinition devicedefinition) throws DBusException {
		super(devicedefinition);
	}

	@Override
	public void Connect() throws DBusException {
		super.Connect();
		for (String componentName : subscribedComponents.keySet()) {
			if (subscribedComponents.get(componentName) > 0) {
				logger.info("Resubscribing to {}", componentName);
				deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
			}
		}
        //setTime();
	}


  @Override
  protected String DeviceRead(String componentName) {
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          switch (componentName) {
      			case Steps:
                return Integer.toString(lastStepValue);
      			case HeartRate:
                return Integer.toString(lastHeartRateValue);
      			default:
              return "";
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
  public synchronized void Subscribe(String componentName) {
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
            if (!hasOtherActiveSubscription(componentName)) {
                logger.info("subscribing to " +componentName);
              deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
              if (componentName.equals(StepsStored) /*|| componentName.equals(SleepStored)*/){
                deviceProtocol.Write(address, getEnableSensorProfile(StepsStored), SEND_STORED_STEPS_CMD);
                trackerHistoryStartTime = System.currentTimeMillis();
              }
              addNewRecordSignalHandler();
              subscribedComponents.put(componentName, subscribedComponents.get(componentName) + 1);
            }
            //subscribedComponents.put(componentName, subscribedComponents.get(componentName) + 1);
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
	logger.info(componentName +" unsubscribe");
    if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
      if (isConnected()) {
        if (isSensorSupported(componentName.trim())) {
          try {
			if ((subscribedComponents.get(componentName)) > 0) {
            	subscribedComponents.put(componentName, subscribedComponents.get(componentName) - 1);
			}
            if (!hasOtherActiveSubscription(componentName)) {
			 logger.info(componentName +" no other active subscription");
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
		if ((subscribedComponents.get(componentName)) > 0)
        	subscribedComponents.put(componentName, subscribedComponents.get(componentName) - 1);
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
            if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
              if (isConnected()) {
                try {
                    String[] commandBytes = command.split(":");
                    if (commandBytes != null && commandBytes.length > 0){
                         switch (commandBytes[0]) {
                            case "set_timezone":
                                if (commandBytes.length > 1) 
                                    setTime(commandBytes[1],"");
                                else 
                                    setTime("","");
                                break;
                            case "set_time":
                                if (commandBytes.length > 1) 
                                    setTime("",commandBytes[1]);
                                else 
                                    setTime("","");
                                break;
                            case "asm":
                                if (commandBytes.length > 1) { 
                                    String asmString = command.substring(4);
                                    byte[] asmCommand= hexStringToByteArray(asmString);
                                    logger.info(address +" : "+ getEnableSensorProfile(Setup) +" : " + DatatypeConverter.printHexBinary(asmCommand));
                                    deviceProtocol.Write(address, getEnableSensorProfile(Setup) , asmCommand);
                                }
                                break;    
                            default:
                        }

                    }                     
                } catch (Exception e) {
                    e.printStackTrace();
                } 
              } else {
                throw new AgileNoResultException("BLE Device not connected: " + deviceName);
              }
            } else {
              throw new AgileNoResultException("Protocol not supported: " + protocol);
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
			logger.info("Activity Gatt Service: "+s.serviceUuid);
			logger.info("Activity Gatt Characteristic: "+s.charValueUuid);

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
			logger.info("component["+componentName+"]:"+ subscribedComponents.get(component));
			if (subscribedComponents.get(component) > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String formatReading(String componentName, byte[] readData) {
		int result = 0;
    logger.info(componentName +" formatReading: " + byteArrayToHex(readData));

		switch (componentName) {
			case Steps:
        if ((readData.length == 18)){
          result = (((readData[4] & 0xff) << 8) | (readData[5] & 0xff));
          lastStepValue = result;
        }
				break;
			case HeartRate:
        if ((readData[0] & 0xff)== 0xfe){
          result = readData[1] & 0xff;
          lastHeartRateValue = result;
        }
				break;
      case StepsStored:
          if ((readData.length == 4) && ((readData[2] & 0x80)) != 0x80) {
            result = (((readData[0] & 0xff)<<8) | (readData[1] & 0xff));
            int timeInc = ((readData[2] & 0xff)<<8) | (readData[3] & 0xff);
            if ((timeInc % 5) !=0){
                 result = 0;
                 logger.info ("StoredStep time not multiple of 5 " + timeInc);
          }
        }
                break;
      case SleepStored:  
          if ((readData.length == 4) && ((readData[2] & 0x80)) == 0x80) {
            result = (((readData[0] & 0xff)<<8) | (readData[1] & 0xff));
            if (result == 0) result = 1;
            logger.info("Sleep");
                //debug
                if (readData.length < 3) break;
                if ((readData.length == 4) && ((readData[2] & 0x80)) != 0x80){
                    int timeInc = ((readData[2] & 0xff)<<8) | (readData[3] & 0xff);
                    if ((timeInc < 18 * 12) && (timeInc > 12 * 12)){
                        logger.info("Debug Sleep");
                        result = 1;
                        }
                }
            }
    
                break;
	  default:

		}
		return Integer.toString(result);
	}

  protected long extractStoredStepTime(byte[] readData, long currentTime){
    long time = currentTime;
    if ((readData.length == 4) /*&& ((readData[2] & 0xff)) != 0x80*/) {
      int timeInc = ((readData[2] & 0x7f)<<8) | (readData[3] & 0xff);
      time = currentTime - (timeInc * 60 * 1000);
      logger.info("StoredStep Time: {} {}", timeInc , time  );
    }
    return time;
  }
	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void addNewRecordSignalHandler() {
    logger.info("ActivityTracker addNewRecordSignalHandler");
		try {
			if (newRecordSigHanlder == null && connection != null) {
 				newRecordSigHanlder = new DBusSigHandler<Protocol.NewRecordSignal>() {
					@Override
					public void handle(NewRecordSignal sig) {
						if (sig.address.equals(address)) {
 							for(String componentName : getComponentNames(sig.profile)){
 								String readVal = formatReading(componentName, sig.record);
 								 if (Integer.parseInt(readVal) != 0 ) {
                   RecordObject recObj;
                   if (componentName.equals(StepsStored) || componentName.equals(SleepStored)){
                     recObj = new RecordObject(deviceID, componentName,
                         formatReading(componentName, sig.record), getMeasurementUnit(componentName), "",
                         extractStoredStepTime(sig.record ,trackerHistoryStartTime));
                   } else {
     									recObj = new RecordObject(deviceID, componentName,
     											formatReading(componentName, sig.record), getMeasurementUnit(componentName), "",
     											System.currentTimeMillis());
                   }
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

    /**
    *  set Time + Timezone
    *  timezone == "" -> set Europe/Athens as default timezone and time from current system time
    *  timezone != "" -> set timezone from string and time from current system time
    *  time != "" -> set time from string
    * 
    */
    private void setTime (String timezone, String time) throws DBusException {

        TimeZone tz = TimeZone.getTimeZone("Europe/Athens");
        boolean tzExists= Arrays.asList(TimeZone.getAvailableIDs()).contains(timezone);
        logger.info(timezone + ":"+time+":tzExists "+tzExists);
        if (!timezone.equals("") && Arrays.asList(TimeZone.getAvailableIDs()).contains(timezone)){
            tz = TimeZone.getTimeZone(timezone);
            logger.info("Setting timezone " + timezone);
        }
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
        logger.info("Time offset " + (offsetInMillis/(1000 * 60)) );

 
        Date curDate;
        String trackerDateString="";
        if (time.equals("")) {
            curDate = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
            dateFormat.setTimeZone(tz);
            trackerDateString = "$T"+ dateFormat.format(curDate);
        } else {
            DateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
            try {    
                curDate = formatter.parse(time); 
                trackerDateString = "$T"+ formatter.format(curDate);               
            } catch (ParseException e) {
                e.printStackTrace();        
            } 
        }

        
        logger.info(address +" : "+ getEnableSensorProfile(Setup) +" : " +trackerDateString);
        deviceProtocol.Write(address, getEnableSensorProfile(Setup) , trackerDateString.getBytes());
    }

   public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                             + Character.digit(s.charAt(i+1), 16));
    }     return data;
   }

}
