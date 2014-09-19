package uk.ac.ebi.pride.tools.cluster.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PropertyUtils {
    /**
     * Load data source properties from property file
     *
     * @return
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    public static Properties loadProperties(String resourceUrl) throws URISyntaxException, IOException {
        ClassLoader classLoader = PropertyUtils.class.getClassLoader();
        URL resource = classLoader.getResource(resourceUrl);
        File file = new File(resource.toURI());

        Properties properties = new Properties();
        properties.load(new FileInputStream(file));

        return properties;
    }

}
