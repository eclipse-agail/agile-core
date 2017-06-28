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

FROM agileiot/raspberry-pi3-zulujdk:8-jdk-maven

# Add packages
RUN apt-get update && apt-get install --no-install-recommends -y \
    build-essential \
    git\
    ca-certificates \
    apt \
    software-properties-common \
    unzip \
    cpp \
    binutils \
    maven \
    gettext \
    libc6-dev \
    make \
    cmake \
    cmake-data \
    pkg-config \
    clang \
    gcc-4.9 \
    g++-4.9 \
    qdbus \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

#Make maven and javac work in ARM emulation
ENV MAVEN_OPTS="-Xint"
ENV _JAVA_OPTIONS="-Xint"
ENV JAVA_TOOL_OPTIONS="-Xint"

# resin-sync will always sync to /usr/src/app, so code needs to be here.
WORKDIR /usr/src/app
ENV APATH /usr/src/app

COPY scripts scripts

RUN CC=clang CXX=clang++ CMAKE_C_COMPILER=clang CMAKE_CXX_COMPILER=clang++ \
scripts/install-dbus-java.sh $APATH/deps

RUN CC=clang CXX=clang++ CMAKE_C_COMPILER=clang CMAKE_CXX_COMPILER=clang++ \
scripts/install-agile-interfaces.sh $APATH/deps

# copy directories into WORKDIR
COPY agile-main agile-main
COPY iot.agile.DeviceManager iot.agile.DeviceManager
COPY iot.agile.DeviceFactory iot.agile.DeviceFactory
COPY iot.agile.ProtocolManager iot.agile.ProtocolManager
COPY iot.agile.http iot.agile.http
COPY test test
COPY pom.xml pom.xml

RUN mvn package

FROM agileiot/raspberry-pi3-zulujdk:8-jre
WORKDIR /usr/src/app
ENV APATH /usr/src/app

COPY --from=0 $APATH/scripts scripts
COPY --from=0 $APATH/deps deps
COPY --from=0 $APATH/iot.agile.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar iot.agile.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar
COPY --from=0 $APATH/iot.agile.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar iot.agile.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar
COPY --from=0 $APATH/iot.agile.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar iot.agile.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar
COPY --from=0 $APATH/iot.agile.DeviceFactory/target/classes/iot/agile/device/instance iot.agile.DeviceFactory/target/classes/iot/agile/device/instance
COPY --from=0 $APATH/iot.agile.http/target/http-1.0-jar-with-dependencies.jar iot.agile.http/target/http-1.0-jar-with-dependencies.jar

CMD [ "bash", "/usr/src/app/scripts/start.sh" ]
