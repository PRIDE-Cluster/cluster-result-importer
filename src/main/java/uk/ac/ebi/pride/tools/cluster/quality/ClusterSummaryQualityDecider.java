package uk.ac.ebi.pride.tools.cluster.quality;

import uk.ac.ebi.pride.spectracluster.repo.model.ClusterQuality;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterSummary;

/**
 * Decide the quality of a cluster for a cluster summary
 *
 * NOTE: the threshold will be inclusive
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterSummaryQualityDecider implements IClusterQualityDecider<ClusterSummary> {

    private final int spectrumCountThreshold;
    private final int projectCountThreshold;
    private final float maxPeptideRatioThreshold;

    public ClusterSummaryQualityDecider(int spectrumCountThreshold, int projectCountThreshold, float maxPeptideRatioThreshold) {
        this.spectrumCountThreshold = spectrumCountThreshold;
        this.projectCountThreshold = projectCountThreshold;
        this.maxPeptideRatioThreshold = maxPeptideRatioThreshold;
    }

    @Override
    public ClusterQuality decideQuality(ClusterSummary cluster) {

        int numberOfSpectra = cluster.getTotalNumberOfSpectra();
        int numberOfProjects = cluster.getNumberOfProjects();
        float maxPeptideRatio = cluster.getMaxPeptideRatio();

        boolean belowSpectrumCountThreshold = numberOfSpectra < spectrumCountThreshold;
        boolean belowProjectCountThreshold = numberOfProjects < projectCountThreshold;
        boolean belowMaxPeptideRatioThreshold = maxPeptideRatio < maxPeptideRatioThreshold;

        // check for high quality cluster
        if (!belowMaxPeptideRatioThreshold && !belowProjectCountThreshold && !belowSpectrumCountThreshold) {
            return ClusterQuality.HIGH;
        }

        // check for medium qualit cluster
        if (!belowMaxPeptideRatioThreshold && belowProjectCountThreshold && !belowSpectrumCountThreshold) {
            return ClusterQuality.MEDIUM;
        }

        return ClusterQuality.LOW;
    }
}
