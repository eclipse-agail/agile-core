package iot.agile.object;

import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;

public class DeviceComponet extends Struct {
  @Position(0)
  public final String id;
  @Position(1)
  public final String unit;
 
  public DeviceComponet(String id, String unit){
    this.id = id;
    this.unit = unit;
  }

}
