package io.dlminer.learn;

import java.io.File;
import java.util.Set;



public class HypothesisEntry {	
	
	private int fitness;
	
	private int braveness;
	
	private int interest;	
	
	private int length;
	
	private Set<String> entities;
	
	private File file;	

	
	
	/**
	 * @param fitness
	 * @param braveness
	 * @param interest
	 * @param length
	 * @param entities
	 * @param file
	 */
	public HypothesisEntry(int fitness, int braveness, int interest,
			int length, Set<String> entities, File file) {		
		this.fitness = fitness;
		this.braveness = braveness;
		this.interest = interest;
		this.length = length;
		this.entities = entities;
		this.file = file;
	}


	
	/**
	 * @return the fitness
	 */
	public int getFitness() {
		return fitness;
	}


	/**
	 * @param fitness the fitness to set
	 */
	public void setFitness(int fitness) {
		this.fitness = fitness;
	}


	/**
	 * @return the braveness
	 */
	public int getBraveness() {
		return braveness;
	}


	/**
	 * @param braveness the braveness to set
	 */
	public void setBraveness(int braveness) {
		this.braveness = braveness;
	}


	/**
	 * @return the interest
	 */
	public int getInterest() {
		return interest;
	}


	/**
	 * @param interest the interest to set
	 */
	public void setInterest(int interest) {
		this.interest = interest;
	}


	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}


	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}


	/**
	 * @return the entities
	 */
	public Set<String> getEntities() {
		return entities;
	}


	/**
	 * @param entities the entities to set
	 */
	public void setEntities(Set<String> entities) {
		this.entities = entities;
	}



	/**
	 * @return the temporary file
	 */
	public File getFile() {
		return file;
	}



	/**
	 * @param file the temporary file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}



	
	@Override
	public String toString() {
		return "[fit=" + fitness +
				", bra=" + braveness + 
				", int=" + interest +
				", len=" + length + 
				", entities number=" + entities.size() +
				", file length=" + file.length() + "]";
	}
	
	
		

}
