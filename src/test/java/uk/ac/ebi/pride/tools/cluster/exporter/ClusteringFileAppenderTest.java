package uk.ac.ebi.pride.tools.cluster.exporter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.IModification;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;
import uk.ac.ebi.pride.tools.cluster.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ClusteringFileAppenderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File clusteringFile;
    private ClusterSummary clusterSummary;

    @Before
    public void setUp() throws Exception {
        clusteringFile = temporaryFolder.newFile();

        clusterSummary = new ClusterSummary();
        clusterSummary.setId(2000l);
        clusterSummary.setAveragePrecursorCharge(2);
        clusterSummary.setAveragePrecursorMz(100.01f);
        clusterSummary.setConsensusSpectrumMz("10.0,20.0,30.0,40.01,50.02");
        clusterSummary.setConsensusSpectrumIntensity("101.0,201.0,301.0,401.01,501.02");

        ClusteredSpectrumSummary clusteredSpectrumSummary = new ClusteredSpectrumSummary();
        clusteredSpectrumSummary.setReferenceId("PXD00001;test.file;spectrum=1");
        clusteredSpectrumSummary.setSpectrumId(1000l);
        clusteredSpectrumSummary.setClusterId(2000l);
        clusteredSpectrumSummary.setSimilarityScore(0.6f);

        SpectrumSummary spectrumSummary = new SpectrumSummary();
        spectrumSummary.setId(1000l);
        spectrumSummary.setIdentified(true);
        spectrumSummary.setReferenceId("PXD00001;test.file;spectrum=1");
        spectrumSummary.setPrecursorCharge(2);
        spectrumSummary.setPrecursorMz(100.01f);
        spectrumSummary.setAssayId(3000l);

        clusteredSpectrumSummary.setSpectrumSummary(spectrumSummary);
        clusterSummary.addClusteredSpectrumSummary(clusteredSpectrumSummary);

        AssaySummary assay = new AssaySummary();
        assay.setId(3000l);
        assay.setTaxonomyId("9606,10056");
        clusterSummary.addAssaySummary(assay);

        ClusteredPSMSummary clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setPsmId(4000l);
        clusteredPSMSummary.setClusterId(2000l);
        clusteredPSMSummary.setSequence("ABCDEFG");
        clusteredPSMSummary.setRank(1);
        clusteredPSMSummary.setPsmRatio(0.5f);

        PSMSummary psmSummary = new PSMSummary();
        psmSummary.setSequence("ABCDEFG");
        psmSummary.setSpectrumId(1000l);
        psmSummary.setAssayId(3000l);
        psmSummary.setModifications("1-MOD:123,1-MOD:234");

        clusteredPSMSummary.setPsmSummary(psmSummary);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

        clusteredPSMSummary = new ClusteredPSMSummary();
        clusteredPSMSummary.setPsmId(5000l);
        clusteredPSMSummary.setClusterId(2000l);
        clusteredPSMSummary.setSequence("CDRTY");
        clusteredPSMSummary.setRank(1);
        clusteredPSMSummary.setPsmRatio(0.5f);

        psmSummary = new PSMSummary();
        psmSummary.setSequence("CDRTY");
        psmSummary.setSpectrumId(1000l);
        psmSummary.setAssayId(3000l);
        psmSummary.setModifications("2-MOD:123,3-MOD:234");

        clusteredPSMSummary.setPsmSummary(psmSummary);
        clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);

    }

    @Test
    public void testAppendCluster() throws Exception {
        PrintWriter writer = new PrintWriter(new FileWriter(clusteringFile));
        ClusteringFileAppender.appendCluster(writer, clusterSummary);
        writer.flush();
        writer.close();

        ClusteringFileReader clusteringFileReader = new ClusteringFileReader(clusteringFile);
        List<ICluster> clusters = clusteringFileReader.readAllClusters();

        assertEquals(1, clusters.size());

        ICluster cluster = clusters.get(0);
        assertEquals(100.01f, cluster.getAvPrecursorMz(), 0.0001);

        assertEquals(1f, cluster.getMaxRatio(), 0.0001);

        Map<String, Integer> psmSequenceCounts = cluster.getPsmSequenceCounts();
        assertEquals(2, psmSequenceCounts.size());

        assertEquals(1, psmSequenceCounts.get("CDRTY").intValue());
        assertEquals(1, psmSequenceCounts.get("ABCDEFG").intValue());

        assertEquals(1, cluster.getSpecCount());

        List<ISpectrumReference> spectrumReferences = cluster.getSpectrumReferences();
        ISpectrumReference spectrumReference = spectrumReferences.get(0);

        assertEquals(0.6f, spectrumReference.getSimilarityScore(), 0.00001);
        assertEquals(2, spectrumReference.getCharge());
        assertEquals(100.01f, spectrumReference.getPrecursorMz(), 0.0001);
        assertEquals("9606,10056", spectrumReference.getSpecies());
        List<IPeptideSpectrumMatch> psMs = spectrumReference.getPSMs();
        for (IPeptideSpectrumMatch psM : psMs) {
            String sequence = psM.getSequence();
            if (sequence.equals("CDRTY")) {
                List<IModification> modifications = psM.getModifications();
                assertEquals(2,modifications.size());
            }
        }


    }
}