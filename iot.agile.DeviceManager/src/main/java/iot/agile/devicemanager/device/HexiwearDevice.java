package iot.agile.devicemanager.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.exceptions.DBusException;
import iot.agile.Device;
import iot.agile.Protocol;
import iot.agile.Protocol.NewRecordSignal;
import iot.agile.object.DeviceDefinition;
import iot.agile.object.DeviceOverview;
import iot.agile.object.RecordObject;
import iot.agile.object.DeviceComponent;
import iot.agile.exception.AgileNoResultException;

public class HexiwearDevice extends AgileBLEDevice implements Device {
  protected Logger logger = LoggerFactory.getLogger(HexiwearDevice.class);
  protected static final Map<String, SensorUuid> sensors = new HashMap<String, SensorUuid>();
	private static final String Acc = "Acc";

	{
		subscribedComponents.put(Acc, 0);
	}

 	{
		profile.add(new DeviceComponent(Acc, ""));

	}
 

 	static {
		sensors.put(Acc, new SensorUuid("00002000-0000-1000-8000-00805f9b34fb", "00002001-0000-1000-8000-00805f9b34fb", "", ""));
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
			logger.info("DENNIS 2 Connect: " + componentName);
			//DeviceRead(componentName);
			if (subscribedComponents.get(componentName) > 0) {
				logger.info("Resubscribing to {}", componentName);
				deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
			}
		}
	}


  @Override
  protected String DeviceRead(String componentName) {
	logger.info("DENNIS DeviceRead: "+ componentName);
/*
	String resultStr1="";
	try {
            byte[] result1 = deviceProtocol.Read(address, getReadValueProfile(componentName));
	    resultStr1=new String(result1);	
	} catch (DBusException e) {
	    logger.error("Dennis error in DeviceRead from DBUS");
	    e.printStackTrace();	
	}
	logger.info ("Dennis result: "+ resultStr1);
	return resultStr1;	
*/
	//TODO handle all exception cases -> check other devices
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
            if (!hasotherActiveSubscription(componentName)) {
              deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
              addNewRecordSignalHandler();
            }
	    logger.info("Dennis Subscribe Hexiwear");
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
            if (!hasotherActiveSubscription(componentName)) {
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

	// =======================Utility methods===========================

	private Map<String, String> getReadValueProfile(String sensorName) {
		Map<String, String> profile = new HashMap<String, String>();
		SensorUuid s = sensors.get(sensorName);
		if (s != null) {
			profile.put(GATT_SERVICE, s.serviceUuid);
			profile.put(GATT_CHARACTERSTICS, s.charValueUuid);
			logger.info("DENNIS Gatt Service: "+s.serviceUuid);
			logger.info("DENNIS Gatt Characteristic: "+s.charValueUuid);
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
	protected boolean hasotherActiveSubscription(String componentName) {
		for (String component : subscribedComponents.keySet()) {
			if (subscribedComponents.get(component) > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String formatReading(String componentName, byte[] readData) {
		switch (componentName) {
   		   case Acc:
			int resultX = 0;
			int resultY = 0;
			int resultZ = 0;
			resultX =   (readData[1] << 8) | readData[0];
			resultY =   (readData[3] << 8) | readData[2];
			resultZ =   (readData[5] << 8) | readData[4];
			String arr = "["+Integer.toString(resultX)+","+Integer.toString(resultY)+","+Integer.toString(resultZ)+"]";
			return arr;
		}
		return "0";
	}

	/**
	 * The read operation on this device returns 4 zero's after a single
	 * non-zero read value. Therefore, in order to avoid signaling of the zero
	 * values (w/h are not the actual read) we override this method and put this
	 * condition to check the value
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void addNewRecordSignalHandler() {
		logger.info("Dennis addNewRecordSignalHandler");	
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
