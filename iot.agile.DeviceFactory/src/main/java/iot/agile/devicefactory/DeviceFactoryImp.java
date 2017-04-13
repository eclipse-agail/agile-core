package iot.agile.devicefactory;

import iot.agile.Device;
import iot.agile.DeviceFactory;
import iot.agile.object.AbstractAgileObject;
import iot.agile.object.DeviceOverview;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    protected final Logger logger = LoggerFactory.getLogger(DeviceFactoryImp.class);
    
    /**
    * Bus name for the device factory
    */
   private static final String AGILE_DEVICEFACTORY_BUS_NAME = "iot.agile.DeviceFactory";
   /**
    * Bus path for the device factory
    */
   private static final String AGILE_DEVICEFACTORY_BUS_PATH = "/iot/agile/DeviceFactory";
   
   /**
    * The HashMap of loaded classes
    */
   private static HashMap <String, Class> Classes= new HashMap <String,Class>();

  /**
   * main method to instantiate device factory
   * @param args 
   */
    public static void main(String[] args) throws DBusException {
        DeviceFactoryImp deviceFactory=new DeviceFactoryImp();
	}
    
    private HashMap<String,Class> loadClasses(){
        
        HashMap <String, Class> Classes= new HashMap <String,Class>();
        
    try{
        File filePath = new File("/home/agile/gitsample/agile-core/iot.agile.DeviceFactory/target/classes/iot/agile/devicefactory/device");
        File[] files = filePath.listFiles();
        for (File file:files)
        {
            if(!(file.getName().contains("$")))
            {
                ClassLoader classLoader = DeviceFactoryImp.class.getClassLoader();
                logger.debug("Loaded ClassLoader, trying to load iot.agile.devicefactory.device."+file.getName().split("\\.")[0]);
                Class aClass = classLoader.loadClass("iot.agile.devicefactory.device."+file.getName().split("\\.")[0]);
                logger.debug("The class was loaded");
                Classes.put(file.getName().split("\\.")[0], aClass);
            }
        }
    }
    catch(SecurityException e){
        logger.error("Error in loading the classloader",e);
    }
    catch(ClassNotFoundException e){
        logger.error("The class was not found",e);
    }
    catch(IllegalArgumentException e){
    logger.error("Illegal Argument Exception occured",e);
    }
    return Classes;
    }
    
    
    public DeviceFactoryImp() throws DBusException {
        dbusConnect(AGILE_DEVICEFACTORY_BUS_NAME, AGILE_DEVICEFACTORY_BUS_PATH, this);
        logger.debug("Started Device Factory");
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
    List <String> deviceClasses = new ArrayList();
    try{
        File filePath = new File("/home/agile/gitsample/agile-core/iot.agile.DeviceFactory/target/classes/iot/agile/devicefactory/device");
        File[] files = filePath.listFiles();
        for (File file:files)
        {
            if(!(file.getName().contains("$")))
                deviceClasses.add(file.getName().split("\\.")[0]);
        }
        
        logger.debug("Classes are: "+ deviceClasses);
        if(deviceClasses.contains(deviceType))
        {
            ClassLoader classLoader = DeviceFactoryImp.class.getClassLoader();
            logger.debug("Loaded ClassLoader, trying to load iot.agile.devicefactory.device."+deviceType);
            Class aClass = classLoader.loadClass("iot.agile.devicefactory.device."+deviceType);
            logger.debug("The class was loaded");
            Constructor constructor = aClass.getConstructor(DeviceOverview.class);
            logger.debug("The Constructor was loaded");
            device = (Device) constructor.newInstance(deviceOverview);    
            logger.debug("The device was loaded");
        }
        else
            logger.debug("Class was not found in the folder");
    }
    catch(SecurityException e){
        logger.error("Error in loading the classloader",e);
    }
    catch(ClassNotFoundException e){
        logger.error("The class was not found",e);
    }
    catch(NoSuchMethodException e){
        logger.error("The constructor was not found", e);
    }
    catch(IllegalAccessException e){
    logger.error("Illegal Access Exception occured",e);
    }
    catch(IllegalArgumentException e){
    logger.error("Illegal Argument Exception occured",e);
    }
    catch(InstantiationException e){
    logger.error("Instantiation Exception occured",e);
    }

// TODO Enum based device type, not string
//    switch (deviceType) {
//    case "TI SensorTag":
//      device = new TISensorTag(deviceOverview);
//      break;
//    case "Oximeter":
//      device = new MedicalDevice(deviceOverview);
//      break;
//    case "HEXIWEAR":
//      device = new HexiwearDevice(deviceOverview);
//      break;
//    case "Dummy":
//      device = new DummyDevice(deviceOverview);
//      break;
//    default:
//      throw new Exception("Device: " + deviceType + " does not have a matching type ");
//    }
    return device;
  }
  
  
  
  
  public List<String> MatchingDeviceTypes(DeviceOverview deviceOverview) {
		List<String> ret = new ArrayList();
//		if(TISensorTag.Matches(deviceOverview)) {
//			ret.add(TISensorTag.deviceTypeName);
//		}
//		if(MedicalDevice.Matches(deviceOverview)) {
//			ret.add(MedicalDevice.deviceTypeName);
//		}
//		if(DummyDevice.Matches(deviceOverview)){
// 		  ret.add(DummyDevice.deviceTypeName);
//		}
//		if(deviceOverview.name.equals("GE Lamp")) {
//			ret.add("GE Lamp");
//		}
//		if(HexiwearDevice.Matches(deviceOverview)) {
//			ret.add(HexiwearDevice.deviceTypeName);
//		}

		return ret; 
	}
  
  /*
  Override abstract method in DBusInterface
  */
  
  @Override
	public boolean isRemote() {
		return false;
	}
}
