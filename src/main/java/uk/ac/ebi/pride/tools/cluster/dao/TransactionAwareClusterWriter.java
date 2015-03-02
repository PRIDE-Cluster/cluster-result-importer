package uk.ac.ebi.pride.tools.cluster.dao;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.ClusterWriter;
import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.IClusterWriteDao;
import uk.ac.ebi.pride.spectracluster.repo.exception.ClusterImportException;
import uk.ac.ebi.pride.spectracluster.repo.model.AssayDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.PSMDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.SpectrumDetail;

import java.util.List;

/**
 * Delegation class for ClusterWriter to include Transaction management
 *
 * @author Rui Wang
 * @version $Id$
 */
public class TransactionAwareClusterWriter implements IClusterWriteDao {

    private final IClusterWriteDao clusterWriter;
    private final TransactionTemplate transactionTemplate;

    public TransactionAwareClusterWriter(DataSourceTransactionManager transactionManager) {
        this.clusterWriter = new ClusterWriter(transactionManager.getDataSource());
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void saveClusters(List<ClusterDetail> clusters) {
        clusterWriter.saveClusters(clusters);
    }

    @Override
    public void saveCluster(final ClusterDetail cluster) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    clusterWriter.saveCluster(cluster);
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting cluster: " + cluster.toString();
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }

    @Override
    public void saveAssay(final AssayDetail assay) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    clusterWriter.saveAssay(assay);
                } catch(Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting assay: " + assay.getAccession();
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }

    @Override
    public void deleteAssayByProjectAccession(final String projectAccession) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    clusterWriter.deleteAssayByProjectAccession(projectAccession);
                } catch(Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error deleting assay using project accession : " + projectAccession;
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }

    @Override
    public void saveSpectra(final List<SpectrumDetail> spectra) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    clusterWriter.saveSpectra(spectra);
                } catch(Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting a set of spectra: " + spectra.size();
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }

    @Override
    public void saveSpectrum(final SpectrumDetail spectrum) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    clusterWriter.saveSpectrum(spectrum);
                } catch(Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting spectrum: " + spectrum.getReferenceId();
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }

    @Override
    public void savePSMs(final List<PSMDetail> psms) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    clusterWriter.savePSMs(psms);
                } catch(Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting a number of PSMs: " + psms.size();
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }

    @Override
    public void savePSM(final PSMDetail psm) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    clusterWriter.savePSM(psm);
                } catch(Exception ex) {
                    status.setRollbackOnly();
                    String message = "Error persisting PSM: " + psm.getArchivePSMId();
                    throw new ClusterImportException(message, ex);
                }
            }
        });
    }
}
