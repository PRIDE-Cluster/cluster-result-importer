package uk.ac.ebi.pride.tools.cluster.exporter;

import uk.ac.ebi.pride.spectracluster.util.CountedString;
import uk.ac.ebi.pride.tools.cluster.model.*;

import java.io.IOException;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringFileAppender {

    /**
     * @param out     !null open appendale
     * @param cluster !null cluster
     */
    public static void appendCluster(final Appendable out, final ClusterSummary cluster) throws IOException {
        out.append("=Cluster=\n");
        out.append("av_precursor_mz=").append(String.format("%10.3f", cluster.getAveragePrecursorMz()).trim());
        out.append("\n");
        out.append("av_precursor_intens=1.0");   // Useless, since intensities are completely random
        out.append("\n");


        String s = mostCommonPeptides(cluster);
        out.append("sequence=[" + s + "]");
        out.append("\n");

        out.append("consensus_mz=").append(cluster.getConsensusSpectrumMz());
        out.append("\n");
        out.append("consensus_intens=").append(cluster.getConsensusSpectrumIntensity());
        out.append("\n");


        List<ClusteredSpectrumSummary> clusteredSpectrumSummaries = cluster.getClusteredSpectrumSummaries();
        for (ClusteredSpectrumSummary clusteredSpectrumSummary : clusteredSpectrumSummaries) {
            StringBuilder sb = new StringBuilder();
            sb.append("SPEC\t");
            String spectrumRef = clusteredSpectrumSummary.getReferenceId();
            sb.append(spectrumRef);

            sb.append("\t");
            SpectrumSummary spectrumSummary = clusteredSpectrumSummary.getSpectrumSummary();
            sb.append(spectrumSummary.isIdentified());

            // append peptide sequence as a extra column
            String peptideSequence = allPeptides(cluster, clusteredSpectrumSummary);
            sb.append("\t");
            if (peptideSequence != null) {
                sb.append(peptideSequence);
            }

            // append precursor m/z as an extra column
            float precursorMz = spectrumSummary.getPrecursorMz();
            sb.append("\t");
            sb.append(String.format("%10.3f", precursorMz).trim());

            // append precursor charge as an extra column
            int precursorCharge = spectrumSummary.getPrecursorCharge();
            sb.append("\t");
            sb.append(precursorCharge);

            // append species
            String species = allSpecies(cluster, clusteredSpectrumSummary);
            sb.append("\t");
            if (species != null) {
                sb.append(species);
            }

            // append modifications
            String modifications = allModifications(cluster, clusteredSpectrumSummary);
            sb.append("\t");
            if (modifications != null) {
                sb.append(modifications);
            }

            // append similarity score between consensus spectrum and spectrum
            float similarity = clusteredSpectrumSummary.getSimilarityScore();
            sb.append("\t");
            sb.append(String.format("%10.3f", similarity).trim());

            sb.append("\n");

            String csq = sb.toString();
            out.append(csq);
        }
    }

    private static String mostCommonPeptides(ClusterSummary cluster) {
        List<ClusteredPSMSummary> clusteredPSMSummaries = cluster.getClusteredPSMSummaries();

        List<String> sequences = new ArrayList<String>();
        Map<Long, Set<String>> spectrumIds = new HashMap<Long, Set<String>>();
        for (ClusteredPSMSummary clusteredPSMSummary : clusteredPSMSummaries) {
            Long spectrumId = clusteredPSMSummary.getSpectrumId();
            String sequence = clusteredPSMSummary.getSequence();

            Set<String> seqs = spectrumIds.get(spectrumId);
            if (seqs != null && seqs.contains(sequence))
                continue;

            if (seqs == null) {
                seqs = new HashSet<String>();
                spectrumIds.put(spectrumId, seqs);
            }

            seqs.add(sequence);
            sequences.add(sequence);
        }

        CountedString[] countedStrings = CountedString.getCountedStrings(sequences);
        StringBuilder sb = new StringBuilder();
        for (CountedString countedString : countedStrings) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(countedString);

        }

        return sb.toString();
    }

    private static String allPeptides(ClusterSummary cluster, ClusteredSpectrumSummary clusteredSpectrumSummary) {
        String sequences = "";

        for (ClusteredPSMSummary clusteredPSMSummary : cluster.getClusteredPSMSummaries()) {
            PSMSummary psmSummary = clusteredPSMSummary.getPsmSummary();
            if (psmSummary.getSpectrumId().equals(clusteredSpectrumSummary.getSpectrumId())) {
                if (sequences.length() > 0)
                    sequences += ",";
                sequences += psmSummary.getSequence();
            }
        }

        return sequences;
    }


    private static String allSpecies(ClusterSummary cluster, ClusteredSpectrumSummary clusteredSpectrumSummary) {
        for (AssaySummary assaySummary : cluster.getAssaySummaries()) {
            if (assaySummary.getId().equals(clusteredSpectrumSummary.getSpectrumSummary().getAssayId())) {
                return assaySummary.getTaxonomyId();
            }
        }
        return null;
    }

    private static String allModifications(ClusterSummary cluster, ClusteredSpectrumSummary clusteredSpectrumSummary) {
        String modifications = "";

        for (ClusteredPSMSummary clusteredPSMSummary : cluster.getClusteredPSMSummaries()) {
            PSMSummary psmSummary = clusteredPSMSummary.getPsmSummary();
            if (psmSummary.getSpectrumId().equals(clusteredSpectrumSummary.getSpectrumId())) {
                if (modifications.length() > 0)
                    modifications += ";";
                String mods = psmSummary.getModifications();
                if (mods != null && mods.length() > 0) {
                    modifications += mods;
                }
            }
        }

        return modifications;
    }
}
