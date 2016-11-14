#!/bin/sh

curl -v POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{  
   "name": "C",
   "protocol": "string", 
   "id": "bleA0E6F8B62304",  
   "status": "CONNECTED" 
 }' 'http://localhost:8080/api/devices/typeof'
