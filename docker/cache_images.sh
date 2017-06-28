#!/bin/bash
# This script saves all of the images to the network drive on uvic's network

docker save -o /network/seng466_shared/docker_images/redis 4e482b
docker save -o /network/seng466_shared/docker_images/cassandra cassandra
docker save -o /network/seng466_shared/docker_images/storm storm
docker save -o /network/seng466_shared/docker_images/kafka kafka
docker save -o /network/seng466_shared/docker_images/zookeeper zookeeper
docker save -o /network/seng466_shared/docker_images/elasticsearch elasticsearch
docker save -o /network/seng466_shared/docker_images/debian debian
docker save -o /network/seng466_shared/docker_images/redis2 4e482b
docker save -o /network/seng466_shared/docker_images/openjdk openjdk
