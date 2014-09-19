package uk.ac.ebi.pride.tools.cluster.importer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.ClusteringFileReader;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterImporterMain {

    private static final Logger logger = LoggerFactory.getLogger(ClusterImporterMain.class);

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

            // create data source
            DataSource dataSource = createDataSource();

            // create transaction manager
            DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

            // create cluster importer
            ClusterDBImporter clusterDBImporter = new ClusterDBImporter(transactionManager);

            // create cluster source listener
            ClusterSourceListener clusterSourceListener = new ClusterSourceListener(clusterDBImporter);
            Collection<IClusterSourceListener> clusterSourceListeners = new ArrayList<IClusterSourceListener>();
            clusterSourceListeners.add(clusterSourceListener);

            // load clusters
            ClusteringFileReader clusteringFileReader = new ClusteringFileReader(file);
            clusteringFileReader.readClustersIteratively(clusterSourceListeners);

        } catch (Exception e) {
            logger.error("Error while running cluster importer", e);
            System.exit(1);
        }
    }

    /**
     * Create data source
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private static DataSource createDataSource() throws IOException, URISyntaxException {
        Properties properties = loadDataSourceProperties();
        BasicDataSource dataSource = new BasicDataSource();

        // driver class name
        String driverClassName = properties.getProperty("pride.cluster.jdbc.driver");
        dataSource.setDriverClassName(driverClassName);

        // connection URL
        String jdbcUrl = properties.getProperty("pride.cluster.jdbc.url");
        dataSource.setUrl(jdbcUrl);

        // connection user
        String user = properties.getProperty("pride.cluster.jdbc.user");
        dataSource.setUsername(user);

        // connection password
        String password = properties.getProperty("pride.cluster.jdbc.password");
        dataSource.setPassword(password);

        // validation query
        String validationQuery = properties.getProperty("pride.cluster.jdbc.validation.query");
        dataSource.setValidationQuery(validationQuery);

        return dataSource;
    }

    /**
     * Load data source properties from property file
     *
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    private static Properties loadDataSourceProperties() throws URISyntaxException, IOException {
        ClassLoader classLoader = ClusterImporterMain.class.getClassLoader();
        URL resource = classLoader.getResource("prop/cluster-database-oracle.properties");
        File file = new File(resource.toURI());

        Properties properties = new Properties();
        properties.load(new FileInputStream(file));

        return properties;
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PRIDE Cluster - Cluster importer", "Imports cluster results into the PRIDE Cluster database.\n", CliOptions.getOptions(), "\n\n", true);
    }

    private static class ClusterSourceListener implements IClusterSourceListener {

        private final IClusterImporter clusterImporter;

        private ClusterSourceListener(IClusterImporter clusterImporter) {
            this.clusterImporter = clusterImporter;
        }

        @Override
        public void onNewClusterRead(ICluster newCluster) {
            clusterImporter.save(newCluster);
        }
    }

}
