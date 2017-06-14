# Choralstorm POC
Choralstorm is a proof of concept designed to handle a large number of requests from IoT devices. In general,
the IoT devices will send data with a given format described by Choral Protocol. This system will then 
parse the data and do general computations such as moving average, max, min. Furthermore, the system will push
the data to a storage layer consisting of two parts: a real time view and the raw data persistence. The real time
view can be used to retrieve live data from these IoT devices such as current value, average, min, and max. The raw
data storage can be used to make special queries such as "Give me all of the temperatures for device 1 ranging from 
2015-2017".

![](/architecture.png)

### Technologies
Choralstorm uses these technologies:

* [Apache Kafka] - data stream message broker
* [Apache Storm] - near real time data stream computation system
* [Apache Cassandra] - persistent storage
* [Redis] - real time cache

### Todo
 - Automate and containerize everything
 - Determine more insights on data stream
 - Figure out how to make custom queries
 
### Installation
These installation steps will get a cluster running with one instance of Kafka, Storm, Cassandra
1. Download [Apache Kafka], unzip in project directory
2. Replace `$KAFKA_DIR/config/zookeeper.properties` and `$KAFKA_DIR/config/server.properties` with the ones found in this repository
3. Download [Apache Storm], unzip in project directory
4. Replace `$STORM_DIR/conf/storm.yaml` with the one found in this repository
5. Download [Apache Cassandra], unzip in project directory
6. Replace `$CASSANDRA_DIR/conf/cassandra.yaml` with the one found in this repository
7. `$KAFKA_DIR/bin/zookeeper-server-start.sh config/zookeeper.properties`
8. `$KAFKA_DIR/bin/kafka-server-start.sh config/server.properties`
9. `$STORM_DIR/bin/storm nimbus`
10. `$STORM_DIR/bin/storm supervisor`
11. `$CASSANDRA_DIR/bin/cassandra`
12. Create a table for Cassandra: `CREATE TABLE choraldatastream.raw_data (device_id int, data text, time timestamp, PRIMARY KEY ((device_id), time));`

At this point, everything should be set up and you can produce data with `ChoralProducer` and start a local Storm cluster 
with `ChoralTopology`.

License
----
Copyright (c) 2017 Choral

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

   [Apache Kafka]: <http://http://kafka.apache.org/>
   [Apache Storm]: <http://http://storm.apache.org/>
   [Apache Cassandra]: <http://http://cassandra.apache.org/>
   [Redis]: <http://redis.io>
