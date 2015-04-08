package uk.ac.ebi.pride.tools.cluster.dao;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import uk.ac.ebi.pride.spectracluster.repo.dao.stats.ClusterRepoStatisticsWriter;
import uk.ac.ebi.pride.spectracluster.repo.dao.stats.IClusterRepoStatisticsWriteDao;
import uk.ac.ebi.pride.spectracluster.repo.exception.ClusterImportException;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterRepoStatistics;

import java.util.List;

/**
 * A wrapper class that makes ClusterRepoStatisticsWriter transaction aware
 *
 * @author Rui Wang
 * @version $Id$
 */
public class TransactionAwareClusterRepoStatisticsWriter implements IClusterRepoStatisticsWriteDao {
    private final ClusterRepoStatisticsWriter clusterRepoStatisticsWriter;
    private final TransactionTemplate transactionTemplate;

    public TransactionAwareClusterRepoStatisticsWriter(DataSourceTransactionManager transactionManager) {
        this.clusterRepoStatisticsWriter = new ClusterRepoStatisticsWriter(transactionManager.getDataSource());
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void saveStatistics(final List<ClusterRepoStatistics> statistics) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    clusterRepoStatisticsWriter.saveStatistics(statistics);
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting cluster repo statistics";
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }
}
