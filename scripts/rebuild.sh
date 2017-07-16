#!/usr/bin/env bash

# use --remote to run with remote settings

docker exec nimbus storm kill ChoralTopology

start_timeout_exceeded=false
count=0
step=10
while $(dirname "$0")/run.sh $1 | grep "already exists"; do
    echo "Submitting topology"
    sleep $step;
    count=$(expr $count + $step)
    if [ $count -gt $START_TIMEOUT ]; then
        start_timeout_exceeded=true
        break
    fi
done