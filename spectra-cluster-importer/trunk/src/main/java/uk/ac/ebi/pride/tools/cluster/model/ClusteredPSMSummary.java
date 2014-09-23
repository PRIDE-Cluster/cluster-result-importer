package uk.ac.ebi.pride.tools.cluster.model;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteredPSMSummary {
    private Long clusterId;
    private Long psmId;
    private float psmRatio;
    private int rank;

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getPsmId() {
        return psmId;
    }

    public void setPsmId(Long psmId) {
        this.psmId = psmId;
    }

    public float getPsmRatio() {
        return psmRatio;
    }

    public void setPsmRatio(float psmRatio) {
        this.psmRatio = psmRatio;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
