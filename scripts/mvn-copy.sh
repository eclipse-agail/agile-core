#!/bin/sh

HOST=$1

if [ "$HOST" = "" ]
then
    echo "Provide a PI host destination as argument"
    exit 1
fi

ssh $HOST 'mkdir -p /home/pi/git/agile-core/iot.agile.ProtocolManager/target'
ssh $HOST 'mkdir -p /home/pi/git/agile-core/iot.agile.DeviceManager/target'
ssh $HOST 'mkdir -p /home/pi/git/agile-core/iot.agile.DeviceFactory/target'
ssh $HOST 'mkdir -p /home/pi/git/agile-core/iot.agile.http/target'

scp iot.agile.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar  $HOST:~/git/agile-core/iot.agile.ProtocolManager/target
scp iot.agile.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar  $HOST:~/git/agile-core/iot.agile.DeviceManager/target
scp iot.agile.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar  $HOST:~/git/agile-core/iot.agile.DeviceFactory/target
scp iot.agile.http/target/http-1.0-jar-with-dependencies.jar  $HOST:~/git/agile-core/iot.agile.http/target


scp -r ../../jnr/jffi/archive $HOST:~/git/agile-core/
