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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclipse.agail.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.agail.devicemanager.DeviceManagerImp;
import org.eclipse.agail.http.HttpServer;
import org.eclipse.agail.protocolmanager.ProtocolManagerImp;

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
