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

import org.evosuite.Properties;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;

/**
 * Historical concrete TestFitnessFactories only implement the getGoals() method
 * of TestFitnessFactory. Those old Factories can just extend these
 * AstractFitnessFactory to support the new method getFitness()
 *
 * @author Sebastian Steenbuck
 */
public abstract class AbstractFitnessFactory<T extends TestFitnessFunction> implements
        TestFitnessFactory<T> {

    /**
     * A concrete factory can store the time consumed to initially compute all
     * coverage goals in this field in order to track this information in
     * SearchStatistics.
     */
    public static long goalComputationTime = 0L;


    protected boolean isCUT(String className) {
        return Properties.TARGET_CLASS.equals("")
                || (className.equals(Properties.TARGET_CLASS)
                || className.startsWith(Properties.TARGET_CLASS + "$"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFitness(TestSuiteChromosome suite) {

        ExecutionTracer.enableTraceCalls();

        int coveredGoals = 0;
        for (T goal : getCoverageGoals()) {
            for (TestChromosome test : suite.getTestChromosomes()) {
                if (goal.isCovered(test)) {
                    coveredGoals++;
                    break;
                }
            }
        }

        ExecutionTracer.disableTraceCalls();

        return getCoverageGoals().size() - coveredGoals;
    }
}
