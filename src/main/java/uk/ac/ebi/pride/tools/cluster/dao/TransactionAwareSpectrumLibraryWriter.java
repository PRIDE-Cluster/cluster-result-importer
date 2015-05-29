package uk.ac.ebi.pride.tools.cluster.dao;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import uk.ac.ebi.pride.spectracluster.repo.dao.library.ISpectrumLibraryWriteDao;
import uk.ac.ebi.pride.spectracluster.repo.dao.library.SpectrumLibraryWriter;
import uk.ac.ebi.pride.spectracluster.repo.exception.ClusterImportException;
import uk.ac.ebi.pride.spectracluster.repo.model.SpectrumLibraryDetail;

import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class TransactionAwareSpectrumLibraryWriter implements ISpectrumLibraryWriteDao {
    private final SpectrumLibraryWriter spectrumLibraryWriter;
    private final TransactionTemplate transactionTemplate;

    public TransactionAwareSpectrumLibraryWriter(DataSourceTransactionManager transactionManager) {
        this.spectrumLibraryWriter = new SpectrumLibraryWriter(transactionManager.getDataSource());
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void saveSpectrumLibraries(final List<SpectrumLibraryDetail> spectrumLibraryDetails) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    spectrumLibraryWriter.saveSpectrumLibraries(spectrumLibraryDetails);
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting cluster repo statistics";
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }

    @Override
    public void saveSpectrumLibrary(final SpectrumLibraryDetail spectrumLibraryDetail) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    spectrumLibraryWriter.saveSpectrumLibrary(spectrumLibraryDetail);
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting cluster repo statistics";
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }


}
