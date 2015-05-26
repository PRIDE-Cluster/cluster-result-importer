package uk.ac.ebi.pride.tools.cluster.utils;

import java.util.Collection;

/**
 * A simple collection utility methods collection
 *
 * @author Rui Wang
 * @version $Id$
 */
public class CollectionUtils {

    private CollectionUtils() {};

    /**
     * Count the intersection between two collections
     *
     * @param col1 collection one
     * @param col2 collection two
     * @return the number of intersection
     */
    public static int countIntersection(Collection col1, Collection col2) {
        Collection a, b;

        if (col1.size() <= col2.size()) {
            a = col1;
            b = col2;
        } else {
            a = col2;
            b = col1;
        }

        int count = 0;
        for (Object e : a) {
            if (b.contains(e)) {
                count++;
            }
        }

        return count;
    }
}
