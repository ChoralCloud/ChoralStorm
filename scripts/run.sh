#!/usr/bin/env bash

mvn package
docker cp target/choralstorm-1.0-jar-with-dependencies.jar nimbus:/topologies/ChoralTopology.jar
docker exec nimbus /bin/bash /scripts/storm-submit-topology.sh