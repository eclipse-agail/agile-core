FROM resin/raspberrypi3-openjdk:openjdk-8-jdk-20170217

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

RUN CC=clang CXX=clang++ CMAKE_C_COMPILER=clang CMAKE_CXX_COMPILER=clang++ \
scripts/install-dbus-java.sh $APATH/deps

RUN CC=clang CXX=clang++ CMAKE_C_COMPILER=clang CMAKE_CXX_COMPILER=clang++ \
scripts/install-agile-interfaces.sh $APATH/deps

# copy directories into WORKDIR
COPY agile-main agile-main
COPY iot.agile.DeviceManager iot.agile.DeviceManager
COPY iot.agile.ProtocolManager iot.agile.ProtocolManager
COPY iot.agile.http iot.agile.http
COPY test test
COPY pom.xml pom.xml

RUN mvn package

CMD [ "bash", "/usr/src/app/scripts/start.sh" ]
