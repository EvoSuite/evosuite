package org.evosuite.ga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of chromosomes
 * 
 * @author Nasser Albunian
 */
public class ChromosomeCollection<T extends Chromosome> implements Serializable{

	private List<T> chromosomes = new ArrayList<T>();
	
	public ChromosomeCollection(List<T> chromosomes_){
		chromosomes.clear();
		chromosomes = chromosomes_;
	}
	
	public List<T> getCollection(){
		return chromosomes;
	}
}
