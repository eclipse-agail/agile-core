#!/bin/sh
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

MODULE=$1

WD=`pwd`

if [ "${MODULE}" = '' ]; then
  echo "Missing module name! Provide one of those options:"
  echo ""
  echo " - DeviceManager"
  echo " - ProtocolManager"
  echo " - http"
  echo " - DeviceFactory"
  echo ""
  exit 1
fi

TOEXPORT=""

if [ ! -z "$DISPLAY" ]; then
  echo ">> DISPLAY available, reusing current display"
else
  export DISPLAY=:0
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

PKG=""
case $MODULE in
   "ProtocolManager")
    PKG="iot.agile.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar"
    ;;
   "DeviceManager")
    PKG="iot.agile.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar"
    ;;
   "DeviceFactory")
    PKG="iot.agile.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar"
    ;;
   "http")
    PKG="iot.agile.http/target/http-1.0-jar-with-dependencies.jar"
    ;;
esac

echo "Modules launched use this variables in the shell:"
echo $TOEXPORT
echo ""

mkdir -p $WD/tmp
chmod 777 $WD/tmp

java -Djava.io.tmpdir=$WD/tmp -jar $PKG
