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

set -e

WD=`pwd`
CURRDIR=${1:-$WD}
BUILD=$CURRDIR/build

mkdir -p $BUILD

git_fetch() {

    cd $BUILD

    REPO=$1
    LIBNAME=$2
    BRANCH=${3:-"master"}

    if [ ! -e "./$LIBNAME" ]
    then
        echo "Clone $REPO to $LIBNAME"
        git clone $REPO $LIBNAME
    fi

    echo "Fetching $LIBNAME"
    cd $LIBNAME
    git checkout $BRANCH
    git pull
    echo "OK"
}

LIB="jnr-unixsocket"
git_fetch "https://github.com/jnr/jnr-unixsocket" "$LIB" # "master"
mvn clean install

LIB="dbus-java-mvn"
git_fetch "https://github.com/muka/dbus-java-mvn.git" "$LIB" # "master"
mvn clean install

LIB="agile-api-spec"
git_fetch "https://github.com/Agile-IoT/agile-api-spec.git" "$LIB" # "master"
cd agile-dbus-java-interface
mvn clean install
