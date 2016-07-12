
FROM resin/raspberrypi2-debian

# Add packages
RUN \
  apt-get -qq update  && apt-get -qq install -y \
  git ca-certificates make cmake wget apt software-properties-common \
  unzip cpp binutils maven

ENV APATH /agile

RUN mkdir -p $APATH

COPY ./*agile* $APATH
COPY ./scripts $APATH
COPY ./pom.xml $APATH

WORKDIR $APATH
RUN ./scripts/install-deps.sh

ENV INITSYSTEM on
CMD [ ./scripts/start.sh, $APATH ]
