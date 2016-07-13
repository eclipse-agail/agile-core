#!/bin/sh

ARG="$1"
PATTERN="iot.agile.$ARG"

echo "Send kill signal to $PATTERN"
pkill -f $PATTERN
