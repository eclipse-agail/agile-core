#!/bin/sh
deviceID=\"$1\"
echo $deviceID
#Extract device from list of discovered devices
device=$(curl -X GET --header 'Accept: application/json' 'http://localhost:8080/api/protocols/devices'  | jq '.[] | select(.id == '$deviceID')|{id: .id, name: .name, protocol: .protocol, status: .status }')

#If SensorTag found Register it
if [ ${#device[@]} -eq 0 ]; then
  echo "SensorTag device not found"
else
  name=$( echo "$device" | jq '.name' )
  id=$( echo "$device" | jq '.id' )
  protocol=$( echo "$device" | jq '.protocol' )

  #Register sensor tag devices
  curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d ' {"id": '$id' ,"name": '"$name"' ,"protocol" : '$protocol', "path": "iot.agile.device.CCSensorTag","streams": [{"id": "Temperaturep", "unit": "C"}]}' 'http://localhost:8080/api/devices'
fi
