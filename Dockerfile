
FROM resin/raspberrypi2-debian

# Add packages
RUN \
  apt-get -qq update  && apt-get -qq install -y \
  git ca-certificates make cmake wget apt software-properties-common \
  unzip cpp binutils maven

ENV APATH /agile

RUN mkdir -p $APATH

COPY ./*agile* $APATH/
COPY ./scripts $APATH/
COPY ./pom.xml $APATH/

WORKDIR $APATH
RUN $APATH/scripts/install-deps.sh $APATH

ENV INITSYSTEM on
CMD [ $APATH/scripts/start.sh, $APATH ]
