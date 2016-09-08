package iot.agile.object;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"parameters"})
public class DeviceComponet extends Struct {
  @Position(0)
  @JsonProperty("id")
  public final String id;

  @Position(1)
  @JsonProperty("unit")
  public final String unit;

  @JsonCreator
  public DeviceComponet(@JsonProperty("id") String id, @JsonProperty("unit") String unit) {
    this.id = id;
    this.unit = unit;
  }

}
