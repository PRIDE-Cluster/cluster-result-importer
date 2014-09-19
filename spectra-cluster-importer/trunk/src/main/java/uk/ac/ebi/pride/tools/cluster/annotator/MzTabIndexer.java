package uk.ac.ebi.pride.tools.cluster.annotator;

import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.tools.cluster.utils.PeptideUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Index a mzTab file for fast access
 *
 * @author Rui Wang
 * @version $Id$
 */
public class MzTabIndexer {
    private final MZTabFile mzTab;
    private final Map<String, PSM> psmIdToPSM = new HashMap<String, PSM>();
    private final Map<String, Set<PSM>> spectrumIdToPSM = new HashMap<String, Set<PSM>>();
    private final Map<String, MsRun> fileToMsRun = new HashMap<String, MsRun>();

    public MzTabIndexer(File mzTabFile) throws IOException {
        MZTabFileParser mzTabFileParser = new MZTabFileParser(mzTabFile, System.out);
        mzTab = mzTabFileParser.getMZTabFile();

        // check whether there is any parsing error
        MZTabErrorList errorList = mzTabFileParser.getErrorList();
        if (errorList.isEmpty()) {
            index();
        } else {
            throw new IOException("mzTab file contains errors: " + mzTabFile.getAbsolutePath());
        }
    }

    public MzTabIndexer(MZTabFile mzTab) {
        this.mzTab = mzTab;
        index();
    }

    private void index() {
        // generate map from file name to ms run
        SortedMap<Integer, MsRun> msRunMap = mzTab.getMetadata().getMsRunMap();
        for (MsRun msRun : msRunMap.values()) {
            String msRunFile = msRun.getLocation().getFile();
            String msRunFileName = new File(msRunFile).getName();
            fileToMsRun.put(msRunFileName, msRun);
        }

        // generate map from spectrum id to PSM
        Collection<PSM> psms = mzTab.getPSMs();
        for (PSM psm : psms) {
            SplitList<SpectraRef> spectraRefs = psm.getSpectraRef();
            if (spectraRefs != null && !spectraRefs.isEmpty()) {
                String spectrumId = spectraRefs.get(0).toString();
                Set<PSM> spectrumRelatedPsms = spectrumIdToPSM.get(spectrumId);
                if (spectrumRelatedPsms == null) {
                    spectrumRelatedPsms = new HashSet<PSM>();
                    spectrumIdToPSM.put(spectrumId, spectrumRelatedPsms);
                }

                PSM cachedPSM = storeAndReturnPSm(psm);
                spectrumRelatedPsms.add(cachedPSM);
            }
        }
    }

    private PSM storeAndReturnPSm(PSM psm) {
        String psmId = createPSMId(psm);
        if (psmIdToPSM.containsKey(psmId)) {
            return psmIdToPSM.get(psmId);
        } else {
            psmIdToPSM.put(psmId, psm);
            return psm;
        }
    }

    private String createPSMId(PSM psm) {
        return psm.getPSM_ID() + "_"
                + psm.getAccession() + "_"
                + PeptideUtils.cleanPeptideSequence(psm.getSequence());
    }

    public int getNumberOfMsRuns() {
        return fileToMsRun.size();
    }

    public MsRun findMsRunUsingFileName(String fileName) {
        return fileToMsRun.get(fileName);
    }

    public Set<PSM> findPSMUsingSpectrumId(String spectrumId) {
        HashSet<PSM> psms = new HashSet<PSM>();

        Set<PSM> cachedPSMs = spectrumIdToPSM.get(spectrumId);
        if (cachedPSMs != null)
            psms.addAll(cachedPSMs);

        return psms;
    }
}
