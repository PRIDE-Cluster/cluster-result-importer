package uk.ac.ebi.pride.tools.cluster.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Utility methods for accessing properties
 *
 * @author Rui Wang
 * @version $Id$
 */
public final class PropertyUtils {
    /**
     * Load data source properties from property file
     *
     * @return
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    public static Properties loadProperties(String resourceUrl) throws URISyntaxException, IOException {
        ClassLoader classLoader = PropertyUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourceUrl);

        Properties properties = new Properties();
        properties.load(inputStream);

        return properties;
    }

}
