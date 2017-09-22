#!/bin/sh

ssh agilepi1 'mkdir -p /home/pi/git/agile-core/iot.agile.ProtocolManager/target'
ssh agilepi1 'mkdir -p /home/pi/git/agile-core/iot.agile.DeviceManager/target'
ssh agilepi1 'mkdir -p /home/pi/git/agile-core/iot.agile.DeviceFactory/target'
ssh agilepi1 'mkdir -p /home/pi/git/agile-core/iot.agile.http/target'

scp iot.agile.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar  agilepi1:~/git/agile-core/iot.agile.ProtocolManager/target
scp iot.agile.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar  agilepi1:~/git/agile-core/iot.agile.DeviceManager/target
scp iot.agile.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar  agilepi1:~/git/agile-core/iot.agile.DeviceFactory/target
scp iot.agile.http/target/http-1.0-jar-with-dependencies.jar  agilepi1:~/git/agile-core/iot.agile.http/target
