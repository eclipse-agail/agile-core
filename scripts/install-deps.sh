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

set -e

apt install --no-install-recommends -y gettext git cmake
# needed for docs only
# sudo apt install --no-install-recommends -y texlive-latex-base texlive-latex-extra tex4ht

CURRDIR=`pwd`
DEPS=${1:-$CURRDIR/deps}

if [ -e "$DEPS" ]; then
  rm $DEPS -rf
fi

cd $CURRDIR

sh ./scripts/install-dbus-java.sh $DEPS
sh ./scripts/install-agile-interfaces.sh $DEPS

mvn clean install -U
