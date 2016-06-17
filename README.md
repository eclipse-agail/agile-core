 This repository contains the alpha version of the following AGILE components:

	- iot.agile.protocol.BLE:
               DBus interface name = "iot.agile.protocol.BLE"
							 DBus interface path = "/iot/agile/protocol/ble";
               Label: Agile Bluetooth Low Energy
               Description: allows clients to discover, connect, and disconnect
							 BLE devices(All implemented). Moreover, provides methods to write
							 and read into/from BLE Devices.
	- iot.agile.ProtcolManager
               DBus interface name = "iot.agile.ProtocolManager"
               DBus interface path = "/iot/agile/ProtocolManager";
               Label: Agile Protocol Manager
               Description: Provide a DBus interface to instantiate device
							 discovery (Implimented for BLE),and to add and remove protocols.
               Example: BLE Device Discovery
                 The package iot.agile.protocolmanager.example contains an example
                 file (i.e BLEDeviceDiscovery.java) to demonstrate the use of AGILE Protocol Manager
                 to discover BLE devices through DBus interface.
	- iot.agile.DeviceManager
	       DBus interface name = "iot.agile.DeviceManger"
               DBus interface path = "/iot/agile/DeviceManager";
               Label: AGILE Device Manager
               Description: Allows clients to create/register a new device
							 object in the device manager
               Example: Register BLE Device
                    The package iot.agile.devicemanager.example contains an example file(i.e RegisterDevice.java)
                    to demonstrate the usage of Agile Device Manager for device registration and creation of DBus interface for the registered device   
  	- AGILE Device (iot.agile.Device)
               DBus interface base name = "iot.agile.device"
               DBus interface base path = "/iot/agile/Device/";
               Label: AGILE Device Manager
               Description: Allows clients to connect and disconnect devices
							 (both implemented for BLE), and to read/write data from/to the
							  device and to subcribe for notification.
               Example One:Connect BLE Device(TI SensorTag)
                     The ConnectDevice.java file in iot.agile.devicemanager.example package demonstrates the usage of
                       Agile iot.agile.Device Dbus interface to connect a TI SensorTag device.
                Example two: Read Data
                     The ReadData.java file in iot.agile.devicemanager.example package demonstrates the usage of iot.agile.Device DBus interface in order to read temperature data from TI SensorTag temperature sensor/service.
