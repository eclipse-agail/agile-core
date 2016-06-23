#!/bin/sh

sudo apt install --no-install-recommends -y gettext git cmake
# needed for docs only
# sudo apt install --no-install-recommends -y texlive-latex-base texlive-latex-extra tex4ht

DBUSJAVA=2.7
LMLIB=0.8

wget http://www.matthew.ath.cx/projects/java/libmatthew-java-$LMLIB.tar.gz
wget https://dbus.freedesktop.org/releases/dbus-java/dbus-java-2.7.tar.gz

tar -xzf libmatthew-java-$LMLIB.tar.gz
rm libmatthew-java-$LMLIB.tar.gz

tar -xzf dbus-java-$DBUSJAVA.tar.gz
rm dbus-java-$DBUSJAVA.tar.gz

DEPS=`pwd`/deps
BUILD=$DEPS/build

if [ -e "$DEPS" ]; then
  rm $DEPS -rf
fi

mkdir -p $BUILD

mv dbus-java-$DBUSJAVA $BUILD
mv libmatthew-java-$LMLIB $BUILD

cd $BUILD/libmatthew-java-$LMLIB

make
PREFIX=$BUILD make install

cd $BUILD/dbus-java-$DBUSJAVA

PREFIX=$BUILD JAVAUNIXLIBDIR=$BUILD/lib/jni JAVAUNIXJARDIR=$BUILD/share/java make bin

cp ./*.jar $DEPS

cd $BUILD
git clone https://github.com/intel-iot-devkit/tinyb.git
cd $BUILD/tinyb

mkdir build
cd build

cmake .. -DBUILDJAVA=ON -DCMAKE_INSTALL_PREFIX=`pwd`
make tinyb
make install

cp lib/java/tinyb.jar $DEPS

rm -rf $BUILD
