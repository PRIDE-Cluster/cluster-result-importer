package uk.ac.ebi.pride.tools.cluster.repo;

import uk.ac.ebi.pride.tools.cluster.model.AssaySummary;
import uk.ac.ebi.pride.tools.cluster.model.PSMSummary;
import uk.ac.ebi.pride.tools.cluster.model.SpectrumSummary;

import java.util.List;

/**
 *
 * Cluster metadata loader interface
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface IArchiveMetaDataWriteDao {

    void saveAssay(AssaySummary assay);

    void deleteAssayByProjectAccession(String projectAccession);

    void saveSpectra(List<SpectrumSummary> spectra);

    void saveSpectrum(SpectrumSummary spectrum);

    void savePSMs(List<PSMSummary> psms);

    void savePSM(PSMSummary psm);
}
