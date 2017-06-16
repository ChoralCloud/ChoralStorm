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
* [Docker] - cluster container

### Todo
 - Automate and containerize everything
 - Determine more insights on data stream
 - Figure out how to make custom queries
 - Figure out merge layer
 - Integrate ElasticSearch
 
### Cluster Installation
These installation steps will get a cluster running with one instance of Kafka, Zookeeper, Storm, Cassandra, Redis
1. Ensure you have Cassandra and Redis running (storage layer)
1. Build docker containers `build.sh`
1. Run docker containers `docker-compose up -d`
1. Remote Cluster steps: 
    * Generate topology `mvn package`
    * Submit topology to Storm `submit.sh PATH/TO/TOPOLOGY.JAR`
1. Local Cluster steps:
    * Compile and run with these args: `choraldatastream local`
    * Update pom.xml `<provided.scope>provided</provided.scope>` under properties

At this point, ChoralStorm (Zookeeper, Kafka, Storm) + Cassandra + Redis should be set up and the cluster can now consume data.

### Local Cluster Installation
These installation steps will get a local cluster running with one instance of Kafka, Storm, Cassandra, Redis
1. Ensure you have Cassandra and Redis running (storage layer)
1. Build docker containers `build.sh`
1. Run docker containers `docker-compose up -d`
1. Generate topology `mvn package`
1. Submit topology to Storm `submit.sh PATH/TO/TOPOLOGY.JAR`

At this point, ChoralStorm (Zookeeper, Kafka, Storm) + Cassandra + Redis should be set up and the cluster can now consume data.

### Cluster Information
- Zookeeper = `localhost:2181`
- Kafka = `localhost:9092, default topic=choraldatastream`
- Storm = `localhost:6627, localhost:6700-6702 (Supervisor), localhost:8080 (UI)`
- Cassandra = `localhost:9042, localhost:9142, localhost:9160, default cluster=Choral`
- Redis = `localhost:6379`

### Docker Commands
- `docker-compose up` run containers
- `docker-compose down` stop containers
- `docker-compose rm` remove containers
- `build.sh` builds kafka, zookeeper, storm, redis, and cassandra images
- `stop.sh [--remove]` stops [and removes] images
- `submit.sh PATH/TO/TOPOLOGY.JAR` submits the topology to choralstorm with default topic

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

   [Apache Kafka]: <http://kafka.apache.org/>
   [Apache Storm]: <http://storm.apache.org/>
   [Apache Cassandra]: <http://cassandra.apache.org/>
   [Redis]: <http://redis.io>
   [Docker]: <http://docker.com/>
