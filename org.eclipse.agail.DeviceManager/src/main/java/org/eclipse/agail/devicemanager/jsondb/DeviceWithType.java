package org.eclipse.agail.devicemanager.jsondb;

import org.eclipse.agail.object.DeviceOverview;

public class DeviceWithType {

	String deviceType;
	DeviceOverview deviceOverview;
	
	public DeviceWithType() {
		// TODO Auto-generated constructor stub
	}

	public DeviceWithType(String deviceType, DeviceOverview deviceOverview) {
		this.deviceType = deviceType;
		this.deviceOverview = deviceOverview;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public DeviceOverview getDeviceOverview() {
		return deviceOverview;
	}

	public void setDeviceOverview(DeviceOverview deviceOverview) {
		this.deviceOverview = deviceOverview;
	}
}
