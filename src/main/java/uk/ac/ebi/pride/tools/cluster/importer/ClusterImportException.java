package uk.ac.ebi.pride.tools.cluster.importer;

/**
 * Runtime exception will be thrown if there is a error while loading a cluster
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterImportException extends RuntimeException{

    public ClusterImportException() {
    }

    public ClusterImportException(String message) {
        super(message);
    }

    public ClusterImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClusterImportException(Throwable cause) {
        super(cause);
    }

    public ClusterImportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
