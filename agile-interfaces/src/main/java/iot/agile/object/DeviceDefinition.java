package iot.agile.object;

import java.util.List;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

public class DeviceDefinition extends Struct {
  @Position(0)
  public final String id;
  @Position(1)
  public final String name;
  @Position(2)
  public final String protocol;
  @Position(3)
  public final String path;
  @Position(4)
  public final List<DeviceComponet> streams;
  
  
  
  public DeviceDefinition(String id, String protocol, String name, String path, List<DeviceComponet> streams) {
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