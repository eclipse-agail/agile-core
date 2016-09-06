package iot.agile.object;

import java.util.List;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceDefinition extends Struct {
	@Position(0)
	@JsonProperty("deviceId")
	public final String deviceId;
	@Position(1)
	@JsonProperty("address")
	public final String address;
	@Position(2)
	@JsonProperty("name")
	public final String name;
	@Position(3)
	@JsonProperty("description")
	public final String description;
	@Position(4)
	@JsonProperty("protocol")
	public final String protocol;
	@Position(5)
	@JsonProperty("path")
	public final String path;
	@Position(6)
	@JsonProperty("streams")
	public final List<DeviceComponet> streams;

	@JsonCreator
	public DeviceDefinition(@JsonProperty("deviceId") String deviceId, @JsonProperty("address") String address,
			@JsonProperty("name") String name, @JsonProperty("description") String description,
			@JsonProperty("protocol") String protocol, @JsonProperty("path") String path,
			@JsonProperty("streams") List<DeviceComponet> streams) {
		this.deviceId = deviceId;
		this.address = address;
		this.name = name;
		this.description = description;
		this.protocol = protocol;
		this.path = path;
		this.streams = streams;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getPath() {
		return path;
	}

	public List<DeviceComponet> getStreams() {
		return streams;
	}

}
