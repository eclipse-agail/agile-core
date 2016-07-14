/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iot.agile.main;

import iot.agile.devicemanager.DeviceManagerImp;
import iot.agile.http.HttpServer;
import iot.agile.protocol.ble.BLEProtocolImp;
import iot.agile.protocolmanager.ProtocolManagerImp;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class Launcher {
  
  static final Logger logger = LoggerFactory.getLogger(Launcher.class);
  
  public static void main(String[] args) {
    (new Launcher()).start();
  }
  
  public void start() {
    
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    
    executor.submit(() -> {
      try {
        new ProtocolManagerImp();
      } catch (DBusException ex) {
        logger.error("ProtocolManagerImp error", ex);
      }
    });
    
    executor.submit(() -> {
      try {
        new DeviceManagerImp();
      } catch (DBusException ex) {
        logger.error("DeviceManagerImp error", ex);
      }
    });
    
//    executor.submit(() -> {
//      try {
//        new BLEProtocolImp();
//      } catch (DBusException ex) {
//        logger.error("BLEProtocolImp error", ex);
//      }
//    });
    
    
    executor.submit(() -> {
      try {
        (new HttpServer()).launch();
      } catch (Exception ex) {
        logger.error("HttpServer error", ex);
      }
    });
    
  }
}
