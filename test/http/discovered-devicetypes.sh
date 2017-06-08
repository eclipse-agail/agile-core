#!/bin/bash
#-------------------------------------------------------------------------------
# Copyright (C) 2017 Create-Net / FBK.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# 
# Contributors:
#     Create-Net / FBK - initial API and implementation
#-------------------------------------------------------------------------------

HOST=${1:-localhost}

#Extract device from list of discovered devices
devices=$(curl -X GET --header 'Accept: application/json' "http://$HOST:8080/api/protocols/devices" | jq -c '.[]')
echo "$devices" | while read device; do
  echo
  echo "$device"
  types=$(curl -X POST --header 'Content-Type: application/json'  --header 'Accept: application/json' 'http://localhost:8080/api/devices/typeof' -d "$device" | jq -r '@csv' )
  echo "Types: $types"
done

exit

