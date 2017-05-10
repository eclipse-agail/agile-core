#!/bin/sh
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

#Read componet value from registered device
deviceID=$1
componetName=$2
result=$(curl -X GET "http://localhost:8080/api/device/$deviceID/$componetName")
echo $componetName  " : "$result
