# ChoralStorm
ChoralStorm is designed to handle a large number of requests from IoT devices. In general, the IoT devices will send 
data with a given format described by Choral Protocol. This system will then parse the data and will push the data to a 
storage layer consisting of two parts: a real time view and the raw data persistence. The real time view can be used to 
retrieve live data from these IoT devices. The raw data storage can be used to make special queries such as "Give me all 
of the temperatures for device 1 ranging from 2015-2017".

### How it works
Each device will send a certain format defined by the Choral Protocol to a central endpoint, namely [ChoralAllegro]. 
The ChoralStorm cluster is composed of the following: Kafka, Zookeeper, Storm, Cassandra, and Redis. Kafka receives 
messages from ChoralAllegro, while Storm consumes the messages. Additionally, Storm will push raw data to Cassandra and
Redis.

![](/architecture.png)

### System Requirements
Minimum hardware requirements
* Memory: 16+ GBs
* Disk: 10+ GBs
* CPU: 2+ cores

Software requirements
* docker and docker-compose
* Java 8

### Running the cluster locally
1. If its the first time running:
    ```
    docker/build.sh
    ```
1. Make sure docker images are not running: `docker-compose -f docker/docker-compose.yml down`
1. Run docker images: `docker-compose -f docker/docker-compose.yml up -d`
1. To see if it is working (make sure data is streaming first):
    ```
    docker exec -it cassandra /usr/bin/cqlsh cassandra
    select * from choraldatastream.raw_data;
    ```
1. If running Storm with debugging:
    * Update pom.xml to `<provided.scope>compile</provided.scope>` under properties
    * Generate topology `mvn package`
    * Run jar: `java -cp choralstorm-1.0-jar-with-dependencies.jar storm.ChoralTopology choraldatastream local`
1. If running Storm without debugging:
    * Update pom.xml to `<provided.scope>provided</provided.scope>` under properties
    * Submit topology `scripts/run.sh`
    * View topology: [http://localhost:8080](http://localhost:8080)
    
    #### Notes
    - if you are developing and changing ChoralTopology.java, you need to resubmit the topology
        ```
        docker exec nimbus storm kill ChoralTopology
        scripts/run.sh
        ```

### Running the cluster remotely
![](/choralcluster.png)
1. SSH into choralcluster1 `ssh -i id_seng466 root@choralcluster1`
1. The `choralcluster` service should be running 
    ```
    systemctl status choralcluster1 #check status
    systemctl start choralcluster1 #start service
    systemctl stop choralcluster1 #stop service
    systemctl restart choralcluster #restart service
    ```
1. If service is not running, run the following script `scripts/restart_cluster.sh`
1. Otherwise, submit topology `scripts/run.sh --remote`

    #### Notes
    - if you run into an issue where nimbus leader cannot be found, likely there is something wrong with zookeeper, one 
    way to fix this is to delete all images on cluster 1, cluster 2, cluster 3, rebuild all of the images
        ```
        # for cluster X
        systemctl stop choralclusterX.service
        docker rmi $(docker images -q)
        docker-compose -f clusterX.yml
        systemctl start choralclusterX.service
        ```

At this point, ChoralStorm (Zookeeper, Kafka, Storm) + Cassandra + Redis should be set up and the cluster can now consume data.

### Technologies
ChoralStorm uses these technologies:

* [Apache Kafka] - data stream message broker
* [Apache Storm] - near real time data stream computation system
* [Apache Cassandra] - persistent storage
* [Redis] - real time cache
* [Docker] - cluster container

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
   [ChoralAllegro]: <https://github.com/choralcloud/ChoralAllegro/>
