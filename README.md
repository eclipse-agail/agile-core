
Agile components
===

This repository contains the alpha version of the following AGILE components:

Agile Bluetooth Low Energy
---

Exposes the [iot.agile.Protocol](http://agile-iot.github.io/agile-api-spec/docs/html/api.html#iot_agile_Protocol) interface

- DBus interface name **iot.agile.protocol.BLE**
- DBus interface path **/iot/agile/protocol/BLE**

Agile Protocol Manager
---

Exposes the [iot.agile.ProtocolManager](http://agile-iot.github.io/agile-api-spec/docs/html/api.html#iot_agile_ProtocolManager) interface

- DBus interface name **iot.agile.ProtocolManager**
- DBus interface path **/iot/agile/ProtocolManager**

**Example**

The package *iot.agile.protocolmanager.example* contains an example
file (i.e BLEDeviceDiscovery.java) to demonstrate the use of AGILE Protocol Manager to discover BLE devices through DBus interface.

AGILE Device Manager
---

Exposes the [iot.agile.DeviceManager](http://agile-iot.github.io/agile-api-spec/docs/html/api.html#iot_agile_DeviceManager) interface

- DBus interface name **iot.agile.DeviceManger**
- DBus interface path **/iot/agile/DeviceManager**

**Example**

Register BLE Device The package iot.agile.devicemanager.example contains an example file (i.e RegisterDevice.java) to demonstrate the usage of Agile Device Manager for device registration and creation of DBus interface for the registered device

**Additional interfaces**

- [iot.agile.Device](http://agile-iot.github.io/agile-api-spec/docs/html/api.html#iot_agile_Device)
  - DBus interface base name *iot.agile.device.[device name]*
  - DBus interface base path */iot/agile/Device/[device name]*

    **Example**

    - Connect BLE Device(TI SensorTag): The ConnectDevice.java file in iot.agile.devicemanager.example package demonstrates the usage of Agile iot.agile.Device Dbus interface to connect a TI SensorTag device.
    - Read Data: The ReadData.java file in iot.agile.devicemanager.example package demonstrates the usage of iot.agile.Device DBus interface in order to read temperature data from TI SensorTag temperature sensor/service.

Running the interfaces
---

Under the `scripts` directory you can find different scripts to setup and start / stop the modules.

*Note* A `java` 8 compatible JDK and maven (`mvn`) must be already available in the system.

- `./scripts/start.sh` will install (if not available) all the dependencies required to run the modules and start all the modules.
  To control which component to start:
  - `./scripts/start.sh ProtocolManager` start only the Protocol Manager module
  - `./scripts/start.sh DeviceManager` start only the Device Manager module
  - `./scripts/start.sh BLE` start only the BLE Protocol module

- `./scripts/stop.sh` will kill all the modules.
  To control which component to stop (based on `pkill -f` pattern):
  - `./scripts/start.sh protocolmanager` stop only the Protocol Manager module
  - `./scripts/start.sh devicemanager` stop only the Device Manager module
  - `./scripts/start.sh protocol.BLE` stop only the BLE Protocol module

-  `./scripts/install-deps.sh` is used to setup all the java relateed and native dependencies to a local directory (`./deps`)

Development environment setup
---

To run the examples above installation of the following hardwares and libraries are required:
- Hardwares:
      Bluetooth adapter must be turned on to run the examples  
      TI SensorTag
-  Libraries:
      - Add dbus-java2.7.jar on your local maven repository
      - Add tinyb.1.0.0.jar on your local maven repository
           (https://github.com/intel-iot-devkit/tinyb )
      - install libjavatinyb.so shared library  

Execution order:

1. First the BLE Protcol API must run, for this compile and run BLEProtocolImp.java file from iot.agile.protocol.BLE package.

2. Start the Agile protocol manager DBus interface, for this compile and run ProtocolManagerImp.java from iot.agile.ProtocolManager package.

3. Start the device manger DBus interface, by compile and run DeviceManagerImp.java file from iot.agile.DeviceManager package.

4. Compile and run BLEDeviceDiscovery.java file from iot.agile.protocolmanager.example package to discover active BLE devices. This will print the name of all discovered BLE devices.

5. Compile and run RegisterDevice.java file from iot.agile.devicemanager.examples package to register TI SensorTag.
*Prerequisite:* A device must be first discovered before registration.
*Note:* Make sure to set you device address (i.e C4:BE:84:70:69:09) of your TI-Sensor tag.

6. Compile and run ConnectDevice.java from iot.agile.devicemanager.examples package to connect the device (i.e. TI SensorTag)
*Prerequisite:* A device must be registered to be connected.

7. Compile and run ReadData.java from iot.agile.devicemanager.examples package to read current temperature data from the sensor tag.
*Prerequisite:* A device must be connected first.

*Note:* step one to three are all mandatory to run any of the example files provided.
