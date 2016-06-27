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

echo "Launching Dbus"
Xvfb :1 -screen 0 1x1x8 &
export DISPLAY=:1
export `dbus-launch`


export MAVEN_OPTS="-Djava.library.path=$DEPS -DDISPLAY=$DISPLAY"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DEPS:$DEPS/lib

if [ $MODULE = 'all' ] || [ $MODULE = 'BLE' ]; then
  ./scripts/stop.sh "protocol.BLE"
  cd iot.agile.protocol.BLE
  DISPLAY=:1 mvn exec:java &
  echo "Started AGILE BLE protocol"
  cd ../
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'ProtocolManager' ]; then
  ./scripts/stop.sh "protocolmanager"
  cd iot.agile.ProtocolManager
  DISPLAY=:1 mvn exec:java &
  echo "Started AGILE Protocol Manager"
  cd ..
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'DeviceManager' ]; then
  ./scripts/stop.sh "devicemanager"
  cd iot.agile.DeviceManager
  DISPLAY=:1 mvn exec:java &
  echo "Started AGILE Device Manager"
  cd ..
fi
