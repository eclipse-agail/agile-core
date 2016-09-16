#!/bin/sh

deviceID=$1

curl -I -X DELETE --header 'Accept: application/json' "http://localhost:8080/api/devices/$deviceID"
