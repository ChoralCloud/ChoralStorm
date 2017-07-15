#!/usr/bin/env bash

for i in {1..4};
    do ssh -i ~/.ssh/id_seng466 root@choralcluster$i "systemctl restart choralcluster$i"; done