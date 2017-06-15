#!/usr/bin/env bash
# cassandra(9042) redis(6379) storm(6627, 6700, 6701, 6702, 8080), zookeeper(2181), kafka(9092)
# docker run -p 2181:2181 -p 9092:9092 -p 6700:6700 -p 6701:6701 -p 6702:6702 -p 8080:8080 -p 6627:6627 -p 6379:6379 -p 9042:9042 choral/choralstorm

# Run cluster
docker run --net="host" -d --name choralstorm choralstorm

docker exec choralstorm /bin/bash /scripts/kafka-create-topic.sh