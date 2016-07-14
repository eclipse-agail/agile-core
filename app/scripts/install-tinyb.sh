#!/bin/sh

CURRDIR=`pwd`
DEPS=/usr/src/app/deps
BUILD=$DEPS/build

if [ ! -e "$BUILD" ] ; then
  mkdir -p $BUILD
fi

if [ ! -e "$BUILD/tinyb" ] ; then
  cd $BUILD
  git clone https://github.com/intel-iot-devkit/tinyb.git
fi

cd $BUILD/tinyb

mkdir -p build
cd build

# echo "add_compile_options(-std=c++11)" >> ../CMakeLists.txt
cmake .. -DCMAKE_C_COMPILER=/usr/bin/clang -DCMAKE_CXX_COMPILER=/usr/bin/clang++ \
-DCMAKE_INSTALL_PREFIX=$DEPS -DBUILDJAVA=ON >> /dev/null

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
                         -Dversion=1.0 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$DEPS
