#!/bin/sh

MODULE=${1:-all}
DEPS=`realpath ./deps`

if [ ! -e "$DEPS" ]; then
  echo "Installing dependencies"
  ./scripts/install-deps.sh
fi

echo "DEPS dir $DEPS"

if [ "${MODULE}" = 'all' ]; then
  echo "To start a single module use:\n $0 DeviceManager\|ProtocolManager\|BLE"
fi

export MAVEN_OPTS="-Djava.library.path=$DEPS"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DEPS:$DEPS/lib

if [ $MODULE = 'all' ] || [ $MODULE = 'BLE' ]; then
  ./scripts/stop.sh "protocol.BLE"
  cd iot.agile.protocol.BLE
  mvn clean install exec:java &
  echo "Started AGILE BLE protocol"
  cd ../
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'ProtocolManager' ]; then
  ./scripts/stop.sh "protocolmanager"
  cd iot.agile.ProtocolManager
  mvn clean install exec:java &
  echo "Started AGILE Protocol Manager"
  cd ..
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'DeviceManager' ]; then
  ./scripts/stop.sh "devicemanager"
  cd iot.agile.DeviceManager
  mvn clean install exec:java &
  echo "Started AGILE Device Manager"
  cd ..
fi
