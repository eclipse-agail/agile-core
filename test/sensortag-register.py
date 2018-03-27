#!/usr/bin/env python3
#-------------------------------------------------------------------------------
# Copyright (C) 2017 Create-Net / FBK.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
# 
# SPDX-License-Identifier: EPL-2.0
# 
# Contributors:
#     Create-Net / FBK - initial API and implementation
#-------------------------------------------------------------------------------

# --- Imports -----------
import sys
import time
import dbus
# -----------------------

# DBus
PM_BUS_NAME = "org.eclipse.agail.ProtocolManager"
PM_OBJ_PATH = "/org/eclipse/agail/ProtocolManager"
DM_BUS_NAME = "org.eclipse.agail.DeviceManager"
DM_OBJ_PATH = "/org/eclipse/agail/DeviceManager"
D_BUS_NAME = "org.eclipse.agail.Device"
D_OBJ_PATH = "/org/eclipse/agail/Device"


# --- Main program ------
if __name__ == "__main__":

   global protocol_manager
   global device_manager

   # DBus
   session_bus = dbus.SessionBus()
   dbuspm = session_bus.get_object(PM_BUS_NAME, PM_OBJ_PATH)
   protocol_manager = dbus.Interface(dbuspm, dbus_interface=PM_BUS_NAME)
   

#   protocol_manager.StartDiscovery()
#   time.sleep(5)
   devices = protocol_manager.Devices()
#   protocol_manager.StopDiscovery()
   print ("\nRegistered devices ")
   print (devices)

   dbusdm = session_bus.get_object(DM_BUS_NAME, DM_OBJ_PATH)
   device_manager = dbus.Interface(dbusdm, dbus_interface=DM_BUS_NAME)

   sensortags = []
   for device in devices:
     print(device)
     types = device_manager.MatchingDeviceTypes(device)
     print("\nMatched types are ")
     print(types)
     if len(types) == 1 :
       sensortag = device_manager.Register(device, types[0])
       print (sensortag)
       sensortags.append(sensortag)

   for sensortag in sensortags:
     print (sensortag[0])
