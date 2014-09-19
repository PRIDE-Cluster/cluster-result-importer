package uk.ac.ebi.pride.tools.cluster.importer;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Import cluster into database
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterDBImporter implements IClusterImporter {
    private final JdbcTemplate jdbcTemplate;
    private final DataSourceTransactionManager transactionManager;

    public ClusterDBImporter(DataSourceTransactionManager transactionManager) {
        DataSource dataSource = transactionManager.getDataSource();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionManager = transactionManager;
    }


    @Override
    public void save(ICluster cluster) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);

        try {
            // save cluster
            Number clusterId = saveCluster(cluster);

            // save each spectrum reference
            Map<Number, ISpectrumReference> spectrumReferenceIdMap = new HashMap<Number, ISpectrumReference>();
            for (ISpectrumReference spectrumReference : cluster.getSpectrumReferences()) {
                Number spectrumId = saveSpectrumReference(spectrumReference);
                spectrumReferenceIdMap.put(spectrumId, spectrumReference);
            }
//
//            // save cluster spectrum mappings
//            saveClusterToSpectrumMappings(clusterId, spectrumReferenceIdMap);

            // commit transaction
            transactionManager.commit(transaction);
        } catch (Exception ex) {
            transactionManager.rollback(transaction);
            String message = "Error loading cluster: " + cluster.toString();
            throw new ClusterImportException(message, ex);
        }
    }

    private Number saveCluster(final ICluster cluster) throws IOException {
        final String INSERT_SQL = "INSERT INTO spectrum_cluster (avg_precursor_mz, avg_precursor_charge, consensus_spectrum_mz, consensus_spectrum_intensity, " +
                                                                 "number_of_spectra, max_ratio) VALUES (?, -1, ?, ?, ?, ?)";

        final byte[] mzByteArray = convertToByteArray(cluster.getConsensusMzValues());
        final byte[] intensityByteArray = convertToByteArray(cluster.getConsensusIntensValues());

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {

                PreparedStatement ps = con.prepareStatement(INSERT_SQL, new String[]{"cluster_pk"});
                ps.setFloat(1, cluster.getAvPrecursorMz());
                ps.setBlob(2, new ByteArrayInputStream(mzByteArray));
                ps.setBlob(3, new ByteArrayInputStream(intensityByteArray));
                ps.setInt(4, cluster.getSpecCount());
                ps.setFloat(5, cluster.getMaxRatio());

                return ps;
            }
        };

        jdbcTemplate.update(preparedStatementCreator, keyHolder);

        return keyHolder.getKey();
    }

    private byte[] convertToByteArray(Collection<Float> content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(content);
        return bos.toByteArray();
    }


    private Number saveSpectrumReference(final ISpectrumReference spectrumReference) {
        final String INSERT_SQL = "INSERT INTO spectrum (spectrum_ref, precursor_mz, precursor_charge, is_identified) SELECT ?, ?, ?, ? FROM DUAL WHERE NOT EXISTS(SELECT * FROM spectrum WHERE spectrum_ref=?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(INSERT_SQL, new String[]{"spectrum_pk"});
                ps.setString(1, spectrumReference.getSpectrumId());
                ps.setFloat(2, spectrumReference.getPrecursorMz());
                ps.setInt(3, spectrumReference.getCharge());
                ps.setBoolean(4, true);
                ps.setString(5, spectrumReference.getSpectrumId());

                return ps;
            }
        };

        jdbcTemplate.update(preparedStatementCreator, keyHolder);

        return keyHolder.getKey();
    }


    private void saveClusterToSpectrumMappings(final Number clusterId, final Map<Number, ISpectrumReference> spectraSimilarity) {
        final String INSERT_SQL = "INSERT INTO cluster_has_spectrum (cluster_fk, spectrum_fk, similarity) VALUES (?, ?, ?)";

        Set<Map.Entry<Number, ISpectrumReference>> entrySet = spectraSimilarity.entrySet();
        final LinkedList<Map.Entry<Number, ISpectrumReference>> entries = new LinkedList<Map.Entry<Number, ISpectrumReference>>(entrySet);

        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, clusterId.longValue());
                ps.setLong(2, entries.get(i).getKey().longValue());
//                ps.setFloat(3, entries.get(i).getValue().floatValue());
                ps.setFloat(3, 0.0f);
            }

            @Override
            public int getBatchSize() {
                return spectraSimilarity.size();
            }
        });
    }
}
