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
  @JsonProperty("id")
  public final String id;
  @Position(1)
  @JsonProperty("name")
  public final String name;
  @Position(2)
  @JsonProperty("protocol")
  public final String protocol;
  @Position(3)
  @JsonProperty("path")
  public final String path;
  @Position(4)
  @JsonProperty("streams")
  public final List<DeviceComponet> streams;

  @JsonCreator
  public DeviceDefinition(@JsonProperty("id") String id,@JsonProperty("name") String name, @JsonProperty("protocol") String protocol,
       @JsonProperty("path") String path,
      @JsonProperty("streams") List<DeviceComponet> streams) {
    this.id = id;
    this.protocol = protocol;
    this.name = name;
    this.path = path;
    this.streams = streams;
  }

  public List<DeviceComponet> getStreams() {
    return streams;
  }

  public String getId() {
    return id;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

}
