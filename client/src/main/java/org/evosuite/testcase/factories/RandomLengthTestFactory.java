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
package org.evosuite.testcase.factories;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory that creates {@link TestChromosome}s of random length.
 *
 * @author Gordon Fraser
 */
public class RandomLengthTestFactory implements ChromosomeFactory<TestChromosome> {

    private static final long serialVersionUID = -5202578461625984100L;

    /**
     * Constant <code>logger</code>
     */
    protected static final Logger logger = LoggerFactory.getLogger(FixedLengthTestChromosomeFactory.class);

    /**
     * Creates a random test case (i.e., a test case consisting of random statements) with the given
     * {@code size} as an exclusive upper bound for the number of contained statements. In
     * particular, {@code size} is chosen at random from the interval [1, size). This means that
     * returned test cases contain at most {@code size - 1} statements. Usually, one can also expect
     * the test case to contain at least one statement, but it is still possible that an empty
     * test case is returned, although very unlikely.
     *
     * @param size the upper bound for the test case length
     * @return a random test case
     */
    private TestCase getRandomTestCase(int size) {
        boolean tracerEnabled = ExecutionTracer.isEnabled();
        if (tracerEnabled)
            ExecutionTracer.disable();

        final TestCase test = getNewTestCase();
        final TestFactory testFactory = TestFactory.getInstance();

        // Choose a random length between 1 (inclusive) and size (exclusive).
        final int length = Randomness.nextInt(1, size);

        // Then add random statements until the test case reaches the chosen length or we run out of
        // generation attempts.
        for (int num = 0; test.size() < length && num < Properties.MAX_ATTEMPTS; num++)
            // NOTE: Even though extremely unlikely, insertRandomStatement could fail every time
            // with return code -1, thus eventually exceeding MAX_ATTEMPTS. In this case, the
            // returned test case would indeed be empty!
            testFactory.insertRandomStatement(test, test.size() - 1);

        if (logger.isDebugEnabled())
            logger.debug("Randomized test case:" + test.toCode());

        if (tracerEnabled)
            ExecutionTracer.enable();

        return test;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Generate a random chromosome
     */
    @Override
    public TestChromosome getChromosome() {
        TestChromosome c = new TestChromosome();
        c.setTestCase(getRandomTestCase(Properties.CHROMOSOME_LENGTH));
        return c;
    }

    /**
     * Provided so that subtypes of this factory type can modify the returned
     * TestCase
     *
     * @return a {@link org.evosuite.testcase.TestCase} object.
     */
    protected TestCase getNewTestCase() {
        return new DefaultTestCase(); // empty test case
    }

}
