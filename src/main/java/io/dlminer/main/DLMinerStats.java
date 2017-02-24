package io.dlminer.main;

import io.dlminer.print.Out;

public class DLMinerStats {
	
	// ====================== attributes ======================
	
	// performance
	/**
	 * ontology parsing time
	 */
	private Double ontologyParsingTime;

	/**
	 * ontology reasoning time, 
	 * i.e. the time of executing the reasoner on the ontology
	 */
	private Double ontologyReasoningTime;

	/**
	 * concept building time
	 */
	private Double conceptBuildingTime;
	
	/**
	 * role building time
	 */
	private Double roleBuildingTime;

	/**
	 * hypotheses building time
	 */
	private Double hypothesesBuildingTime;

	/**
	 * hypotheses cleaning time (removing redundant hypotheses)
	 */
	private Double hypothesesCleaningTime;
	
	
	/**
	 * time to compute basic measures
	 */
	private Double basicMeasuresTime;
	
	
	/**
	 * time to compute main measures
	 */
	private Double contraMeasuresTime;
	
	
	/**
	 * time to compute complex measures: fitness, braveness, novelty, dissimilarity
	 */
	private Double complexMeasuresTime;
	
	
	/**
	 * time to precompute entailments for complex measures
	 */
	private Double complexMeasuresPrecompTime;
	
	
	/**
	 * time to compute strength ordering
	 */
	private Double strengthTime;
	
	
	/**
	 * time to compute dominance ranking
	 */
	private Double dominanceTime;
	
	
	/**
	 * time to check consistency
	 */
	private Double consistencyTime;
	

	// counts
	/**
	 * total number of built concepts
	 */
	private Integer conceptsNumber;

	/**
	 * total number of built roles
	 */
	private Integer rolesNumber;

	/**
	 * total number of returned hypotheses
	 */
	private Integer hypothesesNumber;

	// measures
	/**
	 * maximal hypothesis support
	 */
	private Double maxSupport;

	/**
	 * average hypothesis support
	 */
	private Double averageSupport;

	/**
	 * minimal hypothesis support
	 */
	private Double minSupport;

	/**
	 * maximal hypothesis assumption
	 */
	private Double maxAssumption;

	/**
	 * average hypothesis assumption
	 */
	private Double averageAssumption;

	/**
	 * minimal hypothesis assumption
	 */
	private Double minAssumption;

	/**
	 * minimal hypothesis novelty
	 */
	private Double minNovelty;

	/**
	 * maximal hypothesis novelty
	 */
	private Double maxNovelty;

	/**
	 * average hypothesis novelty
	 */
	private Double averageNovelty;

	/**
	 * minimal hypothesis length
	 */
	private Double minLength;


	/**
	 * maximal hypothesis length
	 */
	private Double maxLength;

	/**
	 * average hypothesis length
	 */
	private Double averageLength;
	
	
	
	
	// ====================== 	getters and setters ====================== 	
	
	/**
	 * @return the ontologyParsingTime
	 */
	public Double getOntologyParsingTime() {
		return ontologyParsingTime;
	}


	/**
	 * @param ontologyParsingTime the ontologyParsingTime to set
	 */
	public void setOntologyParsingTime(Double ontologyParsingTime) {
		this.ontologyParsingTime = ontologyParsingTime;
	}


	/**
	 * @return the ontologyReasoningTime
	 */
	public Double getOntologyReasoningTime() {
		return ontologyReasoningTime;
	}


	/**
	 * @param ontologyReasoningTime the ontologyReasoningTime to set
	 */
	public void setOntologyReasoningTime(Double ontologyReasoningTime) {
		this.ontologyReasoningTime = ontologyReasoningTime;
	}


	/**
	 * @return the conceptBuildingTime
	 */
	public Double getConceptBuildingTime() {
		return conceptBuildingTime;
	}


	/**
	 * @param conceptBuildingTime the conceptBuildingTime to set
	 */
	public void setConceptBuildingTime(Double conceptBuildingTime) {
		this.conceptBuildingTime = conceptBuildingTime;
	}


	/**
	 * @return the roleBuildingTime
	 */
	public Double getRoleBuildingTime() {
		return roleBuildingTime;
	}


	/**
	 * @param roleBuildingTime the roleBuildingTime to set
	 */
	public void setRoleBuildingTime(Double roleBuildingTime) {
		this.roleBuildingTime = roleBuildingTime;
	}


	/**
	 * @return the hypothesesBuildingTime
	 */
	public Double getHypothesesBuildingTime() {
		return hypothesesBuildingTime;
	}


	/**
	 * @param hypothesesBuildingTime the hypothesesBuildingTime to set
	 */
	public void setHypothesesBuildingTime(Double hypothesesBuildingTime) {
		this.hypothesesBuildingTime = hypothesesBuildingTime;
	}


	/**
	 * @return the hypothesesCleaningTime
	 */
	public Double getHypothesesCleaningTime() {
		return hypothesesCleaningTime;
	}


	/**
	 * @param hypothesesCleaningTime the hypothesesCleaningTime to set
	 */
	public void setHypothesesCleaningTime(Double hypothesesCleaningTime) {
		this.hypothesesCleaningTime = hypothesesCleaningTime;
	}


	/**
	 * @return the basicMeasuresTime
	 */
	public Double getBasicMeasuresTime() {
		return basicMeasuresTime;
	}


	/**
	 * @param basicMeasuresTime the basicMeasuresTime to set
	 */
	public void setBasicMeasuresTime(Double basicMeasuresTime) {
		this.basicMeasuresTime = basicMeasuresTime;
	}


	/**
	 * @return the contraMeasuresTime
	 */
	public Double getContraMeasuresTime() {
		return contraMeasuresTime;
	}


	/**
	 * @param contraMeasuresTime the contraMeasuresTime to set
	 */
	public void setContraMeasuresTime(Double contraMeasuresTime) {
		this.contraMeasuresTime = contraMeasuresTime;
	}


	/**
	 * @return the complexMeasuresTime
	 */
	public Double getComplexMeasuresTime() {
		return complexMeasuresTime;
	}


	/**
	 * @param complexMeasuresTime the complexMeasuresTime to set
	 */
	public void setComplexMeasuresTime(Double complexMeasuresTime) {
		this.complexMeasuresTime = complexMeasuresTime;
	}


	/**
	 * @return the complexMeasuresPrecompTime
	 */
	public Double getComplexMeasuresPrecompTime() {
		return complexMeasuresPrecompTime;
	}


	/**
	 * @param complexMeasuresPrecompTime the complexMeasuresPrecompTime to set
	 */
	public void setComplexMeasuresPrecompTime(Double complexMeasuresPrecompTime) {
		this.complexMeasuresPrecompTime = complexMeasuresPrecompTime;
	}


	/**
	 * @return the strengthTime
	 */
	public Double getStrengthTime() {
		return strengthTime;
	}


	/**
	 * @param strengthTime the strengthTime to set
	 */
	public void setStrengthTime(Double strengthTime) {
		this.strengthTime = strengthTime;
	}


	/**
	 * @return the dominanceTime
	 */
	public Double getDominanceTime() {
		return dominanceTime;
	}


	/**
	 * @param dominanceTime the dominanceTime to set
	 */
	public void setDominanceTime(Double dominanceTime) {
		this.dominanceTime = dominanceTime;
	}


	/**
	 * @return the consistencyTime
	 */
	public Double getConsistencyTime() {
		return consistencyTime;
	}


	/**
	 * @param consistencyTime the consistencyTime to set
	 */
	public void setConsistencyTime(Double consistencyTime) {
		this.consistencyTime = consistencyTime;
	}


	/**
	 * @return the conceptsNumber
	 */
	public Integer getConceptsNumber() {
		return conceptsNumber;
	}


	/**
	 * @param conceptsNumber the conceptsNumber to set
	 */
	public void setConceptsNumber(Integer conceptsNumber) {
		this.conceptsNumber = conceptsNumber;
	}


	


	/**
	 * @param rolesNumber the rolesNumber to set
	 */
	public void setRolesNumber(Integer rolesNumber) {
		this.rolesNumber = rolesNumber;
	}

	
	/**
	 * @return the rolesNumber
	 */
	public Integer getRolesNumber() {
		return rolesNumber;
	}
	
	

	/**
	 * @return the hypothesesNumber
	 */
	public Integer getHypothesesNumber() {
		return hypothesesNumber;
	}


	/**
	 * @param hypothesesNumber the hypothesesNumber to set
	 */
	public void setHypothesesNumber(Integer hypothesesNumber) {
		this.hypothesesNumber = hypothesesNumber;
	}


	/**
	 * @return the maxFitness
	 */
	public Double getMaxSupport() {
		return maxSupport;
	}


	/**
	 * @param maxSupport the maxSupport to set
	 */
	public void setMaxSupport(Double maxSupport) {
		this.maxSupport = maxSupport;
	}


	/**
	 * @return the averageSupport
	 */
	public Double getAverageSupport() {
		return averageSupport;
	}


	/**
	 * @param averageSupport the averageSupport to set
	 */
	public void setAverageSupport(Double averageSupport) {
		this.averageSupport = averageSupport;
	}


	/**
	 * @return the minFitness
	 */
	public Double getMinSupport() {
		return minSupport;
	}


	/**
	 * @param minSupport the minSupport to set
	 */
	public void setMinSupport(Double minSupport) {
		this.minSupport = minSupport;
	}


	/**
	 * @return the maxAssumption
	 */
	public Double getMaxAssumption() {
		return maxAssumption;
	}


	/**
	 * @param maxAssumption the maxAssumption to set
	 */
	public void setMaxAssumption(Double maxAssumption) {
		this.maxAssumption = maxAssumption;
	}


	/**
	 * @return the averageAssumption
	 */
	public Double getAverageAssumption() {
		return averageAssumption;
	}


	/**
	 * @param averageAssumption the averageAssumption to set
	 */
	public void setAverageAssumption(Double averageAssumption) {
		this.averageAssumption = averageAssumption;
	}


	/**
	 * @return the minAssumption
	 */
	public Double getMinAssumption() {
		return minAssumption;
	}


	/**
	 * @param minAssumption the minAssumption to set
	 */
	public void setMinAssumption(Double minAssumption) {
		this.minAssumption = minAssumption;
	}


	/**
	 *
	 * @return minNovelty
     */
	public Double getMinNovelty() {
		return minNovelty;
	}

	/**
	 *
	 * @param minNovelty minNovelty
     */
	public void setMinNovelty(Double minNovelty) {
		this.minNovelty = minNovelty;
	}

	/**
	 *
	 * @return maxNovelty
     */
	public Double getMaxNovelty() {
		return maxNovelty;
	}

	/**
	 *
	 * @param maxNovelty maxNovelty
     */
	public void setMaxNovelty(Double maxNovelty) {
		this.maxNovelty = maxNovelty;
	}

	/**
	 *
	 * @return averageNovelty
     */
	public Double getAverageNovelty() {
		return averageNovelty;
	}

	/**
	 *
	 * @param averageNovelty averageNovelty
     */
	public void setAverageNovelty(Double averageNovelty) {
		this.averageNovelty = averageNovelty;
	}

	/**
	 *
	 * @return minLength
     */
	public Double getMinLength() {
		return minLength;
	}

	/**
	 *
	 * @param minLength minLength
     */
	public void setMinLength(Double minLength) {
		this.minLength = minLength;
	}

	/**
	 *
	 * @return maxLength
     */
	public Double getMaxLength() {
		return maxLength;
	}

	/**
	 *
	 * @param maxLength maxLength
     */
	public void setMaxLength(Double maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 *
	 * @return averageLength
     */
	public Double getAverageLength() {
		return averageLength;
	}

	/**
	 *
	 * @param averageLength averageLength
     */
	public void setAverageLength(Double averageLength) {
		this.averageLength = averageLength;
	}
	
	
		
	@Override
	public String toString() {
		return getStatistics();
	}
	
	
	
	public String getStatistics() {		
        String stats = "\nPerformance:"
                + "\n ontologyParsingTime = " + Out.fn(ontologyParsingTime) + " secs"
                + "\n ontologyReasoningTime = " + Out.fn(ontologyReasoningTime) + " secs"
                + "\n conceptBuildingTime = " + Out.fn(conceptBuildingTime) + " secs"
                + "\n hypothesesBuildingTime = " + Out.fn(hypothesesBuildingTime) + " secs"
                + "\n hypothesesCleaningTime = " + Out.fn(hypothesesCleaningTime) + " secs"
                + "\nHypotheses:"
                + "\n hypothesesNumber = " + hypothesesNumber
                + "\n conceptsNumber = " + conceptsNumber
                + "\n rolesNumber = " + rolesNumber
                + "\n maxSupport = " + maxSupport
                + "\n averageSupport = " + (averageSupport == null ? "null" : Out.fn(averageSupport))
                + "\n minSupport = " + minSupport
                + "\n maxAssumption = " + maxAssumption
                + "\n averageAssumption = " + (averageAssumption == null ? "null" : Out.fn(averageAssumption))
                + "\n minAssumption = " + minAssumption
                + "\n maxNovelty = " + maxNovelty
                + "\n averageNovelty = " + (averageNovelty == null ? "null" : Out.fn(averageNovelty))
                + "\n minNovelty = " + minNovelty
                + "\n maxLength = " + maxLength
                + "\n averageLength = " + (averageLength == null ? "null" : Out.fn(averageLength))
                + "\n minLength = " + minLength;
        return stats;
    }
	

}
