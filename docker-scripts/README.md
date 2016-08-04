
Starting AGILE components with Docker
===

This folder contains scripts to start AGILE components as Docker containers

Prerequisites
---
- install docker from [Hypriot](http://blog.hypriot.com/post/your-number-one-source-for-docker-on-arm/)
```
curl -s https://packagecloud.io/install/repositories/Hypriot/Schatzkiste/script.deb.sh | sudo bash
sudo apt-get install docker-hypriot
sudo usermod -a -G docker pi
```

- you can make the [group change effective without logout/login](http://superuser.com/questions/272061/reload-a-linux-users-group-assignments-without-logging-out)
```
`su - $USER`
```

- pull our library to have docker-scripts
```
mkdir -p AGILE && cd AGILE
git clone https://github.com/Agile-IoT/Agile-BLE.git
cd Agile-BLE
```

- pull ARM image file
```
docker pull cskiraly/agile:v0.0.2-arm
docker-scripts/docker-start cskiraly/agile:v0.0.2-arm
. docker-scripts/docker-env
```

Testing the installation
---

```
sudo apt-get install python3-dbus -y
. docker-scripts/docker-env
test/discover.py
```