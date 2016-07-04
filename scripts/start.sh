#!/bin/sh

MODULE=${1:-all}
DEPS=`realpath ./deps`

if [ ! -e "$DEPS" ]; then
  echo "Installing dependencies"
  ./scripts/install-deps.sh
fi

echo "DEPS dir $DEPS"

if [ "${MODULE}" = 'all' ]; then
  echo ""
  echo "To start a single module use:\n $0 DeviceManager|ProtocolManager|BLE"
  echo ""
fi

TOEXPORT=""

if [ `xdpyinfo -display :0 >/dev/null 2>&1 && echo 1 || echo 0 ` -eq 1 ]; then
  export DISPLAY=:0
  echo ">> Using current DISPLAY at $DISPLAY"
  TOEXPORT="\n$TOEXPORT\nDISPLAY=$DISPLAY"
else
  Xvfb :0 -screen 0 1x1x8 &
  export DISPLAY=:0
  echo "++ Created new DISPLAY"
  TOEXPORT="\n$TOEXPORT\nDISPLAY=$DISPLAY"
fi

ME=`whoami`
MID=`sed "s/\n//" /var/lib/dbus/machine-id`

if [ `pgrep -U $ME dbus-daemon -c` -eq 0 ]; then
  export `dbus-launch`
  echo "++ Start new DBus session instance"
  TOEXPORT="\n$TOEXPORT\nexport DBUS_SESSION_BUS_ADDRESS=$DBUS_SESSION_BUS_ADDRESS"
else
  echo ">> Reusing available DBus instance"
  . "/home/$ME/.dbus/session-bus/$MID-0"
  TOEXPORT="\n$TOEXPORT\nexport DBUS_SESSION_BUS_ADDRESS=$DBUS_SESSION_BUS_ADDRESS"
fi

if [ -z "$DBUS_SESSION_BUS_ADDRESS" ]; then
  echo "!! Cannot export DBUS_SESSION_BUS_ADDRESS. Exit"
  exit 1
fi

export MAVEN_OPTS="-Djava.library.path=$DEPS: -DDISPLAY=$DISPLAY -DDBUS_SESSION_BUS_ADDRESS=$DBUS_SESSION_BUS_ADDRESS"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DEPS:$DEPS/lib:/usr/lib:/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/lib/arm

if [ $MODULE = 'all' ] || [ $MODULE = 'BLE' ]; then
  ./scripts/stop.sh "protocol.BLE"
  cd iot.agile.protocol.BLE
  mvn -q exec:java &
  echo "Started AGILE BLE protocol"
  cd ../
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'ProtocolManager' ]; then
  ./scripts/stop.sh "protocolmanager"
  cd iot.agile.ProtocolManager
  mvn -q exec:java &
  echo "Started AGILE Protocol Manager"
  cd ..
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'DeviceManager' ]; then
  ./scripts/stop.sh "devicemanager"
  cd iot.agile.DeviceManager
  mvn -q exec:java &
  echo "Started AGILE Device Manager"
  cd ..
fi


echo "Modules launched use this variables in the shell:\n"
echo $TOEXPORT
