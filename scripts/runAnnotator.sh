#!/bin/sh

##### OPTIONS
# (required)  root path to PRIDE Archive production file syste
ARCHIVE_ROOT_FILE_PATH=$1
# (required)  PRIDE Archive project accession
PROJECT_ACCESSION=$2


##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="PRIDE-CLUSTER-ANNOTATION"
# memory limit
MEMORY_LIMIT=15000
# LSF email notification
JOB_EMAIL="${pride.report.email}"


##### RUN it on the production LSF cluster #####
##### NOTE: you can change LSF group to modify the number of jobs can be run concurrently #####
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh6 -g /pride_cluster_annotation -u ${JOB_EMAIL} -J ${JOB_NAME} java -Xmx${MEMORY_LIMIT} -cp ${project.build.finalName}.jar uk.ac.ebi.pride.tools.cluster.annotator.ArchiveProjectAnnotator ${ARCHIVE_ROOT_FILE_PATH} ${PROJECT_ACCESSION}