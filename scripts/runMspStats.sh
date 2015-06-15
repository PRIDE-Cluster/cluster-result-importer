#!/bin/bash

echo -ne "Filename\ttotal_spectra\tunique_sequences\n"
for MSP_FILE in "$@"; do
        UNIQUE_SEQUENCES=`grep "^Name:" "$MSP_FILE" | sed 's|Name: \(.*\)/.*|\1|' | sort | uniq | wc -l`
        SPECTRA=`grep -c "^Name:" "$MSP_FILE"`

        echo -e "$MSP_FILE\t$SPECTRA\t$UNIQUE_SEQUENCES"
done
