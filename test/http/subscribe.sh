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

deviceID=$1
componetName=$2
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' "http://localhost:8080/api/device/$deviceID/$componetName/subscribe"
