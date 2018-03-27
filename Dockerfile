#-------------------------------------------------------------------------------
# Copyright (C) 2017 Create-Net / FBK.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
# 
# SPDX-License-Identifier: EPL-2.0
# 
# Contributors:
#     Create-Net / FBK - initial API and implementation
#-------------------------------------------------------------------------------

ARG BASEIMAGE_BUILD=agileiot/raspberry-pi3-zulujdk:8-jdk-maven
ARG BASEIMAGE_DEPLOY=agileiot/raspberry-pi3-zulujdk:8-jre
FROM $BASEIMAGE_BUILD

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

# resin-sync will always sync to /usr/src/app, so code needs to be here.
WORKDIR /usr/src/app
ENV APATH /usr/src/app

COPY scripts scripts
COPY agile-dbus-java-interface agile-dbus-java-interface
RUN CC=clang CXX=clang++ CMAKE_C_COMPILER=clang CMAKE_CXX_COMPILER=clang++ \
scripts/install-agile-interfaces.sh $APATH/deps

# copy directories into WORKDIR
COPY agile-main agile-main
COPY org.eclipse.agail.DeviceManager org.eclipse.agail.DeviceManager
COPY org.eclipse.agail.DeviceFactory org.eclipse.agail.DeviceFactory
COPY org.eclipse.agail.ProtocolManager org.eclipse.agail.ProtocolManager
COPY org.eclipse.agail.http org.eclipse.agail.http
COPY test test
COPY pom.xml pom.xml

RUN mvn package

FROM $BASEIMAGE_DEPLOY
WORKDIR /usr/src/app
ENV APATH /usr/src/app

COPY --from=0 $APATH/scripts scripts
COPY --from=0 $APATH/test test
COPY --from=0 $APATH/deps deps
COPY --from=0 $APATH/org.eclipse.agail.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar org.eclipse.agail.ProtocolManager/target/protocol-manager-1.0-jar-with-dependencies.jar
COPY --from=0 $APATH/org.eclipse.agail.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar org.eclipse.agail.DeviceManager/target/device-manager-1.0-jar-with-dependencies.jar
COPY --from=0 $APATH/org.eclipse.agail.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar org.eclipse.agail.DeviceFactory/target/DeviceFactory-1.0-jar-with-dependencies.jar
COPY --from=0 $APATH/org.eclipse.agail.DeviceFactory/target/classes/org/eclipse/agail/device/instance org.eclipse.agail.DeviceFactory/target/classes/org/eclipse/agail/device/instance
COPY --from=0 $APATH/org.eclipse.agail.http/target/http-1.0-jar-with-dependencies.jar org.eclipse.agail.http/target/http-1.0-jar-with-dependencies.jar

CMD [ "bash", "/usr/src/app/scripts/start.sh" ]
