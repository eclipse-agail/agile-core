#!/bin/sh

deviceID=$1
#Connect Device by device id
curl -X POST --header 'Accept: application/json' "http://localhost:8080/api/device/$deviceID/connection"
