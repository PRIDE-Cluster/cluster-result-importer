#!/bin/bash

##### OPTIONS
# (required)  path that contains a list of .clustering files
CLUSTERING_DIRECTORY=$1

while read p; do
    ./runClusterLoader.sh ${CLUSTERING_DIRECTORY}/$p
done
