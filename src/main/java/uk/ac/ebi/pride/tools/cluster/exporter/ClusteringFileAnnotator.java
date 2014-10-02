package uk.ac.ebi.pride.tools.cluster.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.tools.cluster.model.*;
import uk.ac.ebi.pride.tools.cluster.repo.ClusterReader;
import uk.ac.ebi.pride.tools.cluster.repo.ClusterRepositoryBuilder;
import uk.ac.ebi.pride.tools.cluster.utils.SummaryFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringFileAnnotator {
    public static final Logger logger = LoggerFactory.getLogger(ClusteringFileAnnotator.class);

    private final ClusterReader clusterReader;

    public ClusteringFileAnnotator(ClusterReader clusterReader) {
        this.clusterReader = clusterReader;
    }

    public void annotate(File inputFile, File outputFile) throws Exception {

        logger.info("Annotating clustering file: {}", inputFile.getAbsolutePath());
        logger.info("Output clustering file: {}", outputFile.getAbsolutePath());

        // load clusters
        ClusteringFileReader clusteringFileReader = new ClusteringFileReader(inputFile);
        List<ICluster> clusters = clusteringFileReader.readAllClusters();

        // output
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

        for (ICluster cluster : clusters) {
            ClusterSummary clusterSummary = annotateCluster(cluster);
            ClusteringFileAppender.appendCluster(writer, clusterSummary);
        }

        logger.info("Annotated {} clusters.", clusters.size());

        writer.flush();
        writer.close();
    }

    private ClusterSummary annotateCluster(ICluster cluster) throws IOException {
        ClusterSummary clusterSummary = SummaryFactory.summariseCluster(cluster);

        List<String> spectrumRefs = new ArrayList<String>();
        List<ClusteredSpectrumSummary> clusteredSpectrumSummaries = clusterSummary.getClusteredSpectrumSummaries();
        for (ClusteredSpectrumSummary clusteredSpectrumSummary : clusteredSpectrumSummaries) {
            spectrumRefs.add(clusteredSpectrumSummary.getReferenceId());
        }

        List<Long> spectrumIds = new ArrayList<Long>();
        List<SpectrumSummary> spectra = clusterReader.findSpectra(spectrumRefs);
        if (spectra.size() != clusteredSpectrumSummaries.size()) {
            logger.error("The number of spectra found doesn't match the number of clustered spectra: {} missing", spectra.size() - clusteredSpectrumSummaries.size());
        }

        if (spectra.size() > 0) {
            for (SpectrumSummary spectrumSummary : spectra) {
                Long spectrumId = spectrumSummary.getId();
                spectrumIds.add(spectrumId);
                String referenceId = spectrumSummary.getReferenceId();
                for (ClusteredSpectrumSummary clusteredSpectrumSummary : clusteredSpectrumSummaries) {
                    if (clusteredSpectrumSummary.getReferenceId().equals(referenceId)) {
                        clusteredSpectrumSummary.setSpectrumId(spectrumId);
                        clusteredSpectrumSummary.setSpectrumSummary(spectrumSummary);
                    }
                }
            }

            List<Long> assayIds = new ArrayList<Long>();
            List<PSMSummary> psms = clusterReader.findPSMBySpectrumId(spectrumIds);
            for (PSMSummary psm : psms) {
                Long psmId = psm.getId();
                Long assayId = psm.getAssayId();
                assayIds.add(assayId);
                ClusteredPSMSummary clusteredPSMSummary = new ClusteredPSMSummary();
                clusteredPSMSummary.setSpectrumId(psm.getSpectrumId());
                clusteredPSMSummary.setSequence(psm.getSequence());
                clusteredPSMSummary.setPsmId(psmId);
                clusteredPSMSummary.setPsmSummary(psm);
                clusterSummary.addClusteredPSMSummary(clusteredPSMSummary);
            }

            List<AssaySummary> assays = clusterReader.findAssays(assayIds);
            clusterSummary.addAssaySummaries(assays);
        }

        return clusterSummary;
    }

    public static void main(String[] args) throws Exception {
        File inputClusteringFile = new File(args[0]);
        File outputFolder = new File(args[1]);
        File outputClusteringFile = new File(outputFolder, inputClusteringFile.getName());

        ClusterRepositoryBuilder clusterRepositoryBuilder = new ClusterRepositoryBuilder("prop/cluster-database-oracle.properties");

        ClusterReader reader = new ClusterReader(clusterRepositoryBuilder.getTransactionManager());

        ClusteringFileAnnotator clusteringFileAnnotator = new ClusteringFileAnnotator(reader);

        clusteringFileAnnotator.annotate(inputClusteringFile, outputClusteringFile);
    }
}
