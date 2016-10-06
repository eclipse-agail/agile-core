#!/bin/bash
deviceID=\"$1\"
echo $deviceID
#Extract device from list of discovered devices
device=$(curl -X GET --header 'Accept: application/json' 'http://localhost:8080/api/protocols/devices'  | jq '.[] | select(.id == '$deviceID')')

echo $device

types=$(curl -X POST --header 'Content-Type: application/json'  --header 'Accept: application/json' 'http://localhost:8080/api/devices/typeof' -d "$device" | jq -r '@csv' )
echo "Types: $types"
echo ${#types[@]}

if [ ${#types[@]} -ne 1 ]; then
  echo "Cannot automatically determine Device type"
else
  #Register devices
  echo '{"overview": '"$device"' , "type": '"${types[0]}"'}'
  curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{"overview": '"$device"' , "type": '"${types[0]}"'}' 'http://localhost:8080/api/devices/register'
fi


