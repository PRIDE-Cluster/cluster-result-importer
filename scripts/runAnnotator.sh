#!/bin/sh

##### OPTIONS
# (required)  root path to PRIDE Archive production file syste
ARCHIVE_ROOT_FILE_PATH=$1
# (required)  PRIDE Archive project accession
PROJECT_ACCESSION=$2

##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTER-ANNOTATION-${PROJECT_ACCESSION}C"
# memory limit in MGb
MEMORY_LIMIT=50000
# log file name
LOG_FILE_NAME="${JOB_NAME}"

##### RUN it on the production LSF cluster #####
##### NOTE: you can change LSF group to modify the number of jobs can be run concurrently #####
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh6 -g /pride_cluster_annotation -o /dev/null -J ${JOB_NAME} ./runInJava.sh ./log/${LOG_FILE_NAME}.log ${MEMORY_LIMIT}m -cp ${project.build.finalName}.jar uk.ac.ebi.pride.tools.cluster.annotator.ArchiveProjectAnnotator ${ARCHIVE_ROOT_FILE_PATH} ${PROJECT_ACCESSION}