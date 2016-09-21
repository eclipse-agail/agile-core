package iot.agile.devicemanager.device;

public class SensorUuid {
	public String serviceUuid;
	public String charValueUuid;
	public String charConfigUuid;
	public String charFreqUuid;

	public SensorUuid(String service, String charValueUuid, String charConfig, String charFreq) {
		this.serviceUuid = service;
		this.charValueUuid = charValueUuid;
		this.charConfigUuid = charConfig;
		this.charFreqUuid = charFreq;
	}
}
