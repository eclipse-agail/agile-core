#!/bin/sh
#Read temeprature value from registered SensorTag
temp=$(curl -X GET 'http://localhost:8080/api/device/ble_B0B448BD1085/Temperature')
echo "Temperature: "$temp
