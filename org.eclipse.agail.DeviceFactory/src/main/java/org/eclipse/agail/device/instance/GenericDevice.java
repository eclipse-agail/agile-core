package org.eclipse.agail.device.instance;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.agail.Device;
import org.eclipse.agail.device.base.AgileBLEDevice;
import org.eclipse.agail.device.base.SensorUuid;
import org.eclipse.agail.exception.AgileNoResultException;
import org.eclipse.agail.object.DeviceComponent;
import org.eclipse.agail.object.DeviceDefinition;
import org.eclipse.agail.object.DeviceOverview;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericDevice extends AgileBLEDevice implements Device {
	protected Logger logger = LoggerFactory.getLogger(GenericDevice.class);

	public static String deviceTypeName = "Generic";

	protected static Map<String, SensorUuid> sensors = new HashMap<String, SensorUuid>();
	protected Map<String, SensorUuid> sensorsGeneric = new HashMap<String, SensorUuid>();
	protected static final Map<String, byte[]> commands = new HashMap<String, byte[]>();
	private static final byte[] TURN_ON_SENSOR = { 0X01 };
	private static final byte[] TURN_OFF_SENSOR = { 0X00 };

	public GenericDevice(DeviceOverview deviceOverview) throws DBusException {
		super(deviceOverview);
	}

	public GenericDevice(DeviceDefinition devicedefinition) throws DBusException {
		super(devicedefinition);
	}

	public static boolean Matches(DeviceOverview d) {
		return true;
	}

	@Override
	public void Connect() throws DBusException {
		logger.debug("Profile: " + profile.size());
		super.Connect();
		GetServices();
		for (String componentName : subscribedComponents.keySet()) {
			logger.debug("Sensor Name: " + componentName);
			if (subscribedComponents.get(componentName) > 0) {
				Map<String, String> readValue = getReadValueProfile(componentName);
				logger.debug("{}", readValue);
				bleProtocol.Subscribe(address, readValue);
			}
		}
		logger.debug("========== Connected ============");
	}

	@Override
	public void Execute(String commandId) {
		// TODO Auto-generated method stub
	}

	@Override
	public String DeviceRead(String sensorName) {
		if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (bleProtocol != null)) {
			if (isConnected()) {
				if (isSensorSupported(sensorName.trim())) {
					try {
						bleProtocol.Write(address, getEnableSensorProfile(sensorName), TURN_ON_SENSOR);
						byte[] readValue = bleProtocol.Read(address, getReadValueProfile(sensorName));
						return Arrays.toString(readValue);
					} catch (Exception e) {
						logger.debug("Error in reading value from Sensor {}", e);
						e.printStackTrace();
					}
				} else {
					logger.debug("Sensor not supported: {}", sensorName);
					return null;
				}
			} else {
				logger.debug("BLE Device not connected: {}", deviceName);
				return null;
			}
		} else {
			logger.debug("Protocol not supported:: {}", protocol);
			return null;
		}
		return null;
	}

	public String NotificationRead(String componentName) {
		if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (bleProtocol != null)) {
			if (isConnected()) {
				if (isSensorSupported(componentName.trim())) {
					try {
						byte[] result = bleProtocol.NotificationRead(address, getReadValueProfile(componentName));
						formatReading(componentName, result);
						return Arrays.toString(result);
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
		throw new AgileNoResultException("Unable to read " + componentName);
	}

	@Override
	public synchronized void Subscribe(String componentName) {
		logger.info("Subscribe to {}", componentName);
		if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (bleProtocol != null)) {
			if (isConnected()) {
				if (isSensorSupported(componentName.trim())) {
					try {
						if (!hasOtherActiveSubscription()) {
							addNewRecordSignalHandler();
						}
						bleProtocol.Write(address, getEnableSensorProfile(componentName), TURN_ON_SENSOR);
						byte[] result = bleProtocol.Read(address, getReadValueProfile(componentName));
						formatReading(componentName, result);
						subscribedComponents.put(componentName, subscribedComponents.get(componentName) + 1);
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
	}

	@Override
	public synchronized void Unsubscribe(String componentName) throws DBusException {
		logger.info("Unsubscribe from {}", componentName);
		if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (bleProtocol != null)) {
			if (isConnected()) {
				if (isSensorSupported(componentName.trim())) {
					try {
						subscribedComponents.put(componentName, subscribedComponents.get(componentName) - 1);
						if (!hasOtherActiveSubscription(componentName)) {
							// disable notification
							bleProtocol.Unsubscribe(address, getReadValueProfile(componentName));
						}
						if (!hasOtherActiveSubscription()) {
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
		if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (bleProtocol != null)) {
			if (isConnected()) {

				try {
					bleProtocol.Write(address, getReadValueProfile(componentName), getBytes(payload));
				} catch (Exception ex) {
					logger.error("Exception occured in Write: " + ex);
				}
			} else {
				throw new AgileNoResultException("BLE Device not connected: " + deviceName);
			}
		} else {
			throw new AgileNoResultException("Protocol not supported: " + protocol);
		}
	}

	@Override
	public List<String> Commands() {
		// TODO Auto-generated method stub
		return null;
	}

	public void GetServices() throws DBusException {
		if (!profile.isEmpty() && !subscribedComponents.isEmpty() && !sensors.isEmpty()) {
			return;
		}
		Map<String, List<String>> services = null;
		if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (bleProtocol != null)) {
			if (isConnected()) {
				services = bleProtocol.GetSensors(address);
				logger.info("Device connected {} have {} sensors", deviceID, services.size());
				if (!services.isEmpty()) {
					logger.debug("Sensors found {}", services.size());
					int i = 0;
					for (Entry<String, List<String>> deviceComponent : services.entrySet()) {
						logger.debug("GATTCharacteristics size: {}", deviceComponent.getValue().size());
						// if ((deviceComponent.getValue().size() == 2 ||
						// deviceComponent.getValue().size() == 3)) {
						String name = deviceComponent.getKey() + " - Sensor " + (++i);
						subscribedComponents.put(name, 0);
						addSensor(name, deviceComponent.getKey(), deviceComponent.getValue());
						// profile.add(new DeviceComponent(deviceComponent.getKey(),
						// deviceComponent.getKey()));
						profile.add(new DeviceComponent(name, name));
						logger.debug("Profile: {}", profile);
						// logger.debug("Subscribed Components: {} ", subscribedComponents);
						// }
					}
				} else {
					throw new AgileNoResultException("Device doesn't have any sensor: " + address);
				}
			} else {
				throw new AgileNoResultException("BLE Device not connected: " + deviceName);
			}
		} else {
			throw new AgileNoResultException("Protocol not supported: " + protocol);
		}
	}

	@Override
	public List<DeviceComponent> Profile() {
		return profile;
	}

	private void addSensor(String sensorName, String bluetoothGattService, List<String> characteristics) {
		SensorUuid sensorUuid = new SensorUuid();
		sensorUuid.serviceUuid = bluetoothGattService;
		java.util.Collections.sort(characteristics);
		for (int i = 0; i < characteristics.size(); i++) {
			logger.debug("Sensor Name: {} CharUUID: {}", sensorName, characteristics.get(i));
			if (i == 0) {
				sensorUuid.charValueUuid = characteristics.get(i);
			}
			if (i == 1) {
				sensorUuid.charConfigUuid = characteristics.get(i);
			}
			if (i == 2) {
				sensorUuid.charFreqUuid = characteristics.get(i);
			}
		}

		sensors.put(sensorName, sensorUuid);
	}

	private Map<String, String> getReadValueProfile(String sensorName) {
		Map<String, String> profile = new HashMap<String, String>();
		SensorUuid s = sensors.get(sensorName);
		if (s != null) {
			profile.put(GATT_SERVICE, s.serviceUuid);
			profile.put(GATT_CHARACTERSTICS, s.charValueUuid);
		}
		return profile;
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

	private Map<String, String> getFrequencyProfile(String sensorName) {
		Map<String, String> profile = new HashMap<String, String>();
		SensorUuid s = sensors.get(sensorName);
		if (s != null) {
			profile.put(GATT_SERVICE, s.serviceUuid);
			profile.put(GATT_CHARACTERSTICS, s.charFreqUuid);
		}
		return profile;
	}

	private byte[] getBytes(String payload) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		output.write(Byte.valueOf(payload));

		byte[] bytes = output.toByteArray();

		return bytes;
	}

	private Map<String, String> getTurnOffSensorProfile(String sensorName) {
		Map<String, String> profile = new HashMap<String, String>();
		SensorUuid s = sensors.get(sensorName);
		if (s != null) {
			profile.put(GATT_SERVICE, s.serviceUuid);
			profile.put(GATT_CHARACTERSTICS, s.charConfigUuid);
		}
		return profile;
	}

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

	@Override
	protected String formatReading(String sensorName, byte[] readData) {
		String s = new String(readData);
		logger.debug("=======================");
		logger.debug("new String(bytes) -> Simple decryption: " + s);
		logger.debug("Raw format: " + readData);
		logger.debug("Arrays.toString(): " + Arrays.toString(readData));
		logger.debug("=======================");
		return readData.toString();
	}

}
