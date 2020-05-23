#!/usr/bin/env bash

source secvars.sh

echo Deploying to $DEPLOY_HOST...

scp ../application.properties root@$DEPLOY_HOST:/home/wikipedia-pod/application.properties

scp ./run.sh root@$DEPLOY_HOST:/home/wikipedia-pod/run.sh

scp ../target/wikipedia-pod.jar root@$DEPLOY_HOST:/home/wikipedia-pod/wikipedia-pod.jar

ssh root@$DEPLOY_HOST 'sh /home/wikipedia-pod/run.sh'
