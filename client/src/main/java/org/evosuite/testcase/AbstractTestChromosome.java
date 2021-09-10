/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

public abstract class AbstractTestChromosome<E extends AbstractTestChromosome<E>> extends ExecutableChromosome<E> {


    private static final long serialVersionUID = 8274081309132365034L;
    /**
     * The test case encoded in this chromosome
     */
    protected TestCase test = new DefaultTestCase();

    @Override
    public abstract void crossOver(E other, int position1, int position2) throws ConstructionFailedException;

    /**
     * <p>
     * setTestCase
     * </p>
     *
     * @param testCase a {@link org.evosuite.testcase.TestCase} object.
     */
    public void setTestCase(TestCase testCase) {
        test = testCase;
        clearCachedResults();
        clearCachedMutationResults();
        setChanged(true);
    }

    /**
     * <p>
     * getTestCase
     * </p>
     *
     * @return a {@link org.evosuite.testcase.TestCase} object.
     */
    public TestCase getTestCase() {
        return test;
    }

    public abstract ExecutionResult executeForFitnessFunction(
            TestSuiteFitnessFunction testSuiteFitnessFunction);
}

