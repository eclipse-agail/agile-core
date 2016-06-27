#!/bin/sh

DEPS=`basedir ./deps`

if [ ! -e "$DEPS" ] then
  echo "Installing dependencies"
  ./scripts/install-deps.sh
fi

export MAVEN_OPTS="-Djava.library.path=$DEPS"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DEPS:$DEPS/lib

cd iot.agile.protocol.BLE
mvn exec:java &
echo "Started AGILE BLE protocol"

cd ../iot.agile.ProtocolManager
mvn exec:java &
echo "Started AGILE Protocol Manager"

cd ../iot.agile.DeviceManager
mvn exec:java &
echo "Started AGILE Device Manager"
