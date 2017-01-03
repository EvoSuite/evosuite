package org.evosuite.ga.metaheuristics.mosa.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.evosuite.ga.Chromosome;

/**
 * Sort a Collection of Chromosomes by CrowdingDistance
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew
 */
public class OnlyCrowdingComparator implements Comparator<Chromosome>, Serializable {
	
	private static final long serialVersionUID = -6576898111709166470L;

	@Override
	public int compare(Chromosome c1, Chromosome c2) {
		if (c1.getDistance() > c2.getDistance())
			return -1;
		else if (c1.getDistance() < c2.getDistance())
			return +1;
		else 
			return 0;
	}
}
