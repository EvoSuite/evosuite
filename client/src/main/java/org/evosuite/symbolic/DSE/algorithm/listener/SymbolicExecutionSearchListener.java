package org.evosuite.symbolic.DSE.algorithm.listener;

import org.evosuite.ga.Chromosome;
import org.evosuite.symbolic.DSE.algorithm.DSEBaseAlgorithm;

public interface SymbolicExecutionSearchListener {
    /**
	 * Called when a new search is started
	 *
	 * @param algorithm a {@link org.evosuite.symbolic.DSE.algorithm.DSEBaseAlgorithm} object.
	 */
	public void searchStarted(DSEBaseAlgorithm algorithm);

	/**
	 * Called after each iteration of the search
	 *
	 * @param algorithm a {@link org.evosuite.symbolic.DSE.algorithm.DSEBaseAlgorithm} object.
	 */
	public void iteration(DSEBaseAlgorithm algorithm);

	/**
	 * Called after the last iteration
	 *
	 * @param algorithm a {@link org.evosuite.symbolic.DSE.algorithm.DSEBaseAlgorithm} object.
	 */
	public void searchFinished(DSEBaseAlgorithm algorithm);


    //NOTE: The methods below keeps using the GA Chromosome class as they have what we need for now

	/**
	 * Called after every single fitness evaluation
	 *
	 * @param individual a {@link org.evosuite.ga.Chromosome} object.
	 */
	public void fitnessEvaluation(Chromosome individual);

	/**
	 * Called before a chromosome is mutated
	 *
	 * @param individual a {@link org.evosuite.ga.Chromosome} object.
	 */
	public void modification(Chromosome individual);
}
