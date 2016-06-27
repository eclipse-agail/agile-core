#!/bin/sh

sudo apt install --no-install-recommends -y gettext git cmake Xvfb
# needed for docs only
# sudo apt install --no-install-recommends -y texlive-latex-base texlive-latex-extra tex4ht

CURRDIR=`pwd`
DEPS=${1:-$CURRDIR/deps}

if [ -e "$DEPS" ]; then
  rm $DEPS -rf
fi

cd $CURRDIR

sh ./scripts/install-dbus-java.sh $DEPS
sh ./scripts/install-tinyb.sh $DEPS

mvn clean install -U
