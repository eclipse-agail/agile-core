#!/bin/sh

CURRDIR=`pwd`
DEPS=${1:-$CURRDIR/deps}
BUILD=$DEPS/build

TINYB_VER=0.5.0

#clean up first
if [ -e "$DEPS/tinyb" ] ; then
  rm -r $DEPS/tinyb
  rm $DEPS/tinyb*
  rm $DEPS/libjavatinyb.so*
  # drop from local repo eventually
  if [ -e ~/.m2/repository/tinyb ] ; then
    rm -r  ~/.m2/repository/tinyb
  fi
fi

if [ ! -e "$BUILD" ] ; then
  mkdir -p $BUILD
fi

if [ ! -e "$BUILD/tinyb" ] ; then
  cd $BUILD
  git clone https://github.com/intel-iot-devkit/tinyb.git
  cd tinyb
  git checkout "v$TINYB_VER"
  cd ..
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

#rm -rf $BUILD

cd $DEPS
mvn install:install-file -Dfile=$DEPS/tinyb.jar \
                         -DgroupId=tinyb \
                         -DartifactId=tinyb \
                         -Dversion=$TINYB_VER \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS
