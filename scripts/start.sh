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
  echo "To start a single module use:\n $0 DeviceManager|ProtocolManager|BLE|http"
  echo ""
fi

TOEXPORT=""

if [ `xdpyinfo -display :0 >/dev/null 2>&1 && echo 1 || echo 0 ` -eq 1 ]; then
  export DISPLAY=:0
  echo ">> Using current DISPLAY at $DISPLAY"
  TOEXPORT="\n$TOEXPORT\nexport DISPLAY=$DISPLAY"
else
  Xvfb :0 -screen 0 1x1x8 &
  export DISPLAY=:0
  echo "++ Created new DISPLAY"
  TOEXPORT="\n$TOEXPORT\nexport DISPLAY=$DISPLAY"
fi

ME=`whoami`

if [ ! -z "$DBUS_SESSION_BUS_ADDRESS" ]; then
  echo ">> DBUS_SESSION_BUS_ADDRESS available, reusing current instance"
else

  if [ `pgrep -U $ME dbus-daemon -c` -gt 0 ]; then

    echo ">> DBus session available"

    MID=`sed "s/\n//" /var/lib/dbus/machine-id`
    DISPLAYID=`echo $DISPLAY | sed "s/://"`
    SESSFILEPATH="/home/$ME/.dbus/session-bus/$MID-$DISPLAYID"

    if [ -e $SESSFILEPATH ]; then
      echo ">> Loading DBus session instance address from local file"
      echo ">> Source: $SESSFILEPATH"
      . "$SESSFILEPATH"
    else
      # echo "!! Killing current session bus instance"
      # kill `ps aux | grep dbus-daemon | grep session | awk '{print $2}'`
      echo "Cannot get Dbus session address. Panic!"
    fi

  else
    export `dbus-launch`
    sleep 2
    echo "++ Started a new DBus session instance"
  fi

fi

TOEXPORT="\n$TOEXPORT\nexport DBUS_SESSION_BUS_ADDRESS=$DBUS_SESSION_BUS_ADDRESS"

if [ -z "$DBUS_SESSION_BUS_ADDRESS" ]; then
  echo "!! Cannot export DBUS_SESSION_BUS_ADDRESS. Exit"
  exit 1
fi
export DBUS_SESSION_BUS_ADDRESS

export MAVEN_OPTS_BASE="-Djava.library.path=$DEPS:$DEPS/lib -DDISPLAY=$DISPLAY -DDBUS_SESSION_BUS_ADDRESS=$DBUS_SESSION_BUS_ADDRESS"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DEPS:$DEPS/lib:/usr/lib:/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/lib/arm

mvn="mvn"

if [ $MODULE = 'all' ] || [ $MODULE = 'ProtocolManager' ]; then
  ./scripts/stop.sh "ProtocolManager"
  java -jar -Djava.library.path=deps iot.agile.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar &
  echo "Started AGILE Protocol Manager"
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'DeviceManager' ]; then
  ./scripts/stop.sh "DeviceManager"
  java -jar -Djava.library.path=deps iot.agile.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar &
  echo "Started AGILE Device Manager"
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'http' ]; then
  ./scripts/stop.sh "http"
  java -jar -Djava.library.path=deps iot.agile.http/target/http-1.0-jar-with-dependencies.jar &
  echo "Started AGILE HTTP API"
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'BLE' ]; then
  ./scripts/stop.sh "protocol.BLE"
  # wait for ProtocolManager to initialize
  while `! qdbus iot.agile.ProtocolManager > /dev/null`; do
    echo "waiting for ProtocolManager to initialize";
    sleep 1;
  done

  java -cp deps/tinyb.jar:iot.agile.protocol.BLE/target/ble-1.0-jar-with-dependencies.jar -Djava.library.path=deps:deps/lib iot.agile.protocol.ble.BLEProtocolImp &
  echo "Started AGILE BLE protocol"
fi


echo "Modules launched use this variables in the shell:"
echo $TOEXPORT
echo ""

wait
