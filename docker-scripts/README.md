
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

- pull ARM image file and start AGILE
```
docker pull cskiraly/agile-arm:latest # Not strictly needed, as the container is also pulled by docker-start, if not done before.
docker-scripts/docker-start cskiraly/agile-arm:latest
. docker-scripts/docker-env # Expose AGILE D-Bus in shell. 
```

Testing the installation
---

```
sudo apt-get install python3-dbus -y # Make sure D-Bus python support is available.
. docker-scripts/docker-env # Set up environment to connect to AGILE D-Bus.                                                      
test/discover.py
```