#!/bin/bash

##### OPTIONS
# (required)  path that contains a list of .clustering files
CLUSTERING_DIRECTORY=$1

for f in ${CLUSTERING_DIRECTORY}/*.clustering
do
    ./runClusterLoader.sh $f
done