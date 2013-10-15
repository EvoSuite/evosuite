package org.evosuite.localsearch;

import org.evosuite.ga.Chromosome;

public interface LocalSearch<T extends Chromosome> {

	public abstract boolean doSearch(T individual, LocalSearchObjective<T> objective);
	
}
