#!/usr/bin/env bash

echo "Waiting for cassandra"
echo "describe cluster;" > /tmp/dc
echo "describe choraldatastream;" > /tmp/dt
for i in {1..5}; do
  if cqlsh -f /tmp/dc 2>&1 | grep "Cluster"; then
    if cqlsh -f /tmp/dt 2>&1 | grep "not found"; then
      echo "Creating choraldatastream"
      cqlsh -f /scripts/init.cql
    else
      echo "Found choraldatastream"
      exit 0
    fi
  else
    sleep 10
  fi
done
exit 1