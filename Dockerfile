#-------------------------------------------------------------------------------
# Copyright (C) 2017 Create-Net / FBK.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Create-Net / FBK - initial API and implementation
#-------------------------------------------------------------------------------

FROM agileiot/raspberry-pi3-zulujdk:8-jre
WORKDIR /usr/src/app

RUN apt-get update
RUN apt-get install --no-install-recommends -y dbus-x11 && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN mkdir -p iot.agile.ProtocolManager/target
RUN mkdir -p iot.agile.DeviceManager/target
RUN mkdir -p iot.agile.DeviceFactory/target
RUN mkdir -p iot.agile.DeviceFactory

COPY ./scripts scripts

COPY ./iot.agile.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar iot.agile.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar
COPY ./iot.agile.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar iot.agile.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar
COPY ./iot.agile.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar iot.agile.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar
COPY ./iot.agile.http/target/http-1.0-jar-with-dependencies.jar iot.agile.http/target/http-1.0-jar-with-dependencies.jar
COPY ./iot.agile.DeviceFactory/target/classes/iot/agile/device/instance iot.agile.DeviceFactory/target/classes/iot/agile/device/instance

CMD [ "bash", "/usr/src/app/scripts/start.sh" ]
