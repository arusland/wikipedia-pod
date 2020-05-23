#!/usr/bin/env bash

source secvars.sh

echo Sync from $DEPLOY_HOST:/home/wikipedia-pod/already_posted.txt...

scp root@$DEPLOY_HOST:/home/wikipedia-pod/already_posted.txt ../already_posted.txt
