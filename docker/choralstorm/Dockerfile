# Kafka, Zookeeper, and Storm

FROM openjdk:8

ENV DEBIAN_FRONTEND noninteractive
ENV SCALA_VERSION 2.12
ENV KAFKA_VERSION 0.10.2.1
ENV STORM_VERSION 1.1.0
ENV KAFKA_HOME /opt/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION"
ENV STORM_HOME /opt/apache-storm-"$STORM_VERSION"

# Install Kafka, Zookeeper, Storm and others
RUN apt-get update && \
    apt-get install -y wget supervisor dnsutils && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get clean

RUN wget -q http://apache.mirrors.spacedump.net/kafka/"$KAFKA_VERSION"/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz -O /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz && \
    tar xfz /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz -C /opt && \
    rm /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz

RUN wget -q http://apache.mirrors.spacedump.net/storm/apache-storm-"$STORM_VERSION"/apache-storm-"$STORM_VERSION".tar.gz -O /tmp/apache-storm-"$STORM_VERSION".tar.gz && \ 
    tar xfz /tmp/apache-storm-"$STORM_VERSION".tar.gz -C /opt && \
    rm /tmp/apache-storm-"$STORM_VERSION".tar.gz

COPY configs/zookeeper.properties $KAFKA_HOME/config/zookeeper.properties
COPY configs/server.properties $KAFKA_HOME/config/server.properties
COPY configs/storm.yaml $STORM_HOME/conf/storm.yaml

COPY scripts/start-zookeeper.sh /usr/bin/start-zookeeper.sh
COPY scripts/start-kafka.sh /usr/bin/start-kafka.sh
COPY scripts/start-storm-nimbus.sh /usr/bin/start-storm-nimbus.sh
COPY scripts/start-storm-supervisor.sh /usr/bin/start-storm-supervisor.sh
COPY scripts/start-storm-ui.sh /usr/bin/start-storm-ui.sh
COPY jars/kafkaserver.jar /kafkaserver/kafkaserver.jar

ADD supervisor/zookeeper.conf supervisor/kafka.conf supervisor/storm-nimbus.conf supervisor/storm-supervisor.conf supervisor/storm-ui.conf supervisor/kafka-server.conf etc/supervisor/conf.d/

# 2181 - Kafka broker, 9002 - zookeeper, 3030 - KafkaServer, 670X - Storm
EXPOSE 2181 9002 3030 6700 6701 6702 6703 8080

CMD ["supervisord", "-n"]
