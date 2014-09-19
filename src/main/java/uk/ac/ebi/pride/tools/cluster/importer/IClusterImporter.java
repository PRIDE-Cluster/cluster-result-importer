package uk.ac.ebi.pride.tools.cluster.importer;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;

/**
 * Import cluster into a data store
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface IClusterImporter {

    void save(ICluster cluster);
}
