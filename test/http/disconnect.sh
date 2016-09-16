#!/bin/sh

deviceID=$1

#Disconnect Device
curl -X DELETE --header 'Accept: application/json' "http://localhost:8080/api/device/$deviceID/connection"
