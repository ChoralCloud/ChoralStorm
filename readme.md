# ChoralStorm POC
ChoralStorm is a proof of concept designed to handle a large number of requests from IoT devices. In general,
the IoT devices will send data with a given format described by Choral Protocol. This system will then 
parse the data and do general computations such as moving average, max, min. Furthermore, the system will push
the data to a storage layer consisting of two parts: a real time view and the raw data persistence. The real time
view can be used to retrieve live data from these IoT devices such as current value, average, min, and max. The raw
data storage can be used to make special queries such as "Give me all of the temperatures for device 1 ranging from 
2015-2017".

### How it works
The architecture below shows how data is consumed by the user's devices. Each device will send a certain format
defined by the Choral Protocol to a central endpoint. In this case, it is a single go server that essentially 
relays the information to each "cluster". A cluster is composed of the following: HTTP Server, Kafka, Zookeeper, and Storm.
The HTTP Server in the cluster listens for any request from the central endpoint and adds the message to the topic. Storm
then computes the information, and stores the data in the real time view and persistent storage.

![](/architecture.png)

### Technologies
ChoralStorm uses these technologies:

* [Apache Kafka] - data stream message broker
* [Apache Storm] - near real time data stream computation system
* [Apache Cassandra] - persistent storage
* [Redis] - real time cache
* [Go] - "load balancing" server and endpoint for devices 

### Todo
 - Automate and containerize everything
 - Determine more insights on data stream
 - Figure out how to make custom queries
 
### Cluster Installation
These installation steps will get a cluster running with one instance of Kafka, Storm, Cassandra
1. Download [Apache Kafka], unzip in project directory
1. Replace `$KAFKA_DIR/config/zookeeper.properties` and `$KAFKA_DIR/config/server.properties` with the ones found in this repository
1. Download [Apache Storm], unzip in project directory
1. Replace `$STORM_DIR/conf/storm.yaml` with the one found in this repository
1. Download [Apache Cassandra], unzip in project directory
1. Download [Redis], unzip in project directory and `make`
1. Replace `$CASSANDRA_DIR/conf/cassandra.yaml` with the one found in this repository
1. `$KAFKA_DIR/bin/zookeeper-server-start.sh config/zookeeper.properties`
1. `$KAFKA_DIR/bin/kafka-server-start.sh config/server.properties`
1. `$KAFKA_DIR/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic TOPIC_NAME`
1. `$STORM_DIR/bin/storm nimbus`
1. `$STORM_DIR/bin/storm supervisor`
1. `$CASSANDRA_DIR/bin/cassandra`
1. Create a table for Cassandra: `CREATE TABLE choraldatastream.raw_data (device_id text, device_data text, device_timestamp timestamp, time timestamp, primary key((device_id),device_timestamp));`
1. `$REDIS_DIR/src/redis_server` and `$REDIS_DIR/src/redis_client`
1. Run `KafkaServer` and `ChoralTopology`

At this point, everything should be set up and you can produce data with `main.go`.

License
----
Copyright (c) 2017 Choral

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

   [Apache Kafka]: <http://http://kafka.apache.org/>
   [Apache Storm]: <http://http://storm.apache.org/>
   [Apache Cassandra]: <http://http://cassandra.apache.org/>
   [Redis]: <http://redis.io>
   [Go]: <http://golang.org>
