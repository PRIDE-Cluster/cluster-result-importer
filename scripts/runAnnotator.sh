#!/bin/sh

##### OPTIONS
# (required)  the submission ticket number
TICKET=""
# (optional)  a time stamp in the format: yyy-mm-dd
DATE=""
# (optional)  skip raw/peak zip file extraction
SKIP=""


##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="validation"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS=""
# memory limit
MEMORY_LIMIT=15000
# LSF email notification
JOB_EMAIL="${pride.report.email}"


##### RUN it on the production LSF cluster #####
##### NOTE: you can change LSF group to modify the number of jobs can be run concurrently #####
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh6 -g /pride_cluster_annotation -u ${JOB_EMAIL} -J ${JOB_NAME} java -Xmx${MEMORY_LIMIT} -cp ${project.build.finalName}.jar uk.ac.ebi.pride.tools.cluster.annotator.ArchiveProjectAnnotator ${ARCHIVE_ROOT_FILE_PATH} ${PROJECT_ACCESSION}