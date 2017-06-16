FROM cassandra:3.10

ENV CASSANDRA_PORT 9160
ENV CASSANDRA_CLUSTER_NAME Choral
ENV CASSANDRA_HOME /var/lib/cassandra

RUN mkdir /scripts

COPY configs/cassandra.yaml $CASSANDRA_HOME/conf/cassandra.yaml
COPY scripts/init.sh /scripts/init.sh
COPY scripts/init.cql /scripts/init.cql
