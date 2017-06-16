#!/usr/bin/env bash

if ! docker network create --driver bridge choralstorm | grep "already exists"; then
  docker network create --driver bridge choralstorm
fi

docker build -t zookeeper zookeeper
docker build -t kafka kafka
docker build -t storm storm
docker build -t cassandra cassandra
docker build -t redis redis

