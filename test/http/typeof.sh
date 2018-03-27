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

curl -v POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{  
   "name": "C",
   "protocol": "string", 
   "id": "bleA0E6F8B62304",  
   "status": "CONNECTED" 
 }' 'http://localhost:8080/api/devices/typeof'
