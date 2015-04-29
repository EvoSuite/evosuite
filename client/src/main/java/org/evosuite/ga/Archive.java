package org.evosuite.ga;

public interface Archive<T extends Chromosome> {

	public T updateSolution(T solution);
	
}
