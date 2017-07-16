#!/usr/bin/env bash

mvn package

docker cp target/choralstorm-1.0-jar-with-dependencies.jar nimbus:/topologies/ChoralTopology.jar
docker cp target/choralstorm-1.0-jar-with-dependencies.jar nimbus:/topologies/ChoralTopology.jar

if [[ $1 == "--local" ]] ; then
    docker exec nimbus /bin/bash /scripts/local-storm-submit-topology.sh
else
    docker exec nimbus /bin/bash /scripts/remote-storm-submit-topology.sh
fi