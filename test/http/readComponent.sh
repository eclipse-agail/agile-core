#!/bin/sh
#Read componet value from registered device
deviceID=$1
componetName=$2
result=$(curl -X GET "http://localhost:8080/api/device/$deviceID/$componetName")
echo $componetName  " : "$result
