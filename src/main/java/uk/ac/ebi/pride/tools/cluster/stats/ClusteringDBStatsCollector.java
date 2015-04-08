package uk.ac.ebi.pride.tools.cluster.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.ClusterReader;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterQuality;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterRepoStatistics;
import uk.ac.ebi.pride.tools.cluster.dao.TransactionAwareClusterRepoStatisticsWriter;
import uk.ac.ebi.pride.tools.cluster.repo.ClusterRepositoryBuilder;

import java.util.ArrayList;
import java.util.List;

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
    }
}
