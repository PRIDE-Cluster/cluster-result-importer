package uk.ac.ebi.pride.tools.cluster.annotator;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import uk.ac.ebi.pride.tools.cluster.utils.PropertyUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterRepositoryBuilder {

    private final DataSourceTransactionManager transactionManager;

    public ClusterRepositoryBuilder(String propertyFile) {
        try {
            // create data source
            DataSource dataSource = createDataSource(propertyFile);

            // create transaction manager
            transactionManager = new DataSourceTransactionManager(dataSource);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load database properties for PRIDE cluster", e);
        }
    }

    /**
     * Create data source
     * @return
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    private static DataSource createDataSource(String propertyFile) throws IOException, URISyntaxException {
        Properties properties = PropertyUtils.loadProperties(propertyFile);
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

    public DataSource getDataSource() {
        return transactionManager.getDataSource();
    }

    public DataSourceTransactionManager getTransactionManager() {
        return transactionManager;
    }
}
