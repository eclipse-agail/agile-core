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


# --- Main program ------
if __name__ == "__main__":

   global protocol_manager
   global device_manager

   # DBus
   session_bus = dbus.SessionBus()
   dbuspm = session_bus.get_object(PM_BUS_NAME, PM_OBJ_PATH)
   protocol_manager = dbus.Interface(dbuspm, dbus_interface=PM_BUS_NAME)

   protocol_manager.StartDiscovery()
   time.sleep(5)
   devices = protocol_manager.Devices()
   protocol_manager.StopDiscovery()
   print (devices)

   dbusdm = session_bus.get_object(DM_BUS_NAME, DM_OBJ_PATH)
   device_manager = dbus.Interface(dbusdm, dbus_interface=DM_BUS_NAME)

   for device in devices:
     if (device[2] == 'SensorTag'):
       print (device_manager.Create((device[0], device[1], device[2], '',[('Temperature','celsius')])))

   print (device_manager.devices())


#   print (device_manager.devices())

#   print (device_manager.Create(('78:C5:E5:6E:E4:CF', 'iot.agile.protocol.BLE', 'SensorTag', '',[('Temperature','celsius')])))

#   print (device_manager.devices())

