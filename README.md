<!--
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
-->

Agile core components
===

This repository contains the alpha version of the following AGILE components:

Prerequisites
---

For the compilation, the following packages needs to be installed:
- gettext git cmake

A `java` 8 compatible JDK and maven (`mvn`) must be already available in the system.

JAVA_HOME must be set for compilation to work.

Agile Protocol Manager
---

Exposes the [org.eclipse.agail.ProtocolManager](http://agile-iot.github.io/agile-api-spec/docs/html/api.html#iot_agile_ProtocolManager) interface

- DBus interface name **org.eclipse.agail.ProtocolManager**
- DBus interface path **/org/eclipse/agail/ProtocolManager**

**Example**

The package *org.eclipse.agail.protocolmanager.example* contains an example
file (i.e BLEDeviceDiscovery.java) to demonstrate the use of AGILE Protocol Manager to discover BLE devices through DBus interface.

AGILE Device Manager
---

Exposes the [org.eclipse.agail.DeviceManager](http://agile-iot.github.io/agile-api-spec/docs/html/api.html#iot_agile_DeviceManager) interface

- DBus interface name **org.eclipse.agail.DeviceManger**
- DBus interface path **/org/eclipse/agail/DeviceManager**

**Example**

Register BLE Device The package org.eclipse.agail.devicemanager.example contains an example file (i.e RegisterDevice.java) to demonstrate the usage of Agile Device Manager for device registration and creation of DBus interface for the registered device

**Additional interfaces**

- [org.eclipse.agail.Device](http://agile-iot.github.io/agile-api-spec/docs/html/api.html#iot_agile_Device)
  - DBus interface base name *org.eclipse.agail.device.[device name]*
  - DBus interface base path */org/eclipse/agail/Device/[device name]*

    **Example**

    - Connect BLE Device(TI SensorTag): The ConnectDevice.java file in org.eclipse.agail.devicemanager.example package demonstrates the usage of Agile org.eclipse.agail.Device Dbus interface to connect a TI SensorTag device.
    - Read Data: The ReadData.java file in org.eclipse.agail.devicemanager.example package demonstrates the usage of org.eclipse.agail.Device DBus interface in order to read temperature data from TI SensorTag temperature sensor/service.

AGILE Device Factory
---

Exposes the [org.eclipse.agail.DeviceFactory](http://agile-iot.github.io/agile-api-spec/docs/html/api.html#iot_agile_DeviceFactory) interface

- DBus interface name **org.eclipse.agail.DeviceFactory**
- DBus interface path **/org/eclipse/agail/DeviceFactory**

The DeviceFactory can also load device implementation from a plugin folder. This plugin folder should be part of the classpath and also specified as the first parameter to the DeviceFactory.
Class files with device implementation must implement the org.eclipse.egail.Device interface, and should be placed directly in the org.eclipse.agail.device.instance package. Class files must be placed in the plugin folder structured in a folder hierarchy corresponding to the package, i.e. the usual Java way: <pluginfolder>/org/eclipse/agail/device/instance/MyNewAgileDevice.class.

HTTP API
---

The DBus API can be used from the HTTP endpoint exposed by default on port `8080`.

See the [HTTP API reference](http://petstore.swagger.io/?url=http://agile-iot.github.io/agile-api-spec/docs/swagger/api.swagger.yml) for details on the exposed endpoints.

To query the endpoint, we suggest using [Postman](https://www.getpostman.com/) by importing the swagger based [API definition](http://agile-iot.github.io/agile-api-spec/docs/swagger/api.swagger.yml)

Launching the API
---

Under the `scripts` directory you can find different scripts to setup and start / stop the modules.

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
