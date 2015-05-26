#!/bin/bash

##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTER-STATS"
# memory limit in MGb
MEMORY_LIMIT=10000
# log file name
LOG_FILE_NAME="${JOB_NAME}"


##### RUN it on the production LSF cluster #####
##### NOTE: you can change LSF group to modify the number of jobs can be run concurrently #####
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh6 -g /pride_cluster_loader -o /dev/null -J ${JOB_NAME} ./runInJava.sh ./log/${LOG_FILE_NAME}.log ${MEMORY_LIMIT}m -cp ${project.build.finalName}.jar uk.ac.ebi.pride.tools.cluster.stats.ClusteringDBStatsCollector
