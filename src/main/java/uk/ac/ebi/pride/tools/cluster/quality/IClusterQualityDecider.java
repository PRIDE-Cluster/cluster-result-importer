package uk.ac.ebi.pride.tools.cluster.quality;

import uk.ac.ebi.pride.spectracluster.repo.model.ClusterQuality;

/**
 * Interface for deciding the quality of a cluster
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterQualityDecider<T> {

    ClusterQuality decideQuality(T cluster);
}
