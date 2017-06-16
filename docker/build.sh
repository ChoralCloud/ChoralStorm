#!/usr/bin/env bash

docker build -t choralstorm choralstorm
docker build -t choralcassandra cassandra
docker build -t choralredis redis

