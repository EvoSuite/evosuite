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
/**
 * 
 */
package org.evosuite.regression;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.CurrentChromosomeTracker;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.factories.TestSuiteChromosomeFactory;
import org.evosuite.utils.Randomness;


public class RegressionTestSuiteChromosomeFactory extends
		TestSuiteChromosomeFactory {

	private static final long serialVersionUID = -5460006842373221807L;

	/** Factory to manipulate and generate method sequences */

	/** {@inheritDoc} */
	@Override
	public TestSuiteChromosome getChromosome() {

		RegressionTestSuiteChromosome chromosome = new RegressionTestSuiteChromosome(
				testChromosomeFactory);

		chromosome.clearTests();
		CurrentChromosomeTracker<?> tracker = CurrentChromosomeTracker.getInstance();
		tracker.modification(chromosome);
		// ((AllMethodsChromosomeFactory)test_factory).clear();

		int numTests = Randomness.nextInt(Properties.MIN_INITIAL_TESTS,
				Properties.MAX_INITIAL_TESTS + 1);

		for (int i = 0; i < numTests; i++) {
			TestChromosome test = testChromosomeFactory.getChromosome();
			chromosome.addTest(test);
		}
		
		// logger.info("Covered methods: "+((AllMethodsChromosomeFactory)test_factory).covered.size());
		// logger.trace("Generated new test suite:"+chromosome);
		return chromosome;
	}

}
