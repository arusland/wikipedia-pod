#!/usr/bin/env bash

echo "Upload already posted file to $DEPLOY_HOST..."

source ./deploy/secvars.sh

scp already_posted.txt root@$DEPLOY_HOST:/home/wikipedia-pod/already_posted.txt

echo "Already posted file uploaded to server."
