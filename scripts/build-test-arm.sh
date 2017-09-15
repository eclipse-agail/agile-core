#!/bin/sh

./scripts/install-dbus-java.sh `pwd` arm

mvn clean package

docker build . -t 192.168.38.245:5000/agile-core
docker push 192.168.38.245:5000/agile-core
