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
package org.evosuite.testsuite;

import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.testcase.TestFitnessFunction;

import java.util.List;

/**
 * This adapters allows the use of a TestSuiteFitnessFunction as a
 * TestFitnessFactory for the purpose of TestSuite minimization.
 *
 * @author Sebastian Steenbuck
 */
public class TestSuiteFitnessFunc_to_TestFitnessFactory_Adapter implements
        TestFitnessFactory<TestFitnessFunction> {

    private final TestSuiteFitnessFunction testSuiteFitness;

    /**
     * <p>
     * Constructor for TestSuiteFitnessFunc_to_TestFitnessFactory_Adapter.
     * </p>
     *
     * @param testSuiteFitness a {@link org.evosuite.testsuite.TestSuiteFitnessFunction}
     *                         object.
     */
    public TestSuiteFitnessFunc_to_TestFitnessFactory_Adapter(
            TestSuiteFitnessFunction testSuiteFitness) {
        this.testSuiteFitness = testSuiteFitness;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TestFitnessFunction> getCoverageGoals() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFitness(TestSuiteChromosome suite) {
        return testSuiteFitness.getFitness(suite);
    }

}
