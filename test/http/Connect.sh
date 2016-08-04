#!/bin/sh

#Connect Device
curl -X POST --header 'Accept: application/json' 'http://localhost:8080/api/device/ble_B0B448BD1085/connection'
