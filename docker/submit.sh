#!/usr/bin/env bash

docker cp $1 choralstorm:/topologies/ChoralTopology.jar

docker exec choralstorm /bin/bash /scripts/storm-submit-topology.sh