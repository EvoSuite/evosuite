package org.evosuite.ga.metaheuristics.mosa;

import java.util.Comparator;

import org.evosuite.ga.Chromosome;

/**
 * This class implements a <code>Comparator</code> (a method for comparing <code>Chromosomes</code> 
 * objects) based on the dominance test, as in NSGA-II.
 * 
 * @author Annibale Panichella
 */
public class RankAndCrowdingDistanceComparator<T extends Chromosome> implements Comparator<Object> {

	/**
	 * Constructor
	 * 
	 * @param comparator
	 */
	public RankAndCrowdingDistanceComparator() {
		
	}

	/**
	 * Compares two solutions.
	 * 
	 * @param object1
	 *            Object representing the first <code>Solution</code>.
	 * @param object2
	 *            Object representing the second <code>Solution</code>.
	 * @return -1, or 0, or 1 according to the non-dominated ranks
	 */
	public int compare(Object object1, Object object2) {
		if (object1 == null)
			return 1;
		else if (object2 == null)
			return -1;

		T solution1 = (T) object1;
		T solution2 = (T) object2;

		if (solution1.getRank()<solution2.getRank())
			return -1;
		else if (solution1.getRank()>solution2.getRank())
			return +1;
		else if (solution1.getDistance()>solution2.getDistance())
			return -1;
		else if (solution2.getDistance()>solution1.getDistance())
			return +1;
		else
			return 0;
	} // compare
} // DominanceComparator

