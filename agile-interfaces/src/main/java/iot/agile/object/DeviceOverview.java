package iot.agile.object;

 import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

public  final class DeviceOverview extends Struct {
  @Position(0)
  public final String id;
  @Position(1)
  public final String protocol;
  @Position(2)
  public final String name;
  @Position(3)
  public final String status;

  public DeviceOverview(String id, String protocol, String name, String status) {
    this.id = id;
    this.protocol= protocol;
    this.name = name;
    this.status = status;
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

  public String getStatus() {
    return status;
  }

}