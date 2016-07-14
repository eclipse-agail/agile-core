#!/bin/sh

CURRDIR=`pwd`
DEPS=${1:-$CURRDIR/deps}
BUILD=$DEPS/build
TINYB_VERSION=0.5.0

if [ ! -e "$BUILD" ] ; then
  mkdir -p $BUILD
fi

if [ ! -e "$BUILD/tinyb" ] ; then
  cd $BUILD
  git clone https://github.com/intel-iot-devkit/tinyb.git
  cd tinyb
  git checkout $TINYB_VERSION
fi

cd $BUILD/tinyb

mkdir -p build
cd build

# echo "add_compile_options(-std=c++11)" >> ../CMakeLists.txt
cmake .. -DCMAKE_CXX_FLAGS="-std=c++11" -DCMAKE_INSTALL_PREFIX=$DEPS -DBUILDJAVA=ON >> /dev/null
make tinyb >> /dev/null
make install >> /dev/null

cp java/tinyb.jar $DEPS
cp java/jni/*.so $DEPS
cp java/jni/*.so* $DEPS

rm -rf $BUILD

cd $DEPS
mvn install:install-file -Dfile=$DEPS/tinyb.jar \
                         -DgroupId=tinyb \
                         -DartifactId=tinyb \
                         -Dversion=$TINYB_VERSION \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS
