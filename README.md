
Agile core components
===

This repository contains the alpha version of the following AGILE components:

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

HTTP API
---

The DBus API can be used from the HTTP endpoint exposed by default on port `8080`.

See the [HTTP API reference](http://petstore.swagger.io/?url=http://agile-iot.github.io/agile-api-spec/docs/swagger/api.swagger.yml) for details on the exposed endpoints.

To query the endpoint, we suggest using [Postman](https://www.getpostman.com/) by importing the swagger based [API definition](http://agile-iot.github.io/agile-api-spec/docs/swagger/api.swagger.yml)

Launching the API
---

Under the `scripts` directory you can find different scripts to setup and start / stop the modules.

*Note* A `java` 8 compatible JDK and maven (`mvn`) must be already available in the system.

- `./scripts/start.sh` will install (if not available) all the dependencies required to run and start the modules.

  To control which component to start:
  - `./scripts/start.sh ProtocolManager` starts only the Protocol Manager module
  - `./scripts/start.sh DeviceManager` starts only the Device Manager module
  - `./scripts/start.sh http` starts only the HTTP API module

- `./scripts/stop.sh` will kill all the modules.

  To control which component to stop (based on `pkill -f` pattern):
  - `./scripts/stop.sh protocolmanager` stop only the Protocol Manager module
  - `./scripts/stop.sh devicemanager` stop only the Device Manager module
  - `./scripts/stop.sh http` stop only the HTTP API module

-  `./scripts/install-deps.sh` is used to setup all the java and native dependencies to a local directory (placed in the `./deps` folder)

Development environment setup
---
