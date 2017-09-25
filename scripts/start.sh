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
DEFAULTDBUS="unix:path=/usr/src/app/.agile_bus/agile_bus_socket"
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

if [ -z "$DBUS_SESSION_BUS_ADDRESS" ]; then
  echo "Using default Agile DBUS address $DEFAULTDBUS"
  DBUS_SESSION_BUS_ADDRESS=$DEFAULTDBUS
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

java -Djava.library.path="$WD/jni" -jar $PKG
