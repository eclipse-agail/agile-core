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

#ARM toolchain
CC=arm-linux-gnueabi-gcc

WD=`pwd`
CURRDIR=${1:-$WD}
DEPS=$CURRDIR/deps
BUILD=$CURRDIR/build

rm -rf $BUILD

mkdir -p $DEPS
mkdir -p $BUILD

DBUSJAVA=2.9 #note: this is not an official release, that stopped at 2.7
LMLIB=0.8

cd $BUILD

wget http://www.matthew.ath.cx/projects/java/libmatthew-java-$LMLIB.tar.gz >> /dev/null
tar -xzf libmatthew-java-$LMLIB.tar.gz
rm libmatthew-java-$LMLIB.tar.gz

#wget https://dbus.freedesktop.org/releases/dbus-java/dbus-java-$DBUSJAVA.tar.gz
#tar -xzf dbus-java-$DBUSJAVA.tar.gz
#rm dbus-java-$DBUSJAVA.tar.gz

if [ ! -e "./dbus-java-$DBUSJAVA" ]
then
    git clone https://github.com/jeanparpaillon/dbus-java.git dbus-java-$DBUSJAVA
fi

cd "dbus-java-$DBUSJAVA"
git pull origin $DBUSJAVA

cd ../libmatthew-java-$LMLIB

if [ "$2" = "arm" ]
then
    echo "ARM build"
    CC=$CC LD=$CC make
else
    CC=gcc LD=gcc make >> /dev/null
fi

DIST=`pwd`/dist
mkdir -p $DIST

PREFIX=$DIST make install >> /dev/null

cp $DIST/share/java/*.jar $DEPS
cp $DIST/lib/jni/*.so $DEPS
# cp $DIST/libunix-java.so $DEPS/unix-java.so

cd $BUILD/dbus-java-$DBUSJAVA

if [ "$2" = "arm" ]
then
    ARCH=arm CROSS_COMPILE=$CC PREFIX=$BUILD JAVAUNIXLIBDIR=$DIST/lib/jni JAVAUNIXJARDIR=$DIST/share/java make bin >> /dev/null
else
    PREFIX=$BUILD JAVAUNIXLIBDIR=$DIST/lib/jni JAVAUNIXJARDIR=$DIST/share/java make bin >> /dev/null
fi

cp ./*.jar $DEPS

cd $DEPS

# if [ -e ~/.m2/repository/org/freedesktop/dbus-java ] ; then
#   rm -r ~/.m2/repository/org/freedesktop/dbus-java
#   rm -r ~/.m2/repository/org/freedesktop/libdbus-java
#   rm -r ~/.m2/repository/cx/ath
# fi

mvn install:install-file -Dfile=$DEPS/dbus-java-bin-$DBUSJAVA.jar \
                         -DgroupId=org.freedesktop.dbus \
                         -DartifactId=dbus-java \
                         -Dversion=$DBUSJAVA \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/libdbus-java-$DBUSJAVA.jar \
                         -DgroupId=org.freedesktop.dbus \
                         -DartifactId=libdbus-java \
                         -Dversion=$DBUSJAVA \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/unix-0.5.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=unix \
                         -Dversion=0.5 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/debug-enable-1.1.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=debug-enable \
                         -Dversion=1.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/debug-disable-1.1.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=debug-disable \
                         -Dversion=1.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/cgi-0.6.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=cgi \
                         -Dversion=0.6 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/hexdump-0.2.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=hexdump \
                         -Dversion=0.2 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS

mvn install:install-file -Dfile=$DEPS/io-0.1.jar \
                         -DgroupId=cx.ath.matthew \
                         -DartifactId=io \
                         -Dversion=0.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS
