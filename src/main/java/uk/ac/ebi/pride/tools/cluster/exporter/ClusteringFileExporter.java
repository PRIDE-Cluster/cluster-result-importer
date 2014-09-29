package uk.ac.ebi.pride.tools.cluster.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.tools.cluster.model.ClusterSummary;
import uk.ac.ebi.pride.tools.cluster.repo.ClusterReader;
import uk.ac.ebi.pride.tools.cluster.repo.ClusterRepositoryBuilder;

import java.io.*;

/**
 * Exporter for extract clusters from database into clustering file
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringFileExporter {
    public static final Logger logger = LoggerFactory.getLogger(ClusteringFileExporter.class);

    public static void main(String[] args) throws IOException {
        String outputFilePath = args[0];
        long startClusterId = Long.parseLong(args[1]);
        long stopClusterId = Long.parseLong(args[2]);

        File clusteringFile = new File(outputFilePath);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(clusteringFile)));

        ClusterRepositoryBuilder clusterRepositoryBuilder = new ClusterRepositoryBuilder("prop/cluster-database-oracle.properties");
        ClusterReader clusterReader = new ClusterReader(clusterRepositoryBuilder.getTransactionManager());

        for (long i = startClusterId; i <= stopClusterId; i++) {
            ClusterSummary cluster = clusterReader.findCluster(startClusterId);
            if (cluster != null) {
                logger.debug("Export cluster: " + cluster.getId());
                ClusteringFileAppender.appendCluster(writer, cluster);
            }
        }

        writer.flush();
        writer.close();
    }

}
