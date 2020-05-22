#!/bin/bash

sdir="$(dirname $0)"

echo "Script dir=$sdir"

cd $sdir

echo "Killing all started wikipedia-pod instances..."
pgrep -a -f wikipedia-pod.jar | awk '{print $1;}' | while read -r a; do kill -9 $a; done

java -jar wikipedia-pod.jar &
