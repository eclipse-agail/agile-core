#!/bin/sh
#-------------------------------------------------------------------------------
# Copyright (C) 2017 Create-Net / FBK.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
# 
# SPDX-License-Identifier: EPL-2.0
# 
# Contributors:
#     Create-Net / FBK - initial API and implementation
#-------------------------------------------------------------------------------

MODULE=${1:-all}
DEPS=`realpath ./deps`

if [ ! -e "$DEPS" ]; then
  echo "Installing dependencies"
  ./scripts/install-deps.sh
fi

echo "DEPS dir $DEPS"

if [ "${MODULE}" = 'all' ]; then
  echo ""
  echo "To start a single module use:\n $0 DeviceManager|ProtocolManager|http|DeviceFactory"
  echo ""
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

export MAVEN_OPTS_BASE="-Djava.library.path=$DEPS:$DEPS/lib -DDISPLAY=$DISPLAY -DDBUS_SESSION_BUS_ADDRESS=$DBUS_SESSION_BUS_ADDRESS"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DEPS:$DEPS/lib:/usr/lib:/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/lib/arm

mvn="mvn"

if [ $MODULE = 'all' ] || [ $MODULE = 'ProtocolManager' ]; then
  ./scripts/stop.sh "ProtocolManager"
  java -jar -Djava.library.path=deps org.eclipse.agail.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar &
  echo "Started AGILE Protocol Manager"
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'DeviceManager' ]; then
  ./scripts/stop.sh "DeviceManager"
  java -jar -Djava.library.path=deps org.eclipse.agail.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar &
  echo "Started AGILE Device Manager"
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'http' ]; then
  ./scripts/stop.sh "http"
  java -jar -Djava.library.path=deps org.eclipse.agail.http/target/http-1.0-jar-with-dependencies.jar &
  echo "Started AGILE HTTP API"
fi

if [ $MODULE = 'all' ] || [ $MODULE = 'DeviceFactory' ]; then
  ./scripts/stop.sh "DeviceFactory"
  java -cp "org.eclipse.agail.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar:$PWD/plugins" \
       -Djava.library.path=deps \
       org.eclipse.agail.devicefactory.DeviceFactoryImp "$PWD/plugins" &
  echo "Started AGILE Device Factory"
fi


echo "Modules launched use this variables in the shell:"
echo $TOEXPORT
echo ""

wait
