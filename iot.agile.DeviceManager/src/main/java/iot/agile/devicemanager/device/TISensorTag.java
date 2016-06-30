package iot.agile.devicemanager.device;

import java.util.HashMap;
import java.util.Map;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import iot.agile.Device;

public class TISensorTag extends DeviceImp implements   Device{

	/**
	 * GATT service and characteristics for TI Sensor Tag temperature service
	 */
	private static final String TEMPERATURE = "Temperature";

	private static final String SENSOR_NAME = "SensorName";

	private static final String TEMP_GATT_SERVICE = "TemperatureService";

	private static final String TEMP_GATT_SERVICE_UUID = "f000aa00-0451-4000-b000-000000000000";

	private static final String TEMP_VALUE_GATT_CHARACTERSTICS = "TemperatureValueCharacterstics";

	private static final String TEMP_VALUE_GATT_CHARACTERSTICS_UUID = "f000aa01-0451-4000-b000-000000000000";

	private static final String TEMP_CONFIGURATION_GATT_CHARACTERSTICS = "TemperatureConfigurationCharacterstics";

	private static final String TEMP_CONFIGURATION_GATT_CHARACTERSTICS_UUID = "f000aa02-0451-4000-b000-000000000000";

	public TISensorTag(String deviceID, String deviceName, String protocol) throws DBusException {
		super(deviceID, deviceName, protocol);
// 		String devicePath = AGILE_DEVICE_BASE_BUS_PATH + deviceName.trim();
//		connection = DBusConnection.getConnection(DBusConnection.SESSION);
//		connection.requestBusName(deviceAgileID);
//		connection.exportObject(devicePath, this);
	}
	
	
	
	
	

	@Override
	public String Read(String sensorName) {
		if (protocol.equals(BLUETOOTH_LOW_ENERGY) && deviceProtocol != null) {
			if (deviceStatus.equals(CONNECTED)) {

				if (sensorName.equals(TEMPERATURE)) {
					/**
					 * Turn on the temperature service by writing One in the
					 * configuration characteristics
					 */
 					try {
						deviceProtocol.Write(deviceID, getTemperatureProfile());
						return deviceProtocol.Read(deviceID, getTemperatureProfile());

					} catch (DBusException e) {
						logger.debug("dag: {}", sensorName);

						e.printStackTrace();
					}

				} else {
					logger.debug("Sensor not supported: {}", sensorName);
					return "Sensor not supported: " + sensorName;
				}
			} else {
				logger.debug("BLE Device not connected: {}", deviceName);
				return "BLE Device not connected: " + deviceName;
			}
		} else {
			logger.debug("Protocol not supported:: {}", protocol);
			return "Protocol not supported: " + protocol;
		}

		return null;

		// Should be two operation

		// Write

		// Read

	}

	private Map<String, String> getTemperatureProfile() {
		Map<String, String> profile = new HashMap<String, String>();
		profile.put(SENSOR_NAME, TEMPERATURE);
		profile.put(TEMP_GATT_SERVICE, TEMP_GATT_SERVICE_UUID);
		profile.put(TEMP_VALUE_GATT_CHARACTERSTICS, TEMP_VALUE_GATT_CHARACTERSTICS_UUID);
		profile.put(TEMP_CONFIGURATION_GATT_CHARACTERSTICS, TEMP_CONFIGURATION_GATT_CHARACTERSTICS_UUID);

		return profile;
	}

}
