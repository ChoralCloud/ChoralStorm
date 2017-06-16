#!/usr/bin/env bash
# cassandra(9042) redis(6379) storm(6627, 6700, 6701, 6702, 8080), zookeeper(2181), kafka(9092)
# docker run -p 2181:2181 -p 9092:9092 -p 6700:6700 -p 6701:6701 -p 6702:6702 -p 8080:8080 -p 6627:6627 -p 6379:6379 -p 9042:9042 choral/choralstorm

# Run redis
echo "Setting up redis"
if [ ! "$(docker ps -q -f name=choralredis)" ]; then
  docker run -p 6379:6379 \
             --detach \
             --name choralredis \
             choralredis \
             --appendonly yes
else
  docker restart choralredis
fi

# Run cassandra
echo "Setting up cassandra"
if [ ! "$(docker ps -q -f name=choralcassandra)" ]; then
  docker run -p 9042:9042 \
             -p 9160:9160 \
             -p 9142:9142 \
             --detach \
             --name choralcassandra \
             choralcassandra
  docker exec choralcassandra /bin/bash /scripts/init.sh
else
  docker restart choralcassandra
fi

# Run cluster
echo "Setting up storm cluster"
if [ ! "$(docker ps -q -f name=choralstorm)" ]; then
  docker run --detach \
             --name choralstorm \
             --net="host" \
             choralstorm
  docker exec choralstorm /bin/bash /scripts/kafka-create-topic.sh
else
  docker restart choralstorm
fi