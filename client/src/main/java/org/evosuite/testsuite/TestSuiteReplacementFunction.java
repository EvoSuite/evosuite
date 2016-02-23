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
package org.evosuite.testsuite;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ReplacementFunction;

/**
 * <p>
 * TestSuiteReplacementFunction class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestSuiteReplacementFunction extends ReplacementFunction {

	private static final long serialVersionUID = -8472469271120247395L;

	/**
	 * <p>
	 * Constructor for TestSuiteReplacementFunction.
	 * </p>
	 * 
	 * @param maximize
	 *            a boolean.
	 */
	public TestSuiteReplacementFunction(boolean maximize) {
		super(maximize);
	}

	/**
	 * <p>
	 * Constructor for TestSuiteReplacementFunction.
	 * </p>
	 */
	public TestSuiteReplacementFunction() {
		super(false);
	}

	/**
	 * <p>
	 * getLengthSum
	 * </p>
	 * 
	 * @param chromosome1
	 *            a {@link org.evosuite.testsuite.AbstractTestSuiteChromosome}
	 *            object.
	 * @param chromosome2
	 *            a {@link org.evosuite.testsuite.AbstractTestSuiteChromosome}
	 *            object.
	 * @return a int.
	 */
	public int getLengthSum(AbstractTestSuiteChromosome<?> chromosome1,
	        AbstractTestSuiteChromosome<?> chromosome2) {
		return chromosome1.totalLengthOfTestCases()
		        + chromosome2.totalLengthOfTestCases();
	}

	/** {@inheritDoc} */
	@Override
	public boolean keepOffspring(Chromosome parent1, Chromosome parent2,
	        Chromosome offspring1, Chromosome offspring2) {

		// -1 if offspring has lower fitness, +1 if parent has lower fitness
		int cmp = compareBestOffspringToBestParent(parent1, parent2, offspring1,
		                                           offspring2);

		if (Properties.CHECK_PARENTS_LENGTH) {

			int offspringLength = getLengthSum((AbstractTestSuiteChromosome<?>) offspring1,
			                                   (AbstractTestSuiteChromosome<?>) offspring2);
			int parentLength = getLengthSum((AbstractTestSuiteChromosome<?>) parent1,
			                                (AbstractTestSuiteChromosome<?>) parent2);

			//if equivalent, only accept if it does not increase the length
			if (cmp == 0 && offspringLength <= parentLength) {
				return true;
			} else {
				if (maximize) {
					return cmp > 0;
				} else {
					return cmp < 0;
				}
			}
		} else {
			//default check
			if (maximize) {
				return cmp >= 0;
			} else {
				return cmp <= 0;
			}
		}
	}
}
