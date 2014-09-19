package uk.ac.ebi.pride.tools.cluster.annotator;

import uk.ac.ebi.pride.tools.cluster.model.AssaySummary;
import uk.ac.ebi.pride.tools.cluster.model.PSMSummary;
import uk.ac.ebi.pride.tools.cluster.model.SpectrumSummary;

import java.util.Collection;

/**
 *
 * Cluster metadata loader interface
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterMetaDataLoader {

    void saveAssay(AssaySummary assay);

    void saveSpectra(Collection<SpectrumSummary> spectra);

    void saveSpectrum(SpectrumSummary spectrum);

    void savePSMs(Collection<PSMSummary> psms);

    void savePSM(PSMSummary psm);
}
