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
package org.evosuite.testsuite;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.ExecutableChromosome;


/**
 * <p>MinimizeMaxLengthSecondaryObjective class.</p>
 *
 * @author Gordon Fraser
 */
public class MinimizeMaxLengthSecondaryObjective extends SecondaryObjective {

	private static final long serialVersionUID = 2270058273932360617L;

	private int getMaxLength(Chromosome chromosome) {
		int max = 0;
		for (ExecutableChromosome test : ((TestSuiteChromosome) chromosome).tests) {
			max = Math.max(max, test.size());
		}
		return max;
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
		return getMaxLength(chromosome1) - getMaxLength(chromosome2);
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
		return Math.min(getMaxLength(parent1), getMaxLength(parent2))
		        - Math.min(getMaxLength(child1), getMaxLength(child2));
	}

}
