package iot.agile.devicefactory;

import static ch.qos.logback.core.util.Loader.loadClass;
import iot.agile.Device;
import iot.agile.DeviceFactory;
import iot.agile.object.AbstractAgileObject;
import iot.agile.object.DeviceOverview;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import java.util.logging.Level;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import iot.agile.devicefactory.LoadClass;

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
     * The path of the directory where the classes are loaded from
     */
    private static final String CLASSPATH_DIR = "/home/agile/gitsample/agile-core/iot.agile.DeviceFactory/target/classes/iot/agile/devicefactory/device";
    
    /**
     * The directory where .class files can be dropped
     */
    private static final String ADDCLASS_DIR = "/home/agile/gitsample/agile-core/iot.agile.DeviceFactory/target/classes/iot/agile/devicefactory";
    /**
     * WatchService object to observe directory for changes
     */
    private WatchService watcher;

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
           
    }

    /**
     * Load all the classes, from the specified directory, in the HashMap Classes 
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

    private void registerDir() {
        Path dir = FileSystems.getDefault().getPath(ADDCLASS_DIR);
        try {
            //Initialize the watcher for the directory
            watcher = FileSystems.getDefault().newWatchService();
            //Add the types of WatchEvent for the directory
            WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);

        } catch (IOException e) {
            logger.error("IO Exception occured", e);
        }

    }

    private void watchChanges() {
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
                                                          
                            File directory = new File(ADDCLASS_DIR);
                            try{

                                URL[] pathToClass = {directory.toURI().toURL()};
                                URLClassLoader URLLoader = new URLClassLoader(pathToClass);
                                //Failed attempt to load the class directly
//                                logger.error("Attempted URL is "+pathToClass[0].getPath()+file.getFileName().toString().split("\\.")[0]);
//                                Class aClass = URLLoader.loadClass(file.getFileName().toString());
//                                Classes.put(file.getFileName().toString().split("\\.")[0], aClass);
                                
                                //Class loaded as an InputStream and converted to bytes
                                InputStream fileInputStream = URLLoader.getResourceAsStream(file.getFileName().toString());
                                byte[] rawBytes = new byte[fileInputStream.available()];
                                fileInputStream.read(rawBytes);
                                
                                //Create an instance of LoadClass to access protected method defineClass
                                LoadClass loader = new LoadClass();
                                Class<?> recoveredClass = loader.getClassFromBytes(file.getFileName().toString().split("\\.")[0], rawBytes);
                                logger.debug("Filename is "+file.getFileName().toString().split("\\.")[0]);
                                
                                if(Classes.get(file.getFileName().toString().split("\\.")[0])==null)
                                {
                                    Classes.put(file.getFileName().toString().split("\\.")[0], recoveredClass);
                                    logger.debug("Added "+file.getFileName().toString());
                                }
                            
                                else
                                    logger.debug("Class already loaded in the list of classes");
                                                              
                                
                            }
                            catch(MalformedURLException e){
                                logger.error("The argument passed is a malformed URL",e);
                            }
//                            catch(ClassNotFoundException e){
//                                logger.error("The class was not found", e);
//                            }
                            catch(NullPointerException e){
                                logger.error("Null pointer exception occured", e);
                            }
                            catch(IOException e){
                                logger.error("IO exception occured", e);
                            }
                            catch(ClassFormatError e){
                                logger.error("ClassFormatError occured", e);
                            }

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
        
        //Create the Service Watcher and Register the watcher
        registerDir();

        //Start the loop on a thread
        watchChanges();
        
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

        List<String> ret = new ArrayList();
        
        Class[] methodParams = {DeviceOverview.class};
        try
        {
        for (HashMap.Entry<String, Class> entry : Classes.entrySet()) {
                    //Need to remove the following line for the devices (Check which are required where)
                    if(!(entry.getKey().matches("AgileBLEDevice"))&&!(entry.getKey().matches("DeviceImp"))&&!(entry.getKey().matches("SensorUuid")))
                    {
                    logger.debug("Key = " + entry.getKey() + ", Value = " + entry.getValue().getName());
                    //Get the Class object
                    Class aClass = entry.getValue();
//                    logger.debug("Class name is "+(String) aClass.getField("deviceTypeName").get(aClass));
                    //Get the 'Matches' method from the class
                    Method matches = aClass.getDeclaredMethod("Matches", methodParams);
//                    logger.debug(matches.getName());
                    //Call the Matches method with argument deviceOverview, first argument is null since the method is static
                    if((Boolean)(matches.invoke(null, deviceOverview)))
                    {
                        String name = (String) aClass.getField("deviceTypeName").get(aClass);
                        ret.add(name);
                    }
                    }
                }
            
        }
        
        catch(NoSuchMethodException e)
        {
            logger.error("No such method exception occured",e);
        }
        catch(IllegalAccessException e)
        {
            logger.error("Method accessed illegally",e);
        }
        catch(InvocationTargetException e)
        {
            logger.error("Exception from method invoked", e);
        } catch (NoSuchFieldException e) 
        {
            logger.error("The field does not exist", e);
        } catch (SecurityException e) 
        {
            logger.error("Security exception occured", e);
        }
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
