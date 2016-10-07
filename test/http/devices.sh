#!/bin/sh

HOST=${1:-localhost}

#Print Protocol devices
echo "ProtocolManager devices"
curl -X GET --header 'Accept: application/json' "http://$HOST:8080/api/protocols/devices" | jq '.[] | {id: .id, name: .name, protocol: .protocol, status: .status }'

#Print registered devices
echo "Registered devices"
curl -X GET --header 'Accept: application/json' "http://$HOST:8080/api/devices" | jq '.[] | {id: .deviceId, name: .name, protocol: .protocol, address: .address }'
