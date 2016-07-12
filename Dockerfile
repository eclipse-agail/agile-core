
FROM resin/raspberrypi2-debian
# FROM debian:jessie

# Add packages
RUN \
  apt-get -qq update  && apt-get -qq install -y \
  git ca-certificates make cmake wget apt software-properties-common \
  unzip cpp binutils maven \
  gettext Xvfb

RUN \
  echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee /etc/apt/sources.list.d/webupd8team-java.list && \
  echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list && \
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886 && \
  apt-get update && \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  apt-get install oracle-java8-installer -y && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

ENV APATH /agile

RUN mkdir -p $APATH

WORKDIR $APATH

COPY ./ ./

RUN $APATH/scripts/install-dbus-java.sh $APATH/deps
RUN $APATH/scripts/install-tinyb.sh $APATH/deps

RUN \
  cd $APATH && \
  mvn clean install -U

ENV INITSYSTEM on
CMD [ $APATH/scripts/start.sh ]
