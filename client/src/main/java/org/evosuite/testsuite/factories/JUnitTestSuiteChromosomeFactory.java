/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

package org.evosuite.testsuite.factories;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;


/**
 * <p>JUnitTestSuiteChromosomeFactory class.</p>
 *
 * @author fraser
 */
public class JUnitTestSuiteChromosomeFactory implements
        ChromosomeFactory<TestSuiteChromosome> {

    private static final long serialVersionUID = 1L;

    private final ChromosomeFactory<TestChromosome> defaultFactory;

    /**
     * <p>Constructor for JUnitTestSuiteChromosomeFactory.</p>
     *
     * @param defaultFactory a {@link org.evosuite.ga.ChromosomeFactory} object.
     */
    public JUnitTestSuiteChromosomeFactory(
            ChromosomeFactory<TestChromosome> defaultFactory) {
        this.defaultFactory = defaultFactory;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.ChromosomeFactory#getChromosome()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public TestSuiteChromosome getChromosome() {
		/*
		double P_delta = 0.1d;
		double P_clone = 0.1d;
		int MAX_CHANGES = 10;
		*/

        TestSuiteChromosome chromosome = new TestSuiteChromosome(
                new RandomLengthTestFactory());
        chromosome.clearTests();

        int numTests = Randomness.nextInt(Properties.MIN_INITIAL_TESTS,
                Properties.MAX_INITIAL_TESTS + 1);

        for (int i = 0; i < numTests; i++) {
            TestChromosome test = defaultFactory.getChromosome();
            chromosome.addTest(test);
            //chromosome.tests.add(test);
        }

        return chromosome;
    }

}
