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
package org.evosuite.coverage.readability;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

public class ReadabilitySuiteFitness extends TestSuiteFitnessFunction {

    /**
     * 
     */
    private static final long serialVersionUID = 6243235746473531638L;

    /**
     * 
     */
    @Override
    public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite)
    {
        double average = 0.0;

        for (ExecutableChromosome ec : suite.getTestChromosomes()) {
            average += getScore(ec.toString());
        }

        average /= suite.getTestChromosomes().size();

        updateIndividual(this, suite, average);
        return average;
    }

    /**
     * 
     */
    public double getScore(String test)
    {
        // TODO
        return 0.0;
    }

    /**
     * 
     */
    @Override
    public boolean isMaximizationFunction() {
        return false;
    }
}
