#!/usr/bin/env bash

$STORM_HOME/bin/storm jar /topologies/*.jar storm.ChoralTopology $KAFKA_TOPIC remote cluster