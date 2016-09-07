package iot.agile.object;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"parameters"})
public  final class DiscoveryStatus extends Struct {
  @Position(0)
  public final String name;
  @Position(1)
  public final String status;

  public DiscoveryStatus(String name, String status) {
    this.name = name;
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public String getStatus() {
    return status;
  }

}