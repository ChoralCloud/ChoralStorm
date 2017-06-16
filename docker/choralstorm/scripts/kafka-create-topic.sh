#!/usr/bin/env bash

if [[ -z "$START_TIMEOUT" ]]; then
    START_TIMEOUT=600
fi

start_timeout_exceeded=false
count=0
step=10
while netstat -lnt | awk '$4 ~ /:'$KAFKA_PORT'$/ {exit 1}'; do
    echo "Waiting for kafka to be ready"
    sleep $step;
    count=$(expr $count + $step)
    if [ $count -gt $START_TIMEOUT ]; then
        start_timeout_exceeded=true
        break
    fi
done

if $start_timeout_exceeded; then
    echo "Not able to auto-create topic (waited for $START_TIMEOUT sec)"
    exit 1
fi

if ! $KAFKA_HOME/bin/kafka-topics.sh --list --zookeeper $ZOOKEEPER | grep "$KAFKA_TOPIC"; then
  $KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper $ZOOKEEPER --replication-factor 1 --partition 1 --topic $KAFKA_TOPIC
fi