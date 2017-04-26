package iot.agile.devicefactory;

import static ch.qos.logback.core.util.Loader.loadClass;
import iot.agile.Device;
import iot.agile.DeviceFactory;
import iot.agile.object.AbstractAgileObject;
import iot.agile.object.DeviceOverview;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    protected static Logger logger = LoggerFactory.getLogger(DeviceFactoryImp.class);

    /**
     * Bus name for the device factory
     */
    private static final String AGILE_DEVICEFACTORY_BUS_NAME = "iot.agile.DeviceFactory";
    /**
     * Bus path for the device factory
     */
    private static final String AGILE_DEVICEFACTORY_BUS_PATH = "/iot/agile/DeviceFactory";
    /**
     * The path of the directory where the classes are stored
     */
    private static final String CLASSPATH_DIR = "/home/agile/gitsample/agile-core/iot.agile.DeviceFactory/target/classes/iot/agile/devicefactory/device";
    
    /**
     * WatchService object to observe directory for changes
     */
    private static WatchService watcher;

    /**
     * The HashMap of loaded classes
     */
    private static HashMap<String, Class> Classes = new HashMap<String, Class>();

    /**
     * main method to instantiate device factory
     *
     * @param args
     */
    public static void main(String[] args) throws DBusException {
        DeviceFactoryImp deviceFactory = new DeviceFactoryImp();

        //Load the classes from the specified directory into Classes variable
        loadAllClasses();

        //Create the Service Watcher and Register the watcher
        registerDir();

        //Start the loop on a thread
        watchChanges();
    }

    /*
    *Load all the classes, from the specified directory, in a HashMap and return the HashMap 
     */
    private static void loadAllClasses() {

        //Absolute path for the location of the classes
        File filePath = new File(CLASSPATH_DIR);
        File[] files = filePath.listFiles();

        //For each file in the directory, load the class and add to the HashMap
        for (File file : files) {

            if (!(file.getName().contains("$"))) {
                loadOneClass(file.getName());
            }

        }

    }
    
    private static void loadOneClass(String filename) {
        try {
            //Get the classloader from the current class
            ClassLoader classLoader = DeviceFactoryImp.class.getClassLoader();
            logger.debug("Loaded ClassLoader, trying to load iot.agile.devicefactory.device." + filename.split("\\.")[0]);

            //Load the class defined by the file name
            Class aClass = classLoader.loadClass("iot.agile.devicefactory.device." + filename.split("\\.")[0]);
            logger.debug("The class was loaded");

            //Add only name of the class to the HashMap
            Classes.put(filename.split("\\.")[0], aClass);
        } catch (SecurityException e) {
            logger.error("Error in loading the classloader", e);
        } catch (ClassNotFoundException e) {
            logger.error("The class was not found", e);
        } catch (IllegalArgumentException e) {
            logger.error("Illegal Argument Exception occured", e);
        }
    }

    private static void registerDir() {
        Path dir = FileSystems.getDefault().getPath(CLASSPATH_DIR);
        try {
            //Initialize the watcher for the directory
            watcher = FileSystems.getDefault().newWatchService();
            //Add the types of WatchEvent for the directory
            WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);

        } catch (IOException e) {
            logger.error("IO Exception occured", e);
        }

    }

    private static void watchChanges() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WatchKey key;
                for (;;) {
                    try {
                        //Fetch the key from the watcher queue
                        key = watcher.take();
                    } catch (InterruptedException e) {
                        logger.error("Interrupted Exception occured", e);
                        return;
                    }

                    //From the pollEvents method, we get all the events for the key that are signalled
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        //Check for the Overflow event for lost or discarded events
                        if (kind == OVERFLOW) {
                            continue;
                        }

                        if (kind == ENTRY_CREATE) {
                            //Load the classfile and add to the list of loaded classes
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path file = ev.context();
                            if(Classes.get(file.getFileName().toString().split("\\.")[0])==null)
                            {
                                loadOneClass(file.getFileName().toString());
                                logger.debug("Added "+file.getFileName().toString());
                            }
                            
                            else
                                logger.debug("Class already loaded in the list of classes");
//                              
                            

                        } 
                        
                        else if (kind == ENTRY_DELETE) {
                            //Remove the class from the list of loaded classes
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path file = ev.context();
                            Class removedClass = Classes.remove(file.getFileName().toString().split("\\.")[0]);
                            if(removedClass==null)
                                logger.debug("Could not find "+file.getFileName().toString().split("\\.")[0]+" in list of Classes" );
                            else
                                logger.debug("Deleted "+removedClass.getName()+" from the list of Classes");
                        }

                        
                    }

                    boolean valid = key.reset();

                    if (!valid) {
                        break;
                    }

                }
            }
        }).start();
    }


    public DeviceFactoryImp() throws DBusException {
        dbusConnect(AGILE_DEVICEFACTORY_BUS_NAME, AGILE_DEVICEFACTORY_BUS_PATH, this);
        logger.debug("Started Device Factory");
    }

    /**
     * Get device based on device type
     *
     * @param deviceType device type
     * @param deviceOverview device overview
     * @return
     * @throws Exception
     */
    public Device getDevice(String deviceType, DeviceOverview deviceOverview) throws Exception {
        Device device = null;
        try {
            for (HashMap.Entry<String, Class> entry : Classes.entrySet()) {
                if (entry.getKey().equals(deviceType)) {
                    logger.debug("Key = " + entry.getKey() + ", Value = " + entry.getValue().getName());
                    Class aClass = entry.getValue();
                    Constructor constructor = aClass.getConstructor(DeviceOverview.class);
                    logger.debug("The Constructor was loaded");
                    device = (Device) constructor.newInstance(deviceOverview);
                    logger.debug("The device was loaded");
                }

            }
        } catch (InstantiationException e) {
            logger.error("Instantiation Exception occured", e);
        }

        return device;
    }

    public List<String> MatchingDeviceTypes(DeviceOverview deviceOverview) {

        Iterator it = Classes.entrySet().iterator();

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
