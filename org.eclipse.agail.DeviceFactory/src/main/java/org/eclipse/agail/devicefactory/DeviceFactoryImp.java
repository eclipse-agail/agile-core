/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/
package org.eclipse.agail.devicefactory;

import org.eclipse.agail.device.base.LoadClass;
import org.eclipse.agail.Device;
import org.eclipse.agail.DeviceFactory;
import org.eclipse.agail.object.AbstractAgileObject;
import org.eclipse.agail.object.DeviceOverview;
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
import java.util.List;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
/**
 * Agile device factory
 *
 * @author dagi, koustabhdolui
 *
 */
public class DeviceFactoryImp extends AbstractAgileObject implements DeviceFactory {

    protected static Logger logger = LoggerFactory.getLogger(DeviceFactoryImp.class);

    /**
     * Bus name for the device factory
     */
    private static final String AGILE_DEVICEFACTORY_BUS_NAME = "org.eclipse.agail.DeviceFactory";
    /**
     * Bus path for the device factory
     */
    private static final String AGILE_DEVICEFACTORY_BUS_PATH = "/org/eclipse/agail/DeviceFactory";
    /**
     * The path of the directory of the classpath base
     */
    private static final String CLASSPATH_BASE = "classes";
    /**
     * The path of the directory where the classes are loaded from
     */
    private static final String DEVICE_CLASSPATH = "org/eclipse/agail/device/instance";
    
    /**
     * The directory where .class files can be dropped
     */
    private static String ADDCLASS_DIR = null ;

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

        
        if(args.length==1)
        {
            ADDCLASS_DIR = args[0];
            
            logger.debug("Drop new classfiles to "+ADDCLASS_DIR);
            
            deviceFactory.registerDir();
            
            deviceFactory.watchChanges();
        }
        
        
        else
            logger.debug("No path specified to load classes dynamically.");
        
        
        //Load the classes from the specified directory into Classes variable
        loadAllClasses();
    }

    /**
     * Load all the classes, from the specified directory, in the HashMap Classes 
     */
    private static void loadAllClasses() {

        //Absolute path for the location of the classes in classpath
        File filePath = new File(getDir()+CLASSPATH_BASE+"/"+DEVICE_CLASSPATH);
        File[] files = filePath.listFiles();

        //For each file in the directory, load the class and add to the HashMap

        try{
            for (File file : files) {
  		  
                if (!(file.getName().contains("$"))) {		              
                    loadOneClass(file.getName());		                  
                }		              
             
            }
  		  
        
        
        logger.debug("Getting files from"+ADDCLASS_DIR);
        
        //Load all files from the addition directory
        if(ADDCLASS_DIR!=null)
        {   
            
            filePath = new File(ADDCLASS_DIR+"/"+DEVICE_CLASSPATH);
            files = filePath.listFiles();

        //For each file in the directory, load the class and add to the HashMap
        for (File file : files) {

            if (!(file.getName().contains("$"))) {
                loadOneClass(file.getName());
            }

        }
        }
        
        }		         
         
        catch(NullPointerException ex){
            logger.error("NullPointer exception occured"+ex);
        }

    }

    
    private static void loadOneClass(String filename) {
        try {
            //Get the classloader from the current class
            ClassLoader classLoader = DeviceFactoryImp.class.getClassLoader();
            logger.debug("Loaded ClassLoader, trying to load org.eclipse.agail.device.instance." + filename.split("\\.")[0]);

            //Load the class defined by the file name
            Class aClass = classLoader.loadClass("org.eclipse.agail.device.instance." + filename.split("\\.")[0]);
            logger.debug("The class was loaded");

            //Add only name of the class to the HashMap
            Classes.put(filename.split("\\.")[0], aClass);
        } 
        catch (SecurityException e) {
            logger.error("Error in loading the classloader", e);
        } 
        catch (ClassNotFoundException e) {
            logger.error("The class was not found", e);
        } 
        catch (IllegalArgumentException e) {
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

        } 
        catch (IOException e) {
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
                    } 
                    catch (InterruptedException e) {
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
                            try {

                                URL[] pathToClass = {directory.toURI().toURL()};
                                URLClassLoader URLLoader = new URLClassLoader(pathToClass);
                                

                                  //Using the URLClassLoader, place the new file in the directory /org/eclipse/agail/devicefactory/device
//                                logger.error("Attempted URL is "+pathToClass[0].getPath()+file.getFileName().toString().split("\\.")[0]);
//                                Class aClass = URLLoader.loadClass("org.eclipse.agail.device.instance."+file.getFileName().toString());
//                                if(Classes.get(file.getFileName().toString().split("\\.")[0])==null)
//                                {
//                                    Classes.put(file.getFileName().toString().split("\\.")[0], aClass);
//                                    logger.debug("Added "+file.getFileName().toString());
//                                }
                                
                                //Class loaded as an InputStream and converted to bytes
                                InputStream fileInputStream = URLLoader.getResourceAsStream(file.getFileName().toString());
                                byte[] rawBytes = new byte[fileInputStream.available()];
                                fileInputStream.read(rawBytes);
                                
                                //Create an instance of LoadClass to access protected method defineClass
                                LoadClass loader = new LoadClass();
                                //Add the full name of the class with package to match that from the binary
                                Class<?> recoveredClass = loader.getClassFromBytes("org.eclipse.agail.device.instance."+file.getFileName().toString().split("\\.")[0], rawBytes);
                                
                                //Check if the class is already in the list of classes
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
                              //Uncomment to use the URLClassLoader
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
                String name = (String) entry.getValue().getField("deviceTypeName").get(entry.getValue());
                if (name.equals(deviceType)) {
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

                logger.debug("Key = " + entry.getKey() + ", Value = " + entry.getValue().getName());
                //Get the Class object
                Class aClass = entry.getValue();
                //Get the 'Matches' method from the class
                Method matches = aClass.getDeclaredMethod("Matches", methodParams);
                //Call the Matches method with argument deviceOverview, first argument is null since the method is static
                if((Boolean)(matches.invoke(null, deviceOverview)))
                {
                    String name = (String) aClass.getField("deviceTypeName").get(aClass);
                    ret.add(name);
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
    
    /**
     * Get the working directory of the current class to use as a relative path for loading classes
     */
    private static String getDir(){
        //Path to the jar file
        String classDir = DeviceFactoryImp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        //Truncate the path eliminating the jar filename
        classDir = classDir.replaceAll(classDir.substring(classDir.lastIndexOf("/")+1),"");
        return classDir;
    }

    /*
  Override abstract method in DBusInterface
     */
    @Override
    public boolean isRemote() {
        return false;
    }
}
