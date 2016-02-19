/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga;

/**
 * <p>MinimizeSizeSecondaryObjective class.</p>
 *
 * @author Gordon Fraser
 */
public class MinimizeSizeSecondaryObjective<T extends Chromosome> extends SecondaryObjective<T> {

	private static final long serialVersionUID = 7211557650429998223L;

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
		logger.debug("Comparing sizes: " + chromosome1.size() + " vs "
		        + chromosome2.size());
		return chromosome1.size() - chromosome2.size();
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
		return Math.min(parent1.size(), parent2.size())
		        - Math.min(child1.size(), child2.size());
	}

}
