#!/usr/bin/env bash

echo "Stopping..."
docker stop redis
docker stop cassandra
docker stop kafka
docker stop zookeeper
docker stop ui
docker stop supervisor
docker stop nimbus

if [[ $1 == "--remove" ]] ; then
    echo "Removing..."
    docker rm redis
    docker rm cassandra
    docker rm kafka
    docker rm zookeeper
    docker rm ui
    docker rm supervisor
    docker rm nimbus
fi

if [[ $1 == "--rmi" ]] ; then
    echo "Removing..."
    docker rmi redis -f
    docker rmi cassandra -f
    docker rmi kafka -f
    docker rmi zookeeper -f
    docker rmi storm -f
fi