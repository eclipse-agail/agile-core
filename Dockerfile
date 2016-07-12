
FROM resin/raspberrypi2-debian

# Add packages
RUN \
  apt-get -qq update  && apt-get -qq install -y \
  git ca-certificates make cmake wget apt software-properties-common \
  unzip cpp binutils maven

RUN mkdir -p /agile

COPY ./agile* /
COPY ./Agile* /
COPY ./scripts /

RUN /scripts/install-deps.sh

ENV INITSYSTEM on
CMD [ /scripts/start.sh, / ]
