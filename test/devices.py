#!/usr/bin/env python3

# --- Imports -----------
import sys
import time
import dbus
# -----------------------

# DBus
DM_BUS_NAME = "iot.agile.DeviceManager"
DM_OBJ_PATH = "/iot/agile/DeviceManager"

# --- Main program ------
if __name__ == "__main__":

   global device_manager

   # DBus
   session_bus = dbus.SessionBus()
   dbusdm = session_bus.get_object(DM_BUS_NAME, DM_OBJ_PATH)
   device_manager = dbus.Interface(dbusdm, dbus_interface=DM_BUS_NAME)

   print (device_manager.Devices())
