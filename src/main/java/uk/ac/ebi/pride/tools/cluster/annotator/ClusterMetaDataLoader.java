package uk.ac.ebi.pride.tools.cluster.annotator;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import uk.ac.ebi.pride.tools.cluster.model.AssaySummary;
import uk.ac.ebi.pride.tools.cluster.model.PSMSummary;
import uk.ac.ebi.pride.tools.cluster.model.SpectrumSummary;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterMetaDataLoader implements IClusterMetaDataLoader {

    private final JdbcTemplate template;

    public ClusterMetaDataLoader(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public void saveAssay(AssaySummary assay) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(template);

        simpleJdbcInsert.withTableName("assay").usingGeneratedKeyColumns("assay_pk");

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("assay_accession", assay.getAccession());
        parameters.put("project_accession", assay.getProjectAccession());

        if (assay.getProjectTitle() != null)
            parameters.put("project_title", assay.getProjectTitle());

        if (assay.getAssayTitle() != null)
            parameters.put("assay_title", assay.getAssayTitle());

        parameters.put("species", assay.getSpecies());
        parameters.put("multi_species", assay.isMultiSpecies());

        if (assay.getTaxonomyId() != null)
            parameters.put("taxonomy_id", assay.getTaxonomyId());

        if (assay.getDisease() != null)
            parameters.put("disease", assay.getDisease());

        if (assay.getTissue() != null)
            parameters.put("tissue", assay.getTissue());

        if (assay.getSearchEngine() != null)
            parameters.put("search_engine", assay.getSearchEngine());

        if (assay.getInstrument() != null)
            parameters.put("instrument", assay.getInstrument());

        parameters.put("biomedical", assay.isBioMedical());

        Number key = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
        assay.setId(key.longValue());
    }

    @Override
    public void saveSpectra(Collection<SpectrumSummary> spectra) {
        for (SpectrumSummary spectrumSummary : spectra) {
            saveSpectrum(spectrumSummary);
        }
    }

    @Override
    public void saveSpectrum(SpectrumSummary spectrum) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(template);

        simpleJdbcInsert.withTableName("spectrum").usingGeneratedKeyColumns("spectrum_pk");

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("assay_fk", spectrum.getAssayId());
        parameters.put("spectrum_ref", spectrum.getReferenceId());
        parameters.put("precursor_mz", spectrum.getPrecursorMz());
        parameters.put("precursor_charge", spectrum.getPrecursorCharge());
        parameters.put("is_identified", spectrum.isIdentified());

        Number key = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
        spectrum.setId(key.longValue());
    }

    @Override
    public void savePSMs(Collection<PSMSummary> psms) {
        for (PSMSummary psm : psms) {
            savePSM(psm);
        }
    }

    @Override
    public void savePSM(PSMSummary psm) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(template);

        simpleJdbcInsert.withTableName("psm").usingGeneratedKeyColumns("psm_pk");

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("spectrum_fk", psm.getSpectrumId());
        parameters.put("assay_fk", psm.getAssayId());
        parameters.put("archive_psm_id", psm.getArchivePSMId());

        if (psm.getModifications() != null)
            parameters.put("modifications", psm.getModifications());

        if (psm.getSearchEngineScores() != null)
            parameters.put("search_engine_scores", psm.getSearchEngineScores());

        if (psm.getSearchDatabase() != null)
            parameters.put("search_database", psm.getSearchDatabase());

        if (psm.getProteinAccession() != null)
            parameters.put("protein_accession", psm.getProteinAccession());

        if (psm.getProteinGroup() != null)
            parameters.put("protein_group", psm.getProteinGroup());

        if (psm.getProteinName() != null)
            parameters.put("protein_name", psm.getProteinName());

        parameters.put("start_position", psm.getStartPosition());
        parameters.put("stop_position", psm.getStopPosition());

        if (psm.getPreAminoAcid() != null)
            parameters.put("pre_amino_acid", psm.getPreAminoAcid());

        if (psm.getPostAminoAcid() != null)
            parameters.put("post_amino_acid", psm.getSequence());

        parameters.put("delta_mz", psm.getDeltaMZ());

        if (psm.getQuantificationLabel() != null)
            parameters.put("quantification_label", psm.getQuantificationLabel());

        Number key = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
        psm.setId(key.longValue());
    }


}
