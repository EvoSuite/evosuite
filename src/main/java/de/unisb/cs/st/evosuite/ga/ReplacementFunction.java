/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.ga;

import java.io.Serializable;

import de.unisb.cs.st.evosuite.Properties;

/**
 * Decides when offspring replaces its parents for the next generation
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class ReplacementFunction implements Serializable {

	private static final long serialVersionUID = 8507488475265387482L;

	protected boolean maximize = false;
	
	public ReplacementFunction(boolean maximize) {
		this.maximize = maximize;
	}

	protected boolean isBetter(Chromosome chromosome1, Chromosome chromosome2) {
		if (maximize) {
			return chromosome1.compareTo(chromosome2) > 0;
		} else {
			return chromosome1.compareTo(chromosome2) < 0;
		}

	}

	protected boolean isBetterOrEqual(Chromosome chromosome1, Chromosome chromosome2) {
		if (maximize) {
			return chromosome1.compareTo(chromosome2) >= 0;
		} else {
			return chromosome1.compareTo(chromosome2) <= 0;
		}

	}

	protected Chromosome getBest(Chromosome chromosome1, Chromosome chromosome2) {
		if (isBetter(chromosome1, chromosome2))
			return chromosome1;
		else
			return chromosome2;
	}

	/**
	 * Decide whether to keep the offspring or the parents
	 * 
	 * @param parent1
	 * @param parent2
	 * @param offspring1
	 * @param offspring2
	 * @return
	 */
	public  boolean keepOffspring(Chromosome parent1, Chromosome parent2,
	        Chromosome offspring1, Chromosome offspring2){		
		return compareBestOffspringToBestParent(parent1,parent2,offspring1,offspring2) >= 0 ;
	}
	
	/**
	 * Check how the best offspring  compares with best parent
	 * @param parent1
	 * @param parent2
	 * @param offspring1
	 * @param offspring2
	 * @return
	 */
	protected int compareBestOffspringToBestParent(Chromosome parent1, Chromosome parent2,
	        Chromosome offspring1, Chromosome offspring2){
		
		int o1o2 = offspring1.compareTo(offspring2);
		int p1p2 = parent1.compareTo(parent2);
		
		Chromosome bestOffspring = null;
		if(o1o2 > 0) {
			bestOffspring = offspring1;
		} else {
			bestOffspring = offspring2;
		}
		
		Chromosome bestParent = null;
		if(p1p2 > 0) {
			bestParent = parent1;
		} else {
			bestParent = parent2;
		}
				
		return bestOffspring.compareTo(bestParent);
	}

	/**
	 * Decide which of two offspring to keep
	 * 
	 * @param parent
	 * @param offspring
	 * @return
	 * 
	 * @deprecated should not be used, as it does not handle Properties.CHECK_PARENTS_LENGTH 
	 */
	public  boolean keepOffspring(Chromosome parent, Chromosome offspring){
		return isBetterOrEqual(offspring, parent);
	}
}
