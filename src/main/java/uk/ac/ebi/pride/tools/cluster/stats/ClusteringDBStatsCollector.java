package uk.ac.ebi.pride.tools.cluster.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.ClusterReader;
import uk.ac.ebi.pride.spectracluster.repo.model.*;
import uk.ac.ebi.pride.spectracluster.repo.utils.paging.Page;
import uk.ac.ebi.pride.tools.cluster.dao.TransactionAwareClusterRepoStatisticsWriter;
import uk.ac.ebi.pride.tools.cluster.repo.ClusterRepositoryBuilder;
import uk.ac.ebi.pride.tools.cluster.utils.CollectionUtils;

import java.util.*;

/**
 * ClusteringDBStatsCollector collects statistics from the clustering database
 * <p/>
 * - Number of clusters
 * - Number of high quality clusters
 * - Number of medium quality clusters
 * - Number of low quality clusters
 * - Number of species
 * - Number of projects
 * - Number of assays
 * - Number of distinct peptides
 * - Number of identified spectra
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringDBStatsCollector {

    private static final Logger logger = LoggerFactory.getLogger(ClusteringDBStatsCollector.class);

    public static final String NUMBER_OF_CLUSTERS = "Number of clusters";
    public static final String NUMBER_OF_HIGH_QUALITY_CLUSTERS = "Number of high quality clusters";
    public static final String NUMBER_OF_MEDIUM_QUALITY_CLUSTERS = "Number of medium quality clusters";
    public static final String NUMBER_OF_LOW_QUALITY_CLUSTERS = "Number of low quality clusters";
    public static final String NUMBER_OF_SPECIES = "Number of species";
    public static final String NUMBER_OF_PROJECTS = "Number of projects";
    public static final String NUMBER_OF_ASSAYS = "Number of assays";
    public static final String NUMBER_OF_DISTINCT_PEPTIDES = "Number of distinct peptides";
    public static final String NUMBER_OF_IDENTIFIED_SPECTRA = "Number of identified spectra";
    public static final String NUMBER_OF_CLUSTERS_PER_SPECIES = "Number of clusters per species - ";
    public static final String NUMBER_OF_UNIQUE_PEPTIDES_PER_SPECIES = "Number of unique peptides per species - ";
    public static final String OVERLAPPING_UNIQUE_PEPTIDES_ON_SPEICES = "Overlapping unique peptides on species - ";


    public static void main(String[] args) {
        // create data source
        ClusterRepositoryBuilder clusterRepositoryBuilder = new ClusterRepositoryBuilder("prop/cluster-database-oracle.properties");

        // create cluster reader
        DataSourceTransactionManager transactionManager = clusterRepositoryBuilder.getTransactionManager();
        ClusterReader clusterReader = new ClusterReader(transactionManager.getDataSource());

        // create cluster repo statistics writer
        TransactionAwareClusterRepoStatisticsWriter clusterRepoStatisticsWriter = new TransactionAwareClusterRepoStatisticsWriter(transactionManager);

        // statistics
        List<ClusterRepoStatistics> statistics = new ArrayList<ClusterRepoStatistics>();

        // number of clusters
        long numberOfClusters = clusterReader.getNumberOfClusters();
        statistics.add(new ClusterRepoStatistics(NUMBER_OF_CLUSTERS, numberOfClusters));
        logger.info(NUMBER_OF_CLUSTERS + " " + numberOfClusters + "");

        // number of high quality clusters
        long numberOfHighQualityClusters = clusterReader.getNumberOfClustersByQuality(ClusterQuality.HIGH);
        statistics.add(new ClusterRepoStatistics(NUMBER_OF_HIGH_QUALITY_CLUSTERS, numberOfHighQualityClusters));
        logger.info(NUMBER_OF_HIGH_QUALITY_CLUSTERS + " " + numberOfHighQualityClusters + "");

        // number of medium quality clusters
        long numberOfMediumQualityClusters = clusterReader.getNumberOfClustersByQuality(ClusterQuality.MEDIUM);
        statistics.add(new ClusterRepoStatistics(NUMBER_OF_MEDIUM_QUALITY_CLUSTERS, numberOfMediumQualityClusters));
        logger.info(NUMBER_OF_MEDIUM_QUALITY_CLUSTERS + " " + numberOfMediumQualityClusters + "");

        // number of low quality clusters
        long numberOfLowQualityClusters = clusterReader.getNumberOfClustersByQuality(ClusterQuality.LOW);
        statistics.add(new ClusterRepoStatistics(NUMBER_OF_LOW_QUALITY_CLUSTERS, numberOfLowQualityClusters));
        logger.info(NUMBER_OF_LOW_QUALITY_CLUSTERS + " " + numberOfLowQualityClusters + "");

        // number of species
        long numberOfClusteredSpecies = clusterReader.getNumberOfClusteredSpecies();
        statistics.add(new ClusterRepoStatistics(NUMBER_OF_SPECIES, numberOfClusteredSpecies));
        logger.info(NUMBER_OF_SPECIES + " " + numberOfClusteredSpecies + "");

        // number of projects
        long numberOfClusteredProjects = clusterReader.getNumberOfClusteredProjects();
        statistics.add(new ClusterRepoStatistics(NUMBER_OF_PROJECTS, numberOfClusteredProjects));
        logger.info(NUMBER_OF_PROJECTS + " " + numberOfClusteredProjects + "");

        // number of assays
        long numberOfClusteredAssays = clusterReader.getNumberOfClusteredAssays();
        statistics.add(new ClusterRepoStatistics(NUMBER_OF_ASSAYS, numberOfClusteredAssays));
        logger.info(NUMBER_OF_ASSAYS + " " + numberOfClusteredAssays + "");

        // number of distinct peptides
        long numberOfClusteredDistinctPeptides = clusterReader.getNumberOfClusteredDistinctPeptides();
        statistics.add(new ClusterRepoStatistics(NUMBER_OF_DISTINCT_PEPTIDES, numberOfClusteredDistinctPeptides));
        logger.info(NUMBER_OF_DISTINCT_PEPTIDES + " " + numberOfClusteredDistinctPeptides + "");

        // number of identified spectra
        long numberOfClusteredIdentifiedSpectra = clusterReader.getNumberOfClusteredIdentifiedSpectra();
        statistics.add(new ClusterRepoStatistics(NUMBER_OF_IDENTIFIED_SPECTRA, numberOfClusteredIdentifiedSpectra));
        logger.info(NUMBER_OF_IDENTIFIED_SPECTRA + " " + numberOfClusteredIdentifiedSpectra + "");

        // write statistics
        clusterRepoStatisticsWriter.saveStatistics(statistics);

        // clear previous stats
        statistics.clear();

        Map<String, Long> clusterCounts = new HashMap<String, Long>();
        Map<String, Set<String>> uniquePeptides = new HashMap<String, Set<String>>();

        int pageSize = 50;
        int numberOfPages = (int) Math.ceil(numberOfHighQualityClusters * 1.0 / pageSize * 1.0);

        for (int pageNumber = 1; pageNumber <= numberOfPages; pageNumber++) {
            Page<Long> highQualityClusterIds = clusterReader.getAllClusterIdsByQuality(pageNumber, pageSize, ClusterQuality.HIGH);
            for (Long clusterId : highQualityClusterIds.getPageItems()) {
                ClusterDetail cluster = clusterReader.findCluster(clusterId);

                // count number of clusters per species
                List<AssayDetail> assayDetails = cluster.getAssayDetails();
                for (AssayDetail assayDetail : assayDetails) {
                    Set<String> speciesEntries = assayDetail.getSpeciesEntries();
                    for (String speciesEntry : speciesEntries) {
                        Long count = clusterCounts.get(speciesEntry);
                        if (count == null) {
                            clusterCounts.put(speciesEntry, 1l);
                        } else {
                            count += 1;
                            clusterCounts.put(speciesEntry, count);
                        }
                    }
                }

                List<ClusteredPSMDetail> psmDetails = cluster.getClusteredPSMDetails();
                for (ClusteredPSMDetail psmDetail : psmDetails) {
                    if (psmDetail.getPsmRatio() == 1.1) {
                        String sequence = psmDetail.getSequence();
                        Long assayId = psmDetail.getPsmDetail().getAssayId();
                        AssayDetail assayDetail = cluster.getAssayDetail(assayId);
                        for (String assaySpecies : assayDetail.getSpeciesEntries()) {
                            Set<String> peptides = uniquePeptides.get(assaySpecies);
                            if (peptides == null) {
                                peptides = new HashSet<String>();
                                uniquePeptides.put(assaySpecies, peptides);
                            }
                            peptides.add(sequence);
                        }
                    }
                }
            }
        }


        // store cluster counts
        for (Map.Entry<String, Long> clusterCount : clusterCounts.entrySet()) {
            statistics.add(new ClusterRepoStatistics(NUMBER_OF_CLUSTERS_PER_SPECIES + clusterCount.getKey(), clusterCount.getValue()));
        }
        clusterRepoStatisticsWriter.saveStatistics(statistics);
        statistics.clear();

        // store unique peptide counts
        for (Map.Entry<String, Set<String>> uniquePeptideCount : uniquePeptides.entrySet()) {
            statistics.add(new ClusterRepoStatistics(NUMBER_OF_UNIQUE_PEPTIDES_PER_SPECIES + uniquePeptideCount.getKey(), (long)uniquePeptideCount.getValue().size()));
        }
        clusterRepoStatisticsWriter.saveStatistics(statistics);
        statistics.clear();

        // store unique peptide intersection counts
        for (Map.Entry<String, Set<String>> uniquePeptideCount : uniquePeptides.entrySet()) {
            String species = uniquePeptideCount.getKey();
            Set<String> peptides = uniquePeptideCount.getValue();

            for (String speciesToCompare : uniquePeptides.keySet()) {
                if (!species.equals(speciesToCompare)) {
                    int intersection = CollectionUtils.countIntersection(peptides, uniquePeptides.get(speciesToCompare));
                    statistics.add(new ClusterRepoStatistics(OVERLAPPING_UNIQUE_PEPTIDES_ON_SPEICES + species + "-" + speciesToCompare, (long)intersection));
                }
            }
        }
        clusterRepoStatisticsWriter.saveStatistics(statistics);
        statistics.clear();
    }
}
