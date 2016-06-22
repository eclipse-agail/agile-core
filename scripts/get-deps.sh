#!/bin/sh

sudo apt install --no-install-recommends -y gettext
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
  rm $DEPS -r
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
echo "rm -r $BUILD"

mvn install:install-file \
   -Dfile=$DEPS/dbus-java-bin-2.7.jar \
   -DgroupId=org.freedesktop.dbus \
   -DartifactId=dbus-java \
   -Dversion=$DBUSJAVA \
   -Dpackaging=org.freedesktop.dbus \
   -DgeneratePom=true
