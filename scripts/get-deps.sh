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

make >> /dev/null
PREFIX=$BUILD make install >> /dev/null

cp ./*.jar $DEPS
cp ./*.so $DEPS

cd $BUILD/dbus-java-$DBUSJAVA

PREFIX=$BUILD JAVAUNIXLIBDIR=$BUILD/lib/jni JAVAUNIXJARDIR=$BUILD/share/java make bin >> /dev/null

cp ./*.jar $DEPS

cd $BUILD
git clone https://github.com/intel-iot-devkit/tinyb.git >> /dev/null
cd $BUILD/tinyb

mkdir build
cd build

echo "add_compile_options(-std=c++)" >> ../CMakeLists.txt
cmake .. -DBUILDJAVA=ON -DCMAKE_INSTALL_PREFIX=`pwd` >> /dev/null
make tinyb >> /dev/null
make install >> /dev/null

cp lib/java/tinyb.jar $DEPS
cp lib/*.so $DEPS
cp lib/*.so* $DEPS

cd $DEPS/../

mvn install:install-file -Dfile=$DEPS/dbus-java-bin-2.7.jar \
                         -DgroupId=org.freedesktop.dbus \
                         -DartifactId=dbus-java \
                         -Dversion=2.7 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS


mvn install:install-file -Dfile=$DEPS/libdbus-java-2.7.jar \
                         -DgroupId=org.freedesktop.dbus \
                         -DartifactId=libdbus-java \
                         -Dversion=2.7 \
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

mvn install:install-file -Dfile=$DEPS/tinyb.jar \
                         -DgroupId=tinyb \
                         -DartifactId=tinyb \
                         -Dversion=1.0 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS


# rm -rf $BUILD
