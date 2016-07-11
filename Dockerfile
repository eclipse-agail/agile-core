
FROM resin/rpi-raspbian:wheezy-2015-01-15

RUN sudo apt-get install git

RUN git clone https://github.com/Agile-IoT/Agile-BLE.git ./api
RUN cd ./api

EXPOSE 8080

CMD [./scripts/start.sh]
