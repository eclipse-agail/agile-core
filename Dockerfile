
FROM sdhibit/rpi-raspbian

RUN sudo apt-get install git

RUN git clone https://github.com/Agile-IoT/Agile-BLE.git ./api
RUN cd ./api

RUN ./scripts/start.sh

EXPOSE 8080
