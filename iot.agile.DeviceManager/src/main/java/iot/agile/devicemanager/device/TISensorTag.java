package iot.agile.devicemanager.device;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import iot.agile.Device;
import iot.agile.object.DeviceDefinition;
import iot.agile.object.RecordObject;

import org.freedesktop.dbus.exceptions.DBusException;

public class TISensorTag extends DeviceImp implements Device {

	private static final String SENSOR_NAME = "SENSOR_NAME";
	
	private static final String UNIT = "UNIT";

	private static final String GATT_SERVICE = "GATT_SERVICE";

	private static final String GATT_CHARACTERSTICS_VALUE = "VALUE_CHAR";

	private static final String GATT_CHARACTERSTICS_CONFIG = "CONFIGURATION_CHAR";

	private static final String SENSOR_TURN_ON = "SENSOR_TURN_ON";
	
	private static final byte[] SENSOR_TURN_ON_VALUE ={0X01};
	
	private static final String SENSOR_TURN_OFF = "SENSOR_TURN_OFF";
	
	private static final byte[] SENSOR_TURN_OFF_VALUE ={0X01};
	
	
	/**
	 * TI Sensor Tag temperature service
	 */
	private static final String TEMPERATURE = "Temperature";

	private static final String TEMP_GATT_SERVICE_UUID = "f000aa00-0451-4000-b000-000000000000";

	private static final String TEMP_GATT_CHARACTERSTICS_UUID = "f000aa01-0451-4000-b000-000000000000";

	private static final String TEMP_GATT_CHARACTERSTICS_CONFIG_UUID = "f000aa02-0451-4000-b000-000000000000";
	/**
	 * TI Sensor Tag Accelerometer service
	 */
	private static final String ACCELEROMETER = "Accelerometer";

	private static final String ACC_GATT_SERVICE_UUID = "f000aa10-0451-4000-b000-000000000000";

	private static final String ACC_GATT_CHARACTERSTICS_UUID = "f000aa11-0451-4000-b000-000000000000";

	private static final String ACC_GATT_CHARACTERSTICS_CONFIG_UUID = "f000aa12-0451-4000-b000-000000000000";
	/**
	 * TI Sensor Tag Humidity service
	 */
	private static final String HUMIDITY = "Humidity";

	private static final String HUMIDITY_GATT_SERVICE_UUID = "f000aa20-0451-4000-b000-000000000000";

	private static final String HUMIDITY_GATT_CHARACTERSTICS_UUID = "f000aa21-0451-4000-b000-000000000000";

	private static final String HUMIDITY_GATT_CHARACTERSTICS_CONFIG_UUID = "f000aa22-0451-4000-b000-000000000000";
	/**
	 * TI Sensor Tag Magnetometer service
	 */
	private static final String MAGNETOMETER = "Magnetometer";

	private static final String MAGNETOMETER_GATT_SERVICE_UUID = "f000aa30-0451-4000-b000-000000000000";

	private static final String MAGNETOMETER_GATT_CHARACTERSTICS_UUID = "f000aa31-0451-4000-b000-000000000000";

	private static final String MAGNETOMETER_GATT_CHARACTERSTICS_CONFIG_UUID = "f000aa32-0451-4000-b000-000000000000";
	/**
	 * TI Sensor Tag Barometer service
	 */
	private static final String BAROMETER = "Barometer";

	private static final String BAROMETER_GATT_SERVICE_UUID = "f000aa40-0451-4000-b000-000000000000";

	private static final String BAROMETER_GATT_CHARACTERSTICS_UUID = "f000aa41-0451-4000-b000-000000000000";

	private static final String BAROMETER_GATT_CHARACTERSTICS_CONFIG_UUID = "f000aa42-0451-4000-b000-000000000000";
	/**
	 * TI Sensor Tag Gyroscope service
	 */
	private static final String GYROSCOPE = "Gyroscope";

	private static final String GYROSCOPE_GATT_SERVICE_UUID = "f000aa50-0451-4000-b000-000000000000";

	private static final String GYROSCOPE_GATT_CHARACTERSTICS_UUID = "f000aa51-0451-4000-b000-000000000000";

	private static final String GYROSCOPE_GATT_CHARACTERSTICS_CONFIG_UUID = "f000aa52-0451-4000-b000-000000000000";

	public TISensorTag(DeviceDefinition devicedefinition) throws DBusException {
		super(devicedefinition);
	}

	@Override
	public RecordObject Read(String sensorName) {
		if ((protocol.equals(BLUETOOTH_LOW_ENERGY)) && (deviceProtocol != null)) {
			if (deviceStatus.equals(CONNECTED)) {
				if (isSensorSupported(sensorName.trim())) {
					try {
						deviceProtocol.Write(deviceID, getProfile(sensorName.trim()));
						/**
						 * The default read data period (frequency) of most of
						 * sensor tag sensors is 1000ms therefore the first data
						 * will be available to read after 1000ms for these we
						 * call Read method after 1 second
						 */
						Thread.sleep(1010);
						RecordObject readValue = deviceProtocol.Read(deviceID, getProfile(sensorName.trim()));
						readValue.setValue(formatReading(sensorName, readValue.getValue()));
						data = readValue;
						return readValue;
					} catch (Exception e) {
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

	@Override
	protected boolean isSensorSupported(String sensorName) {
		if (sensorName.equals(TEMPERATURE) || sensorName.equals(ACCELEROMETER) || sensorName.equals(HUMIDITY)
				|| sensorName.equals(BAROMETER) || sensorName.equals(GYROSCOPE) || sensorName.equals(MAGNETOMETER)) {
			return true;
		}
		return false;
	}

	private Map<String, String> getProfile(String sensorName) {
		Map<String, String> profile = new HashMap<String, String>();
		profile.put(SENSOR_NAME, sensorName);
		if (sensorName.equals(TEMPERATURE)) {
			profile.put(UNIT, "Celsius");
			profile.put(GATT_SERVICE, TEMP_GATT_SERVICE_UUID);
			profile.put(GATT_CHARACTERSTICS_VALUE, TEMP_GATT_CHARACTERSTICS_UUID);
			profile.put(GATT_CHARACTERSTICS_CONFIG, TEMP_GATT_CHARACTERSTICS_CONFIG_UUID);
			profile.put(SENSOR_TURN_ON, new String(SENSOR_TURN_ON_VALUE));
			profile.put(SENSOR_TURN_OFF, new String(SENSOR_TURN_OFF_VALUE));
		} else if (sensorName.equals(ACCELEROMETER)) {
			profile.put(GATT_SERVICE, ACC_GATT_SERVICE_UUID);
			profile.put(GATT_CHARACTERSTICS_VALUE, ACC_GATT_CHARACTERSTICS_UUID);
			profile.put(GATT_CHARACTERSTICS_CONFIG, ACC_GATT_CHARACTERSTICS_CONFIG_UUID);
		} else if (sensorName.equals(HUMIDITY)) {
			profile.put(UNIT, "Relative humidity (%RH)");
 			profile.put(GATT_SERVICE, HUMIDITY_GATT_SERVICE_UUID);
			profile.put(GATT_CHARACTERSTICS_VALUE, HUMIDITY_GATT_CHARACTERSTICS_UUID);
			profile.put(GATT_CHARACTERSTICS_CONFIG, HUMIDITY_GATT_CHARACTERSTICS_CONFIG_UUID);
			profile.put(SENSOR_TURN_ON, new String(SENSOR_TURN_ON_VALUE));
			profile.put(SENSOR_TURN_OFF, new String(SENSOR_TURN_OFF_VALUE));
		} else if (sensorName.equals(BAROMETER)) {
			profile.put(GATT_SERVICE, MAGNETOMETER_GATT_SERVICE_UUID);
			profile.put(GATT_CHARACTERSTICS_VALUE, MAGNETOMETER_GATT_CHARACTERSTICS_UUID);
			profile.put(GATT_CHARACTERSTICS_CONFIG, MAGNETOMETER_GATT_CHARACTERSTICS_CONFIG_UUID);
		} else if (sensorName.equals(GYROSCOPE)) {
			profile.put(GATT_SERVICE, BAROMETER_GATT_SERVICE_UUID);
			profile.put(GATT_CHARACTERSTICS_VALUE, BAROMETER_GATT_CHARACTERSTICS_UUID);
			profile.put(GATT_CHARACTERSTICS_CONFIG, BAROMETER_GATT_CHARACTERSTICS_CONFIG_UUID);
		} else if (sensorName.equals(MAGNETOMETER)) {
			profile.put(GATT_SERVICE, GYROSCOPE_GATT_SERVICE_UUID);
			profile.put(GATT_CHARACTERSTICS_VALUE, GYROSCOPE_GATT_CHARACTERSTICS_UUID);
			profile.put(GATT_CHARACTERSTICS_CONFIG, GYROSCOPE_GATT_CHARACTERSTICS_CONFIG_UUID);
		}
		return profile;
	}

	/**
	 *
	 * The sensor service returns the data in an encoded format which can
	 * be found in the
	 * wiki(http://processors.wiki.ti.com/index.php/SensorTag_User_Guide#
	 * IR_Temperature_Sensor). 
	 * Convert the raw sensor reading value format to human understandable value and
	 * print it. 
	 * @param sensorName
	 *            Name of the sensor to read value from
	 * @param readingValue
	 *            the raw value read from the sensor
	 * @return
	 */
	private String formatReading(String sensorName, String readingValue) {
		String result;
		if (sensorName.contains(TEMPERATURE)) {
			byte[] temp = readingValue.getBytes(StandardCharsets.ISO_8859_1);
			int ambientTempRaw = (temp[2] & 0xff) | (temp[3] << 8);
			float ambientTempCelsius = convertCelsius(ambientTempRaw);
			result = Float.toString(ambientTempCelsius);
		} else if (sensorName.contains(HUMIDITY)) {
			byte[] humidity = readingValue.getBytes(StandardCharsets.ISO_8859_1);
			int lowerByte = Byte.toUnsignedInt(humidity[2]);
			int upperByte = Byte.toUnsignedInt(humidity[3]);
			int rawResult = (upperByte << 8) + (lowerByte & 0xff);
			float hum = convertHumidity(rawResult);
			result = Float.toString(hum);
		} else {
			// TODO
			result = "";
		}

		return result;

	}

	/**
	 * Converts temperature into degree Celsius
	 *
	 * @param raw
	 * @return
	 */
	private float convertCelsius(int raw) {
		return raw / 128f;
	}

	/**
	 * Formats humidity value according to SensorTag WIKI
	 * 
	 * @param raw
	 * @return
	 */
	private float convertHumidity(int raw) {
		int a = raw - (raw % 4);
		return ((-6f) + 125f * (a / 65535f));
	}
}
