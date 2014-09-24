package uk.ac.ebi.pride.tools.cluster.repo;

import uk.ac.ebi.pride.tools.cluster.model.ClusterSummary;

import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterWriteDao {

    void saveClusters(List<ClusterSummary> clusters);

    void saveCluster(ClusterSummary cluster);
}
