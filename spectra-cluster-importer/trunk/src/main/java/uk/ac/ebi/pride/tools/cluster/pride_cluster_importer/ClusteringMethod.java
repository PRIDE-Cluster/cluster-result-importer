package uk.ac.ebi.pride.tools.cluster.pride_cluster_importer;

/**
 * Represents a row in the clustering_method
 * table of the PRIDE Q test database.
 * @author jg
 *
 */
public class ClusteringMethod {
	private Integer id;
	private String name;
	private String similarityMethod;
	private Double threshold;
	private Double fdr;
	private String description;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSimilarityMethod() {
		return similarityMethod;
	}
	public void setSimilarityMethod(String similarityMethod) {
		this.similarityMethod = similarityMethod;
	}
	public Double getThreshold() {
		return threshold;
	}
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}
	public Double getFdr() {
		return fdr;
	}
	public void setFdr(Double fdr) {
		this.fdr = fdr;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((fdr == null) ? 0 : fdr.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime
				* result
				+ ((similarityMethod == null) ? 0 : similarityMethod.hashCode());
		result = prime * result
				+ ((threshold == null) ? 0 : threshold.hashCode());
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
		ClusteringMethod other = (ClusteringMethod) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (fdr == null) {
			if (other.fdr != null)
				return false;
		} else if (!fdr.equals(other.fdr))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (similarityMethod == null) {
			if (other.similarityMethod != null)
				return false;
		} else if (!similarityMethod.equals(other.similarityMethod))
			return false;
		if (threshold == null) {
			if (other.threshold != null)
				return false;
		} else if (!threshold.equals(other.threshold))
			return false;
		return true;
	}
}
