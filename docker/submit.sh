#!/usr/bin/env bash

docker cp $1 nimbus:/topologies/ChoralTopology.jar

docker exec nimbus /bin/bash /scripts/storm-submit-topology.sh