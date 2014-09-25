package uk.ac.ebi.pride.tools.cluster.repo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import uk.ac.ebi.pride.tools.cluster.model.ClusterSummary;
import uk.ac.ebi.pride.tools.cluster.model.ClusteredPSMSummary;
import uk.ac.ebi.pride.tools.cluster.model.ClusteredSpectrumSummary;
import uk.ac.ebi.pride.tools.cluster.utils.ClusterSummaryUtils;
import uk.ac.ebi.pride.tools.cluster.utils.CollectionUtils;
import uk.ac.ebi.pride.tools.cluster.utils.Constants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterWriteDao implements IClusterWriteDao{

    private static final Logger logger = LoggerFactory.getLogger(ClusterWriteDao.class);

    private final JdbcTemplate template;
    private final TransactionTemplate transactionTemplate;

    public ClusterWriteDao(DataSourceTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.template = new JdbcTemplate(transactionManager.getDataSource());
    }

    @Override
    public void saveClusters(final List<ClusterSummary> clusters) {
        for (ClusterSummary cluster : clusters) {
            saveCluster(cluster);
        }
    }

    @Override
    public void saveCluster(final ClusterSummary cluster) {
        logger.debug("Insert a cluster into database: {}", cluster.toString());

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    saveClusterSummary(cluster);

                    updateSpectrumIdForClusteredSpectra(cluster);

                    validateClusterMappings(cluster);

                    ClusterSummaryUtils.updateClusteredPSMStatistics(cluster);

                    saveClusteredSpectra(cluster.getClusteredSpectrumSummaries());

                    saveClusteredPSMs(cluster.getClusteredPSMSummaries());

                } catch (Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting cluster: " + cluster.toString();
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }

    private void saveClusterSummary(final ClusterSummary cluster) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(template);

        final byte[] mzByteArray = cluster.getConsensusSpectrumMz();
        final byte[] intensityByteArray = cluster.getConsensusSpectrumIntensity();

        simpleJdbcInsert.withTableName("spectrum_cluster").usingGeneratedKeyColumns("cluster_pk");

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("avg_precursor_mz", cluster.getAveragePrecursorMz());
        parameters.put("avg_precursor_charge", cluster.getAveragePrecursorCharge());
        parameters.put("number_of_spectra", cluster.getNumberOfSpectra());
        parameters.put("max_ratio", cluster.getMaxPeptideRatio());
        parameters.put("consensus_spectrum_mz",mzByteArray);
        parameters.put("consensus_spectrum_intensity", intensityByteArray);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource(parameters);
        parameterSource.registerSqlType("consensus_spectrum_mz", Types.BINARY);
        parameterSource.registerSqlType("consensus_spectrum_intensity", Types.BINARY);

        Number key = simpleJdbcInsert.executeAndReturnKey(parameterSource);
        long clusterId = key.longValue();
        cluster.setId(clusterId);

        // update cluster id for all the clustered spectra
        List<ClusteredSpectrumSummary> clusteredSpectrumSummaries = cluster.getClusteredSpectrumSummaries();
        for (ClusteredSpectrumSummary clusteredSpectrumSummary : clusteredSpectrumSummaries) {
            clusteredSpectrumSummary.setClusterId(clusterId);
        }
    }

    private void updateSpectrumIdForClusteredSpectra(final ClusterSummary cluster) {
        String SELECT_QUERY = "select spectrum.spectrum_pk, spectrum.spectrum_ref, psm.psm_pk, psm.sequence from spectrum " +
                "join psm on (spectrum.spectrum_pk = psm.spectrum_fk) where spectrum.spectrum_ref in ";

        List<String> queries = concatenateSpectrumReferencesForQuery(cluster.getClusteredSpectrumSummaries(), 100);
        for (final String query : queries) {
            String sql = SELECT_QUERY + "(" + query + ")";
            template.query(sql, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    long spectrumPk = rs.getLong(1);
                    String spectrumRef = rs.getString(2);
                    long psmPk = rs.getLong(3);
                    String sequence = rs.getString(4);

                    ClusteredSpectrumSummary clusteredSpectrumSummary = cluster.getClusteredSpectrumSummary(spectrumRef);
                    clusteredSpectrumSummary.setSpectrumId(spectrumPk);

                    ClusteredPSMSummary clusteredPSMSummary = new ClusteredPSMSummary();
                    clusteredPSMSummary.setClusterId(cluster.getId());
                    clusteredPSMSummary.setPsmId(psmPk);
                    clusteredPSMSummary.setSpectrumId(spectrumPk);
                    clusteredPSMSummary.setSequence(sequence);
                    cluster.addClusteredPSMSummary(clusteredPSMSummary);
                }
            });
        }
    }

    private void validateClusterMappings(final ClusterSummary cluster) {
        List<String> invalidSpectrumReference = new ArrayList<String>();
        List<ClusteredSpectrumSummary> clusteredSpectrumSummaries = cluster.getClusteredSpectrumSummaries();

        for (ClusteredSpectrumSummary clusteredSpectrumSummary : clusteredSpectrumSummaries) {
            if (clusteredSpectrumSummary.getSpectrumId() == null) {
                invalidSpectrumReference.add(clusteredSpectrumSummary.getReferenceId());
            }
        }

        if (!invalidSpectrumReference.isEmpty()) {
            String message = "Spectrum references not found in database: " + StringUtils.join(invalidSpectrumReference, Constants.COMMA);
            logger.error(message);
            throw new IllegalStateException(message);
        }

        if (clusteredSpectrumSummaries.size() > cluster.getClusteredPSMSummaries().size()) {
            String message = "PSM not found in database for cluster that contains spectrum reference: "
                    + clusteredSpectrumSummaries.get(0).getReferenceId();
            logger.error(message);
            throw new IllegalStateException(message);
        }
    }


    private List<String> concatenateSpectrumReferencesForQuery(final List<ClusteredSpectrumSummary> clusteredSpectra, int limit) {
        List<String> queries = new ArrayList<String>();

        List<List<ClusteredSpectrumSummary>> chunks = CollectionUtils.chunks(clusteredSpectra, limit);
        for (List<ClusteredSpectrumSummary> chunk : chunks) {
            String query = "";
            for (ClusteredSpectrumSummary clusteredSpectrumSummary : chunk) {
                query += "'" + clusteredSpectrumSummary.getReferenceId() + "',";
            }
            queries.add(query.substring(0, query.length() - 1));
        }

        return queries;
    }

    private void saveClusteredSpectra(final List<ClusteredSpectrumSummary> clusteredSpectra) {
        String INSERT_QUERY = "INSERT INTO cluster_has_spectrum (cluster_fk, spectrum_fk, similarity) " +
                              "VALUES (?, ?, ?)";

        template.batchUpdate(INSERT_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ClusteredSpectrumSummary spectrumSummary = clusteredSpectra.get(i);
                ps.setLong(1, spectrumSummary.getClusterId());
                ps.setLong(2, spectrumSummary.getSpectrumId());
                ps.setFloat(3, spectrumSummary.getSimilarityScore());
            }

            @Override
            public int getBatchSize() {
                return clusteredSpectra.size();
            }
        });
    }

    private void saveClusteredPSMs(final List<ClusteredPSMSummary> clusteredPSMSummaries) {
        String INSERT_QUERY = "INSERT INTO cluster_has_psm (cluster_fk, psm_fk, ratio, rank) " +
                              "VALUES (?, ?, ?, ?)";

        template.batchUpdate(INSERT_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ClusteredPSMSummary clusteredPSMSummary = clusteredPSMSummaries.get(i);
                ps.setLong(1, clusteredPSMSummary.getClusterId());
                ps.setLong(2, clusteredPSMSummary.getPsmId());
                ps.setFloat(3, clusteredPSMSummary.getPsmRatio());
                ps.setInt(4, clusteredPSMSummary.getRank());
            }

            @Override
            public int getBatchSize() {
                return clusteredPSMSummaries.size();
            }
        });
    }
}
