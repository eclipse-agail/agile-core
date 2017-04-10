#!/usr/bin/env python3

# --- Imports -----------
import sys
import time
import dbus
# -----------------------

# DBus
PM_BUS_NAME = "iot.agile.ProtocolManager"
PM_OBJ_PATH = "/iot/agile/ProtocolManager"
DM_BUS_NAME = "iot.agile.DeviceManager"
DM_OBJ_PATH = "/iot/agile/DeviceManager"
D_BUS_NAME = "iot.agile.Device"
D_OBJ_PATH = "/iot/agile/Device"
DF_BUS_NAME = "iot.agile.DeviceFactory"
DF_OBJ_PATH = "/iot/agile/DeviceFactory"


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

   dbusdf = session_bus.get_object(DF_BUS_NAME, DF_OBJ_PATH)
   device_factory = dbus.Interface(dbusdf, dbus_interface=DF_BUS_NAME)

   sensortags = []
   for device in devices:
     print(device)
     types = device_factory.MatchingDeviceTypes(device)
     print("\nMatched types are ")
     print(types)
     if len(types) == 1 :
       sensortag = device_manager.Register(device, types[0])
       print (sensortag)
       sensortags.append(sensortag)

   for sensortag in sensortags:
     print (sensortag[0])
