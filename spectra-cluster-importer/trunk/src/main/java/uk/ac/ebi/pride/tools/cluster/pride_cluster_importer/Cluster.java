package uk.ac.ebi.pride.tools.cluster.pride_cluster_importer;

/**
 * Represents a row in the cluster table of the PRIDE Q test database.
 * 
 * @author jg
 * 
 */
public class Cluster {
	private Integer id;
	private Integer clusteringMethodId;
	private Double avPrecursorMz;
	private String consensusMz;
	private String consensusIntensity;
	private Boolean isReliable;
	private String sequence;
	private String mods;
	private Double reliabilityScore;
	private String manualAssessment;
	private Double threshold;
	private String uid;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getClusteringMethodId() {
		return clusteringMethodId;
	}
	public void setClusteringMethodId(Integer clusteringMethodId) {
		this.clusteringMethodId = clusteringMethodId;
	}
	public Double getAvPrecursorMz() {
		return avPrecursorMz;
	}
	public void setAvPrecursorMz(Double avPrecursorMz) {
		this.avPrecursorMz = avPrecursorMz;
	}
	public String getConsensusMz() {
		return consensusMz;
	}
	public void setConsensusMz(String consensusMz) {
		this.consensusMz = consensusMz;
	}
	public String getConsensusIntensity() {
		return consensusIntensity;
	}
	public void setConsensusIntensity(String consensusIntensity) {
		this.consensusIntensity = consensusIntensity;
	}
	public Boolean getIsReliable() {
		return isReliable;
	}
	public void setIsReliable(Boolean isReliable) {
		this.isReliable = isReliable;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public String getMods() {
		return mods;
	}
	public void setMods(String mods) {
		this.mods = mods;
	}
	public Double getReliabilityScore() {
		return reliabilityScore;
	}
	public void setReliabilityScore(Double reliabilityScore) {
		this.reliabilityScore = reliabilityScore;
	}
	public String getManualAssessment() {
		return manualAssessment;
	}
	public void setManualAssessment(String manualAssessment) {
		this.manualAssessment = manualAssessment;
	}
	public Double getThreshold() {
		return threshold;
	}
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((avPrecursorMz == null) ? 0 : avPrecursorMz.hashCode());
		result = prime
				* result
				+ ((clusteringMethodId == null) ? 0 : clusteringMethodId
						.hashCode());
		result = prime
				* result
				+ ((consensusIntensity == null) ? 0 : consensusIntensity
						.hashCode());
		result = prime * result
				+ ((consensusMz == null) ? 0 : consensusMz.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((isReliable == null) ? 0 : isReliable.hashCode());
		result = prime
				* result
				+ ((manualAssessment == null) ? 0 : manualAssessment.hashCode());
		result = prime * result + ((mods == null) ? 0 : mods.hashCode());
		result = prime
				* result
				+ ((reliabilityScore == null) ? 0 : reliabilityScore.hashCode());
		result = prime * result
				+ ((sequence == null) ? 0 : sequence.hashCode());
		result = prime * result
				+ ((threshold == null) ? 0 : threshold.hashCode());
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cluster other = (Cluster) obj;
		if (avPrecursorMz == null) {
			if (other.avPrecursorMz != null)
				return false;
		} else if (!avPrecursorMz.equals(other.avPrecursorMz))
			return false;
		if (clusteringMethodId == null) {
			if (other.clusteringMethodId != null)
				return false;
		} else if (!clusteringMethodId.equals(other.clusteringMethodId))
			return false;
		if (consensusIntensity == null) {
			if (other.consensusIntensity != null)
				return false;
		} else if (!consensusIntensity.equals(other.consensusIntensity))
			return false;
		if (consensusMz == null) {
			if (other.consensusMz != null)
				return false;
		} else if (!consensusMz.equals(other.consensusMz))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isReliable == null) {
			if (other.isReliable != null)
				return false;
		} else if (!isReliable.equals(other.isReliable))
			return false;
		if (manualAssessment == null) {
			if (other.manualAssessment != null)
				return false;
		} else if (!manualAssessment.equals(other.manualAssessment))
			return false;
		if (mods == null) {
			if (other.mods != null)
				return false;
		} else if (!mods.equals(other.mods))
			return false;
		if (reliabilityScore == null) {
			if (other.reliabilityScore != null)
				return false;
		} else if (!reliabilityScore.equals(other.reliabilityScore))
			return false;
		if (sequence == null) {
			if (other.sequence != null)
				return false;
		} else if (!sequence.equals(other.sequence))
			return false;
		if (threshold == null) {
			if (other.threshold != null)
				return false;
		} else if (!threshold.equals(other.threshold))
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}
}
