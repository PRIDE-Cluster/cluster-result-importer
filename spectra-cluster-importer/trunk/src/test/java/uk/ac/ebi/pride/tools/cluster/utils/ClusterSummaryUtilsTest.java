package uk.ac.ebi.pride.tools.cluster.utils;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.tools.cluster.model.ClusterSummary;
import uk.ac.ebi.pride.tools.cluster.model.ClusteredPSMSummary;
import uk.ac.ebi.pride.tools.cluster.model.ClusteredSpectrumSummary;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterSummaryUtilsTest {

    private ClusterSummary clusterSummary;

    @Before
    public void setUp() throws Exception {

        clusterSummary = new ClusterSummary();

        ClusteredSpectrumSummary clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 1");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 2");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 3");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 4");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 5");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 6");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 7");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 8");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 9");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("spectrum - 10");
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        ClusteredPSMSummary clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("AAAAAAAAA");
        clusteredPSMSummary.setSpectrumId(1111l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("AAAAAAAAA");
        clusteredPSMSummary.setSpectrumId(1112l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("AAAAAAAAA");
        clusteredPSMSummary.setSpectrumId(1113l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("AAAAAAAAA");
        clusteredPSMSummary.setSpectrumId(1114l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("BBBBBBBBB");
        clusteredPSMSummary.setSpectrumId(2221l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("BBBBBBBBB");
        clusteredPSMSummary.setSpectrumId(2222l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("BBBBBBBBB");
        clusteredPSMSummary.setSpectrumId(2223l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("CCCCCCCCC");
        clusteredPSMSummary.setSpectrumId(3331l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("CCCCCCCCC");
        clusteredPSMSummary.setSpectrumId(3332l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setSequence("DDDDDDDDD");
        clusteredPSMSummary.setSpectrumId(4441l);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        ClusterSummaryUtils.updateClusteredPSMStatistics(clusterSummary);
    }

    @Test
    public void testPSMRankings() throws Exception {
        List<ClusteredPSMSummary> clusteredPSMSummaries = clusterSummary.getClusteredPSMSummaries("AAAAAAAAA");
        assertEquals(4, clusteredPSMSummaries.size());
        for (ClusteredPSMSummary clusteredPSMSummary : clusteredPSMSummaries) {
            assertEquals(1, clusteredPSMSummary.getRank());
        }

        clusteredPSMSummaries = clusterSummary.getClusteredPSMSummaries("BBBBBBBBB");
        assertEquals(3, clusteredPSMSummaries.size());
        for (ClusteredPSMSummary clusteredPSMSummary : clusteredPSMSummaries) {
            assertEquals(2, clusteredPSMSummary.getRank());
        }

        clusteredPSMSummaries = clusterSummary.getClusteredPSMSummaries("CCCCCCCCC");
        assertEquals(2, clusteredPSMSummaries.size());
        for (ClusteredPSMSummary clusteredPSMSummary : clusteredPSMSummaries) {
            assertEquals(3, clusteredPSMSummary.getRank());
        }

        clusteredPSMSummaries = clusterSummary.getClusteredPSMSummaries("DDDDDDDDD");
        assertEquals(1, clusteredPSMSummaries.size());
        for (ClusteredPSMSummary clusteredPSMSummary : clusteredPSMSummaries) {
            assertEquals(4, clusteredPSMSummary.getRank());
        }
    }
}
