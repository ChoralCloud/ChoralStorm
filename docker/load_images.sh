#!/bin/bash
# loads all the images for choral storm from the drive

docker load -i /network/seng466_shared/redis
docker load -i /network/seng466_shared/cassandra
docker load -i /network/seng466_shared/storm
docker load -i /network/seng466_shared/kafka
docker load -i /network/seng466_shared/zookeeper
docker load -i /network/seng466_shared/elasticsearch
docker load -i /network/seng466_shared/debian
docker load -i /network/seng466_shared/redis2
docker load -i /network/seng466_shared/openjdk

