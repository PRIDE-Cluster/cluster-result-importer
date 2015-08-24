#!/bin/bash

# The input parameter should be in the format of
# /path/to/spectra/libraries/*

echo -ne "Filename\ttotal_spectra\tunique_sequences\n"
for MSP_FILE in "$@"; do
        UNIQUE_SEQUENCES=`zgrep "^Name:" "$MSP_FILE" | sed 's|Name: \(.*\)/.*|\1|' | sort | uniq | wc -l`
        SPECTRA=`zgrep -c "^Name:" "$MSP_FILE"`
        BASENAME=`basename "$MSP_FILE"`

        echo -e "$BASENAME\t$SPECTRA\t$UNIQUE_SEQUENCES"
done
