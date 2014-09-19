package uk.ac.ebi.pride.tools.cluster.annotator;

/**
 * Annotate for a particular project
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface IProjectAnnotator {

    void annotate(String projectAccession);
}
