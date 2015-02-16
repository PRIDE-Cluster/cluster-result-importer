package uk.ac.ebi.pride.tools.cluster.loader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.repo.dao.IClusterWriteDao;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterDetail;
import uk.ac.ebi.pride.tools.cluster.dao.TransactionAwareClusterWriter;
import uk.ac.ebi.pride.tools.cluster.repo.ClusterRepositoryBuilder;
import uk.ac.ebi.pride.tools.cluster.utils.SummaryFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringFileLoader {

    private static final Logger logger = LoggerFactory.getLogger(ClusteringFileLoader.class);

    private static final Pattern AMINO_ACID_PATTERN = Pattern.compile("[ABCDEFGHIJKLMNPQRSTUVWXYZ]+");

    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(CliOptions.getOptions(), args);

            // HELP
            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue())) {
                printUsage();
                return;
            }

            // IN
            File file;
            if (commandLine.hasOption(CliOptions.OPTIONS.FILE.getValue()))
                file = new File(commandLine.getOptionValue(CliOptions.OPTIONS.FILE.getValue()));
            else
                throw new Exception("Missing required parameter '" + CliOptions.OPTIONS.FILE.getValue() + "'");

            if (!file.exists())
                throw new Exception("Input .clustering file must be valid.");

            loadClusteringFile(file);


        } catch (Exception e) {
            logger.error("Error while running cluster importer", e);
            System.exit(1);
        }
    }

    private static void loadClusteringFile(File file) throws Exception {
        logger.info("Loading clustering file: {}", file.getAbsolutePath());

        // create data source
        ClusterRepositoryBuilder clusterRepositoryBuilder = new ClusterRepositoryBuilder("prop/cluster-database-oracle.properties");

        // create cluster importer
        IClusterWriteDao clusterDBImporter = new TransactionAwareClusterWriter(clusterRepositoryBuilder.getTransactionManager());

        // create cluster source listener
        ClusterSourceListener clusterSourceListener = new ClusterSourceListener(clusterDBImporter);
        Collection<IClusterSourceListener> clusterSourceListeners = new ArrayList<IClusterSourceListener>();
        clusterSourceListeners.add(clusterSourceListener);

        // load clusters
        ClusteringFileReader clusteringFileReader = new ClusteringFileReader(file);
        clusteringFileReader.readClustersIteratively(clusterSourceListeners);
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PRIDE Cluster - Cluster importer", "Imports cluster results into the PRIDE Cluster database.\n", CliOptions.getOptions(), "\n\n", true);
    }

    private static class ClusterSourceListener implements IClusterSourceListener {

        private final IClusterWriteDao clusterImporter;

        private ClusterSourceListener(IClusterWriteDao clusterImporter) {
            this.clusterImporter = clusterImporter;
        }

        @Override
        public void onNewClusterRead(ICluster newCluster) {
            try {
                if (newCluster.getSpecCount() > 1) {
                    String maxSequence = newCluster.getMaxSequence();
                    Matcher matcher = AMINO_ACID_PATTERN.matcher(maxSequence);

                    // remove clusters that identify illegal peptide sequences
                    if (matcher.matches()) {
                        ClusterDetail clusterSummary = SummaryFactory.summariseCluster(newCluster);
                        clusterImporter.saveCluster(clusterSummary);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to summaries cluster", e);
            }
        }
    }
}
