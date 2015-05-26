package org.evosuite.ga;

public interface Archive<T extends Chromosome> {

	public T createMergedSolution(T solution);
	
}
