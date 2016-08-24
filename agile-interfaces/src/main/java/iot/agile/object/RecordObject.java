package iot.agile.object;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

public final class RecordObject extends Struct {
	@Position(0)
	public String deviceID;
	@Position(1)
	public String componentID;
	@Position(2)
	public String value;
	@Position(3)
	public String unit;
	@Position(4)
	public String format;
	@Position(5)
	public long lastUpdate;

	public RecordObject(String deviceID, String componentID, String value, String unit, String format,
			long lastUpdate) {
		this.deviceID = deviceID;
		this.componentID = componentID;
		this.value = value;
		this.unit = unit;
		this.format = format;
		this.lastUpdate = lastUpdate;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getComponentID() {
		return componentID;
	}

	public void setComponentID(String componentID) {
		this.componentID = componentID;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

 

}
