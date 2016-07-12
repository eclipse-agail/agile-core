
FROM resin/rpi-raspbian:jessie-20160706

RUN mkdir -p /agile

# Add packages
RUN \
  apt-get -qq update  && apt-get -qq install -y \
  git ca-certificates make cmake wget apt software-properties-common \
  unzip cpp binutils maven

COPY ./agile* /
COPY ./Agile* /
COPY ./scripts /

RUN /scripts/install-deps.sh

CMD [ /scripts/start.sh ]
