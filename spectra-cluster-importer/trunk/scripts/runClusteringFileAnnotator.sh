#!/bin/bash

##### OPTIONS
# (required)  path to .clustering file
CLUSTER_FILE_PATH=$1
CLUSTER_FILE_NAME=$(basename "$CLUSTER_FILE_PATH" ".clustering")
OUTPUT_FILE_PATH=$2


##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTERING-FILE-ANNOTATOR"
# memory limit in MGb
MEMORY_LIMIT=10000
# log file name
LOG_FILE_NAME="${JOB_NAME}-${CLUSTER_FILE_NAME}"


##### RUN it on the production LSF cluster #####
##### NOTE: you can change LSF group to modify the number of jobs can be run concurrently #####
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh6 -g /pride_clustering_file_annotator -o /dev/null -J ${JOB_NAME} ./runInJava.sh ./log/${LOG_FILE_NAME}.log ${MEMORY_LIMIT}m -cp ${project.build.finalName}.jar uk.ac.ebi.pride.tools.cluster.exporter.ClusteringFileAnnotator ${CLUSTER_FILE_PATH} ${OUTPUT_FILE_PATH}