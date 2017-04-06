package iot.agile.devicefactory;

import iot.agile.Device;
import iot.agile.DeviceFactory;
import iot.agile.object.AbstractAgileObject;
import iot.agile.object.DeviceOverview;
import iot.agile.devicefactory.device.DummyDevice;
import iot.agile.devicefactory.device.MedicalDevice;
import iot.agile.devicefactory.device.TISensorTag;
import iot.agile.devicefactory.device.HexiwearDevice;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agile device factory
 * 
 * @author dagi
 *
 */
public class DeviceFactoryImp extends AbstractAgileObject implements DeviceFactory {
    
    /**
    * Bus name for the device factory
    */
   private static final String AGILE_DEVICEFACTORY_BUS_NAME = "iot.agile.DeviceFactory";
   /**
    * Bus path for the device factory
    */
   private static final String AGILE_DEVICEFACTORY_BUS_PATH = "/iot/agile/DeviceFactory";

  /**
   * main method to instantiate device factory
   * @param args 
   */
    public static void main(String[] args) throws DBusException {
        DeviceFactoryImp deviceFactory=new DeviceFactoryImp();
	}
    
    public DeviceFactoryImp() throws DBusException {
        dbusConnect(AGILE_DEVICEFACTORY_BUS_NAME, AGILE_DEVICEFACTORY_BUS_PATH, this);
        //logger.debug("Started Device Factory");
    }
  /**
   * Get device based on device type
   * 
   * @param deviceType
   *          device type
   * @param deviceOverview
   *          device overview
   * @return
   * @throws Exception
   */
  public Device getDevice(String deviceType, DeviceOverview deviceOverview) throws Exception {
    Device device = null;
    // TODO Enum based device type, not string
    switch (deviceType) {
    case "TI SensorTag":
      device = new TISensorTag(deviceOverview);
      break;
    case "Oximeter":
      device = new MedicalDevice(deviceOverview);
      break;
    case "HEXIWEAR":
      device = new HexiwearDevice(deviceOverview);
      break;
    case "Dummy":
      device = new DummyDevice(deviceOverview);
      break;
    default:
      throw new Exception("Device: " + deviceType + " does not have a matching type ");
    }
    return device;
  }
  
  /*
  Override abstract method in DBusInterface
  */
  
  @Override
	public boolean isRemote() {
		return false;
	}
}
