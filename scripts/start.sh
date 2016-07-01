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

if [ `xdpyinfo -display :1 >/dev/null 2>&1 && echo 1 || echo 0 ` -eq 1 ]; then
  export DISPLAY=:1
  echo "Using current DISPLAY at $DISPLAY"
else
  Xvfb :1 -screen 0 1x1x8 &
  export DISPLAY=:1
  echo "Created new DISPLAY at $DISPLAY"
fi

if [ `ps aux | grep "dbus-daemon" | wc -l` -eq 1 ]; then
  export `dbus-launch`
  echo "Launched new DBus instance"
  echo $DBUS_SESSION_BUS_ADDRESS
else

  echo "DBus instance available"

  MID=`sed "s/\n//" /var/lib/dbus/machine-id`
  ME=`whoami`
  
  # use . instead of source in dash
  . "/home/$ME/.dbus/session-bus/$MID-0"

  echo "ls -l /home/$ME/.dbus/session-bus/"

  echo $DBUS_SESSION_BUS_ADDRESS
fi

if [ -z "$DBUS_SESSION_BUS_ADDRESS" ]; then
  echo "Cannot export DBUS_SESSION_BUS_ADDRESS. Exit"
  exit 1
fi

export MAVEN_OPTS="-Djava.library.path=$DEPS -DDISPLAY=$DISPLAY -DDBUS_SESSION_BUS_ADDRESS=$DBUS_SESSION_BUS_ADDRESS"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DEPS:$DEPS/lib

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
