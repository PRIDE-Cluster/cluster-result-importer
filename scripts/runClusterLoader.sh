#!/bin/bash

##### OPTIONS
# (required)  path to .clustering file
CLUSTER_FILE_PATH=$1


##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTER-LOADER"
# memory limit in MGb
MEMORY_LIMIT=10000
# LSF email notification
JOB_EMAIL="rwang@ebi.ac.uk"


##### RUN it on the production LSF cluster #####
##### NOTE: you can change LSF group to modify the number of jobs can be run concurrently #####
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh6 -g /pride_cluster_loader -u ${JOB_EMAIL} -J ${JOB_NAME} java -Xmx${MEMORY_LIMIT}m -cp ${project.build.finalName}.jar uk.ac.ebi.pride.tools.cluster.loader.ClusteringFileLoader -in ${CLUSTER_FILE_PATH}
