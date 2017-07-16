#!/usr/bin/env bash

docker network create --driver bridge choralstorm

docker build -t zookeeper zookeeper
docker build -t kafka kafka
docker build -t storm storm
docker build -t cassandra cassandra
docker build -t redis redis
