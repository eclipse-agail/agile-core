#!/bin/sh
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

HOST=${1:-localhost}

#Start discovery
echo "Device discovery started"
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' "http://$HOST:8080/api/protocols/discovery"
#sleep for 10 seconds
sleep 10s
#stop discovery
echo "Device discovery stopped"
curl -X DELETE --header 'Accept: application/json' "http://$HOST:8080/api/protocols/discovery"
#sleep 1s
#Print discovered devices
echo "Discovered devices"
curl -X GET --header 'Accept: application/json' "http://$HOST:8080/api/protocols/devices" | jq '.[] | {id: .id, name: .name, protocol: .protocol, status: .status }'
