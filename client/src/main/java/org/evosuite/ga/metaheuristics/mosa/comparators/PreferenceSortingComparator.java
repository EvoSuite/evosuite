package org.evosuite.ga.metaheuristics.mosa.comparators;

import java.util.Comparator;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

/**
 * This class implements a <code>Comparator</code> (a method for comparing <code>Chromosomes</code> objects) 
 * based on the dominance test and considering the specified test goals only.
 * 
 * @author Annibale Panichella
 */
public class PreferenceSortingComparator<T extends Chromosome> implements Comparator<Object> {

	private FitnessFunction<T> objective;
	private TestSizeComparator<T> comparator = new TestSizeComparator<T>();

	/**
	 *  Constructor
	 * @param pNumberOfObjectives
	 * @param goals set of test goals to consider when computing the dominance relationship 
	 * @param applySecondaryCriterion 
	 */
	public PreferenceSortingComparator(FitnessFunction<T> goals) {
		this.objective = goals;
	}

	/**
	 * Compares two test cases focusing only on the goals in {@link MOSADominanceComparator#objectives}".
	 * 
	 * @param object1
	 *            Object representing the first test cases.
	 * @param object2
	 *            Object representing the second test cases.
	 * @return -1, or 0, or 1 if object1 dominates object2, both are non-dominated, or solution1 is dominated by solution2, respectively.
	 */
	@SuppressWarnings("unchecked")
	public int compare(Object object1, Object object2) {
		if (object1 == null)
			return 1;
		else if (object2 == null)
			return -1;

		T solution1 = (T) object1;
		T solution2 = (T) object2;

		double value1, value2;
		value1 = solution1.getFitness(objective);
		value2 = solution2.getFitness(objective);
		if (value1 < value2)
			return -1;
		else if (value1 > value2)
			return +1;
		else {
			return comparator.compare(solution1, solution2);
		}

	} // compare

} // PreferenceCriterion

