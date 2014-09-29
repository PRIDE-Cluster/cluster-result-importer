package uk.ac.ebi.pride.tools.cluster.repo;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import uk.ac.ebi.pride.archive.repo.assay.AssayRepository;
import uk.ac.ebi.pride.archive.repo.file.ProjectFileRepository;
import uk.ac.ebi.pride.archive.repo.project.ProjectRepository;
import uk.ac.ebi.pride.tools.cluster.utils.PropertyUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Building an PRIDE Archive access repository
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ArchiveRepositoryBuilder {

    private RepositoryFactorySupport factory;

    public ArchiveRepositoryBuilder(String propertyFile) {
        Properties properties;
        try {
            properties = loadArchiveDataSourceProperties(propertyFile);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load database properties for PRIDE Archive", e);
        }

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("application", properties);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        factory = new JpaRepositoryFactory(entityManager);
    }

    /**
     * Create data source
     * @return
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    private Properties loadArchiveDataSourceProperties(String propertyFile) throws IOException, URISyntaxException {
        Properties properties = PropertyUtils.loadProperties(propertyFile);
        Properties archiveRepositoryProperties = new Properties();

        archiveRepositoryProperties.put("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");

        // driver class name
        String driverClassName = properties.getProperty("pride.archive.jdbc.driver");
        archiveRepositoryProperties.put("javax.persistence.jdbc.driver", driverClassName);

        // connection URL
        String jdbcUrl = properties.getProperty("pride.archive.jdbc.url");
        archiveRepositoryProperties.put("javax.persistence.jdbc.url", jdbcUrl);

        // connection user
        String user = properties.getProperty("pride.archive.jdbc.user");
        archiveRepositoryProperties.put("javax.persistence.jdbc.user", user);

        // connection password
        String password = properties.getProperty("pride.archive.jdbc.password");
        archiveRepositoryProperties.put("javax.persistence.jdbc.password", password);

        return archiveRepositoryProperties;
    }

    public ProjectRepository getProjectRepository() {
        return factory.getRepository(ProjectRepository.class);
    }

    public AssayRepository getAssayRepository() {
        return factory.getRepository(AssayRepository.class);
    }

    public ProjectFileRepository getProjectFileRepository() {
        return factory.getRepository(ProjectFileRepository.class);
    }
}
