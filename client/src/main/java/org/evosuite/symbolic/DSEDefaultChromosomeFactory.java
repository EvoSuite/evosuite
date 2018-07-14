package org.evosuite.symbolic;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;

public abstract class DSEDefaultChromosomeFactory<T extends Chromosome> implements ChromosomeFactory<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1737491796764323810L;

	public abstract int numberOfDefaultChromosomes();
	
	public abstract T getDefaultChromosome(int i);
}
