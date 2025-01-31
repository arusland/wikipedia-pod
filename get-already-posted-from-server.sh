#!/usr/bin/env bash

echo "Downloading already posted file from $DEPLOY_HOST..."

source ./deploy/secvars.sh

scp -r root@$DEPLOY_HOST:/home/wikipedia-pod/already_posted.txt .

echo "Already posted file downloaded from server."
