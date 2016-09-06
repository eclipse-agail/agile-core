#!/bin/sh
#Read temeprature value from registered SensorTag
temp=$(curl -X GET 'http://localhost:8080/api/device/bleC4BE84706909/Optical')
echo "Temperature: "$temp
