package uk.ac.ebi.pride.tools.cluster.repo;

import uk.ac.ebi.pride.tools.cluster.model.ClusterSummary;

import java.util.List;

/**
 * DAO interface for writing clusters into a repository
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterWriteDao {

    /**
     * Save a list of clusters
     * @param clusters  a list of clusters
     */
    void saveClusters(List<ClusterSummary> clusters);

    /**
     * Save a single cluster
     * @param cluster   given cluster
     */
    void saveCluster(ClusterSummary cluster);
}
