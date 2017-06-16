#!/usr/bin/env bash

echo "Stopping..."
docker stop choralredis
docker stop choralcassandra
docker stop choralstorm

if [[ $1 == "--remove" ]] ; then
    echo "Removing..."
    docker rm choralredis
    docker rm choralcassandra
    docker rm choralstorm
fi