package iot.agile.devicemanager.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.exceptions.DBusException;

import iot.agile.Device;
import iot.agile.Protocol;
import iot.agile.Protocol.NewRecordSignal;
import iot.agile.object.DeviceDefinition;
import iot.agile.object.RecordObject;

public class MedicalDevice extends AgileBLEDevice implements Device {
	private static final String SpO2 = "SpO2";
	private static final String PULSE = "PULSE";
	private static final String PI = "PI";

	{
		subscribedComponents.put(SpO2, 0);
		subscribedComponents.put(PULSE, 0);
		subscribedComponents.put(PI, 0);
	}

	private static final Map<String, String> componentUnits = new HashMap<String, String>();
 	static {
		componentUnits.put(SpO2, "");
		componentUnits.put(PULSE, "");
		componentUnits.put(PI, "");

	}
 

 	static {
		sensors.put(PI, new SensorUuid("cdeacb80-5235-4c07-8846-93a37ee6b86d", "cdeacb81-5235-4c07-8846-93a37ee6b86d", "", ""));
		sensors.put(PULSE,
				new SensorUuid("cdeacb80-5235-4c07-8846-93a37ee6b86d", "cdeacb81-5235-4c07-8846-93a37ee6b86d", "", ""));
		sensors.put(SpO2,
				new SensorUuid("cdeacb80-5235-4c07-8846-93a37ee6b86d", "cdeacb81-5235-4c07-8846-93a37ee6b86d","", ""));
	}

	public MedicalDevice(DeviceDefinition devicedefinition) throws DBusException {
		super(devicedefinition);
	}


	@Override
	protected String DeviceRead(String componentName) {
		logger.info("Read not supported");
		return null;
	}
	
	@Override
	public void Subscribe(String componentName) {
 		if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
			if (deviceStatus.equals(CONNECTED)) {
				if (isSensorSupported(componentName.trim())) {
					try {
						if (!hasotherActiveSubscription(componentName)) {
							deviceProtocol.Subscribe(address, getReadValueProfile(componentName));
							addNewRecordSignalHandler();
						}
						subscribedComponents.put(componentName, subscribedComponents.get(componentName) + 1);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					logger.debug("Sensor not supported: {}", componentName);
				}
			} else {
				logger.debug("BLE Device not connected: {}", deviceName);
			}
		} else {
			logger.debug("Protocol not supported:: {}", protocol);
		}
	}

	@Override
	public void Unsubscribe(String componentName) throws DBusException {
		if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
			if (deviceStatus.equals(CONNECTED)) {
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
					logger.debug("Sensor not supported: {}", componentName);
				}
			} else {
				logger.debug("BLE Device not connected: {}", deviceName);
			}
		} else {
			logger.debug("Protocol not supported:: {}", protocol);
		}
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
		float result = 0;
		if ((readData.length == 11) && ((readData[0] & 0xff) == 0x88)) {
			switch (componentName) {
			case SpO2:
				break;
			case PULSE:
				break;
			case PI:
				break;
			default:
				break;
			}
		} else if (readData.length == 4 && ((readData[0] & 0xff) == 0x81)) {
			switch (componentName) {
			case SpO2:
				result = readData[2] & 0xff;
				break;
			case PULSE:
				result = readData[1] & 0xff;
				break;
			case PI:
				result = (float) ((readData[3] & 0xff) / 10.0);
				break;
			default:
				break;
			}
		}
		return Float.toString(result);
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
 		return "Percentile(%)";
 	}

}
