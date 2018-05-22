#!/bin/bash
#-------------------------------------------------------------------------------
# Copyright (C) 2017 Create-Net / FBK.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
# 
# SPDX-License-Identifier: EPL-2.0
# 
# Contributors:
#     Create-Net / FBK - initial API and implementation
#-------------------------------------------------------------------------------

deviceID=\"$1\"
deviceType="$2"

#Extract device from list of discovered devices
device=$(curl -X GET --header 'Accept: application/json' 'http://localhost:8080/api/protocols/devices'  | jq '.[] | select(.id == '$deviceID')')
echo $device

if [ -z "$deviceType" ]; then
  types=$(curl -X POST --header 'Content-Type: application/json'  --header 'Accept: application/json' 'http://localhost:8080/api/devices/typeof' -d "$device" | jq -r '@csv' )
  echo "Types: $types"

echo ${#types[@]}

  if [[ ${#types[@]} -ne 1 || -z "${types[0]}" ]]; then
    echo "Cannot automatically determine Device type"
    exit 1
  else
    deviceType="${types[0]}"
  fi
else
  deviceType=\""$deviceType"\"
fi

echo Registering device
echo '{"overview": '"$device"' , "type": '"${types[0]}"'}'
curl  -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{"overview": '"$device"' , "type": '"$deviceType"'}' 'http://localhost:8080/api/devices'
