package org.evosuite.ga.metaheuristics.mosa;

import java.util.List;
import java.util.Map;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

/**
 * Interface for ranking algorithms
 * @author Annibale, Fitusm
 *
 * @param <T>
 */
public interface Ranking<T extends Chromosome> {

	/**
	 * Returns a <code>list of chromosome</code> containing the solutions of a given rank.
	 * 
	 * @param rank
	 *            The rank
	 * @return Object representing the <code>SolutionSet</code>.
	 */
	public List<T> getSubfront(int rank); // getSubFront

	/**
	 * Returns the total number of subFronts founds.
	 */
	public int getNumberOfSubfronts(); // getNumberOfSubfronts

	/**
	 * Returns the covered goals and the corresponding covering test cases
	 */
	public Map<FitnessFunction<T>, T> getNewCoveredGoals();
}