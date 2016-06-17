 This repository contains the alpha version of the following AGILE components:

    iot.agile.protocol.BLE:
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
                    The package iot.agile.devicemanager.example contains an example
                    file (i.e RegisterDevice.java) to demonstrate the usage of Agile Device Manager
                     for device registration and creation of DBus interface for the registered device   
  	- AGILE Device (iot.agile.Device)
               DBus interface base name = "iot.agile.device"
               DBus interface base path = "/iot/agile/Device/";
               Label: AGILE Device Manager
               Description: Allows clients to connect and disconnect devices
							 (both implemented for BLE), and to read/write data from/to the
							  device and to subcribe for notification.
               Example One:Connect BLE Device(TI SensorTag)
                     The ConnectDevice.java file in iot.agile.devicemanager.example package demonstrates
                     the usage of Agile iot.agile.Device Dbus interface to connect a TI SensorTag device.
                Example two: Read Data
                     The ReadData.java file in iot.agile.devicemanager.example package demonstrates the
                     usage of iot.agile.Device DBus interface in order to read temperature data from
                     TI SensorTag temperature sensor/service.

  Prerequisite:

       To run the examples above installation of the following hardwares and libraries are required:

          - Hardwares:
                Bluetooth adapter must be turned on to run the examples(or at least to get the expected outcomes).
                TI SensorTag
          -  Libraries:


Execution order:

          - Step one: First the BLE Protcol API must run, for this compile and run BLEProtocolImp.java file
                      from iot.agile.protocol.BLE package.
          -  Step two: Start the Agile protocol manager DBus interface, for this compile and run
                      ProtocolManagerImp.java from iot.agile.ProtocolManager package.
          -  Step three: Start the device manger DBus interface, by compile and run DeviceManagerImp.java
                       file from iot.agile.DeviceManager package.
          -  Step four: Compile and run BLEDeviceDiscovery.java file from iot.agile.protocolmanager.example
                       package to discover active BLE devices. This will print the name of all discovered
                        BLE devices.
          -  Step five: Compile and run RegisterDevice.java file from iot.agile.devicemanager.examples package
                       to register TI SensorTag.
                       Prerequisite: A device must be first discovered before registration.
                       Note: Make sure the hard-coded device address(i.e C4:BE:84:70:69:09) is the correct for
                             your TI-Sensor tag, if not change it.
          -  Step six: Compile and run ConnectDevice.java from iot.agile.devicemanager.examples package to connect
                      the device (i.e. TI SensorTag)
                      Prerequisite: A device must be registered to be connected.
          -  Step seven: Compile and run ReadData.java from iot.agile.devicemanager.examples package to read current
                       temperature data from the sensor tag.
                        Prerequisite : A device must be connected first.

    Note 1: step one to three are all mandatory to run any of the example files provided.
   
