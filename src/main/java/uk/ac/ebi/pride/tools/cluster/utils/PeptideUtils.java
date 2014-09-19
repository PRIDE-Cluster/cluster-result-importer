package uk.ac.ebi.pride.tools.cluster.utils;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PeptideUtils {

    public static final String NO_PEPTIDE_REGEX = "[^ABCDEFGHIJKLMNPQRSTUVWXYZ]";

    public static String cleanPeptideSequence(String peptideSequence) {
        String res = null;

        if(peptideSequence!= null){
            res = peptideSequence.toUpperCase();
            res = res.replaceAll(NO_PEPTIDE_REGEX,"");
        }

        return res;
    }
}
