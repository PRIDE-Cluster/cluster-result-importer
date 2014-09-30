#!/bin/bash

##### OPTIONS
# (required)  path that contains a list of .clustering files
CLUSTERING_DIRECTORY=$1
OUTPUT_DIRECTORY=$2

for f in ${CLUSTERING_DIRECTORY}/*.clustering
do
    ./runClusteringFileAnnotator.sh $f ${OUTPUT_DIRECTORY}
done