#!/usr/bin/env bash

if [ ! -f target/choralstorm-1.0-jar-with-dependencies.jar ]; then
  mvn package
fi

docker cp target/choralstorm-1.0-jar-with-dependencies.jar nimbus:/topologies/ChoralTopology.jar
docker exec nimbus /bin/bash /scripts/storm-submit-topology.sh
