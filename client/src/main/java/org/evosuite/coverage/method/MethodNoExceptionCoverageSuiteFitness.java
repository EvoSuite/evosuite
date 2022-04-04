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
package org.evosuite.coverage.method;

import org.evosuite.Properties;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Fitness function for a whole test suite for all methods considering only normal behaviour (no exceptions)
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class MethodNoExceptionCoverageSuiteFitness extends MethodCoverageSuiteFitness {

    private static final long serialVersionUID = -704561530935529634L;

    private final static Logger logger = LoggerFactory.getLogger(MethodNoExceptionCoverageSuiteFitness.class);

    /**
     * Initialize the set of known coverage goals
     */
    @Override
    protected void determineCoverageGoals() {
        List<MethodNoExceptionCoverageTestFitness> goals = new MethodNoExceptionCoverageFactory().getCoverageGoals();
        for (MethodNoExceptionCoverageTestFitness goal : goals) {
            methodCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), goal);
            if (Properties.TEST_ARCHIVE)
                Archive.getArchiveInstance().addTarget(goal);
        }
    }

    @Override
    protected void handleConstructorExceptions(TestChromosome test, ExecutionResult result, Set<String> calledMethods) {
        return; // No-op
    }

    /**
     * Some useful debug information
     *
     * @param coveredMethods
     * @param fitness
     */
    @Override
    protected void printStatusMessages(TestSuiteChromosome suite,
                                       int coveredMethods, double fitness) {
        if (coveredMethods > maxCoveredMethods) {
            logger.info("(Methods No-Exc) Best individual covers " + coveredMethods + "/"
                    + totalMethods + " methods");
            maxCoveredMethods = coveredMethods;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
        if (fitness < bestFitness) {
            logger.info("(Fitness) Best individual covers " + coveredMethods + "/"
                    + totalMethods + " methods");
            bestFitness = fitness;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());
        }
    }

}
