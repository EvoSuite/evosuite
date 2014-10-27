/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga;


/**
 * <p>MinimizeSizeSecondaryObjective class.</p>
 *
 * @author Gordon Fraser
 */
public class IBranchSecondaryObjective extends SecondaryObjective {

	private FitnessFunction<?> ff;
	private static final long serialVersionUID = 7211557650429998223L;

	public IBranchSecondaryObjective(FitnessFunction<?> fitness) {
		ff = fitness;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.ga.SecondaryObjective#compareChromosomes(de.unisb
	 * .cs.st.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareChromosomes(Chromosome chromosome1, Chromosome chromosome2) {
		logger.debug("Comparing sizes: " + chromosome1.getFitness(ff) + " vs "
				+ chromosome2.getFitness(ff));
		if (chromosome1.getFitness(ff) < chromosome2.getFitness(ff)) {
			return -1;
		}
		if (chromosome1.getFitness(ff) > chromosome2.getFitness(ff)) {
			return 1;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.ga.SecondaryObjective#compareGenerations(de.unisb
	 * .cs.st.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome,
	 * org.evosuite.ga.Chromosome,
	 * org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareGenerations(Chromosome parent1, Chromosome parent2,
	        Chromosome child1, Chromosome child2) {
		logger.debug("Comparing sizes: " + parent1.size() + ", " + parent1.size()
		        + " vs " + child1.size() + ", " + child2.size());
		
		double minParents = Math.min(parent1.getFitness(ff), parent2.getFitness(ff));
		double minChildren = Math.min(child1.getFitness(ff), child2.getFitness(ff));
		if (minParents<minChildren) {
			return -1;
		}
		if (minParents>minChildren) {
			return 1;
		}
		return 0;  
	}

}
