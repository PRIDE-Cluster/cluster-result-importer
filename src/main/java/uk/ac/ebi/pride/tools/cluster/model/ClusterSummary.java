package uk.ac.ebi.pride.tools.cluster.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterSummary {
    private Long id;
    private float averagePrecursorMz;
    private float averagePrecursorCharge;
    private byte[] consensusSpectrumMz;
    private byte[] consensusSpectrumIntensity;
    private int numberOfSpectra;
    private float maxPeptideRatio;
    private final List<ClusteredSpectrumSummary> clusteredSpectrumSummaries = new ArrayList<ClusteredSpectrumSummary>();
    private final List<ClusteredPSMSummary> clusteredPSMSummaries = new ArrayList<ClusteredPSMSummary>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getAveragePrecursorMz() {
        return averagePrecursorMz;
    }

    public void setAveragePrecursorMz(float averagePrecursorMz) {
        this.averagePrecursorMz = averagePrecursorMz;
    }

    public float getAveragePrecursorCharge() {
        return averagePrecursorCharge;
    }

    public void setAveragePrecursorCharge(float averagePrecursorCharge) {
        this.averagePrecursorCharge = averagePrecursorCharge;
    }

    public byte[] getConsensusSpectrumMz() {
        return consensusSpectrumMz;
    }

    public void setConsensusSpectrumMz(byte[] consensusSpectrumMz) {
        this.consensusSpectrumMz = consensusSpectrumMz;
    }

    public byte[] getConsensusSpectrumIntensity() {
        return consensusSpectrumIntensity;
    }

    public void setConsensusSpectrumIntensity(byte[] consensusSpectrumIntensity) {
        this.consensusSpectrumIntensity = consensusSpectrumIntensity;
    }

    public int getNumberOfSpectra() {
        return numberOfSpectra;
    }

    public void setNumberOfSpectra(int numberOfSpectra) {
        this.numberOfSpectra = numberOfSpectra;
    }

    public float getMaxPeptideRatio() {
        return maxPeptideRatio;
    }

    public void setMaxPeptideRatio(float maxPeptideRatio) {
        this.maxPeptideRatio = maxPeptideRatio;
    }

    public List<ClusteredSpectrumSummary> getClusteredSpectrumSummaries() {
        return clusteredSpectrumSummaries;
    }

    public void addClusteredSpectrumSummary(ClusteredSpectrumSummary clusteredSpectrumSummary) {
        clusteredSpectrumSummaries.add(clusteredSpectrumSummary);
    }

    public List<ClusteredPSMSummary> getClusteredPSMSummaries() {
        return clusteredPSMSummaries;
    }

    public void addClusteredPSMSummary(ClusteredPSMSummary clusteredPSMSummary) {
        clusteredPSMSummaries.add(clusteredPSMSummary);
    }
}
