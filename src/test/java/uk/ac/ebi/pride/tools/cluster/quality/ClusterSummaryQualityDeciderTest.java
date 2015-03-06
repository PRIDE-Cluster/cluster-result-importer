package uk.ac.ebi.pride.tools.cluster.quality;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterQuality;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterSummary;

import static org.junit.Assert.assertEquals;

public class ClusterSummaryQualityDeciderTest {

    private ClusterSummaryQualityDecider clusterSummaryQualityDecider;

    @Before
    public void setUp() throws Exception {
        clusterSummaryQualityDecider = new ClusterSummaryQualityDecider(10, 2, 0.7f);
    }

    @Test
    public void testNumberOfSpectra() throws Exception {
        ClusterSummary clusterSummary = new ClusterSummary();
        clusterSummary.setTotalNumberOfSpectra(9);
        clusterSummary.setNumberOfProjects(2);
        clusterSummary.setMaxPeptideRatio(0.7f);

        assertEquals(ClusterQuality.LOW, clusterSummaryQualityDecider.decideQuality(clusterSummary));

        clusterSummary.setTotalNumberOfSpectra(10);

        assertEquals(ClusterQuality.HIGH, clusterSummaryQualityDecider.decideQuality(clusterSummary));
    }

    @Test
    public void testMaxRatio() throws Exception {
        ClusterSummary clusterSummary = new ClusterSummary();
        clusterSummary.setTotalNumberOfSpectra(10);
        clusterSummary.setNumberOfProjects(2);
        clusterSummary.setMaxPeptideRatio(0.69f);

        assertEquals(ClusterQuality.LOW, clusterSummaryQualityDecider.decideQuality(clusterSummary));

        clusterSummary.setMaxPeptideRatio(0.8f);

        assertEquals(ClusterQuality.HIGH, clusterSummaryQualityDecider.decideQuality(clusterSummary));
    }

    @Test
    public void testNumberOfProjects() throws Exception {
        ClusterSummary clusterSummary = new ClusterSummary();
        clusterSummary.setTotalNumberOfSpectra(10);
        clusterSummary.setNumberOfProjects(1);
        clusterSummary.setMaxPeptideRatio(0.7f);

        assertEquals(ClusterQuality.MEDIUM, clusterSummaryQualityDecider.decideQuality(clusterSummary));

        clusterSummary.setNumberOfProjects(2);

        assertEquals(ClusterQuality.HIGH, clusterSummaryQualityDecider.decideQuality(clusterSummary));
    }
}