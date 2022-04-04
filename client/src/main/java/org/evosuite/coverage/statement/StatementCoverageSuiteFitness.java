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
package org.evosuite.coverage.statement;

import org.evosuite.Properties;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Statement fitness function.
 *
 * @author Gordon Fraser, José Campos
 */
public class StatementCoverageSuiteFitness extends TestSuiteFitnessFunction {

    private static final long serialVersionUID = -3037573618694670748L;

    private final int numStatements;
    private final Set<TestFitnessFunction> statementGoals = new LinkedHashSet<>();

    private final Set<TestFitnessFunction> removedStatements = new LinkedHashSet<>();
    private final Set<TestFitnessFunction> toRemoveStatements = new LinkedHashSet<>();

    public StatementCoverageSuiteFitness() {
        List<StatementCoverageTestFitness> goals = new StatementCoverageFactory().getCoverageGoals();

        for (StatementCoverageTestFitness goal : goals) {
            this.statementGoals.add(goal);

            if (Properties.TEST_ARCHIVE) {
                Archive.getArchiveInstance().addTarget(goal);
            }
        }

        this.numStatements = this.statementGoals.size();
        logger.info("Total statement goals: " + this.numStatements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateCoveredGoals() {
        if (!Properties.TEST_ARCHIVE) {
            return false;
        }

        for (TestFitnessFunction goal : this.toRemoveStatements) {
            if (this.statementGoals.remove(goal)) {
                this.removedStatements.add(goal);
            } else {
                throw new IllegalStateException("goal to remove not found");
            }
        }

        this.toRemoveStatements.clear();
        logger.info("Current state of archive: " + Archive.getArchiveInstance().toString());

        assert this.numStatements == this.statementGoals.size() + this.removedStatements.size();
        return true;
    }

    /**
     * Iterate over all execution results and summarise statistics.
     *
     * @param results
     * @param coveredStatements
     * @return
     */
    private boolean analyzeTraces(List<ExecutionResult> results, Set<TestFitnessFunction> coveredStatements) {
        boolean hasTimeoutOrTestException = false;

        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
                continue;
            }

            TestChromosome test = new TestChromosome();
            test.setTestCase(result.test);
            test.setLastExecutionResult(result);
            test.setChanged(false);

            for (TestFitnessFunction goal : this.statementGoals) {
                double fit = goal.getFitness(test, result); // archive is updated by the TestFitnessFunction class

                if (fit == 0.0) {
                    coveredStatements.add(goal); // helper to count the number of covered goals
                    this.toRemoveStatements.add(goal); // goal to not be considered by the next iteration of the evolutionary algorithm
                }
            }
        }

        return hasTimeoutOrTestException;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFitness(TestSuiteChromosome suite) {
        List<ExecutionResult> results = runTestSuite(suite);
        double fitness = 0.0;

        Set<TestFitnessFunction> coveredStatements = new LinkedHashSet<>();
        boolean hasTimeoutOrTestException = analyzeTraces(results, coveredStatements);

        if (hasTimeoutOrTestException) {
            logger.info("Test suite has timed out, setting fitness to max value " + this.numStatements);
            fitness = this.numStatements;
        } else {
            int totalStatements = this.numStatements;
            int numCoveredStatements = coveredStatements.size() + this.removedStatements.size();
            suite.setNumOfCoveredGoals(this, numCoveredStatements);

            if (totalStatements > 0) {
                suite.setCoverage(this, (double) numCoveredStatements / (double) totalStatements);
            } else {
                suite.setCoverage(this, 1.0);
            }
            fitness = normalize(totalStatements - numCoveredStatements);

            assert (numCoveredStatements <= totalStatements) : "Covered " + numCoveredStatements + " vs total goals "
                    + totalStatements;
            assert (fitness >= 0.0);
            assert (fitness != 0.0 || numCoveredStatements == totalStatements) : "Fitness: " + fitness + ", "
                    + "coverage: " + numCoveredStatements + "/" + totalStatements;
            assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
                    + suite.getCoverage(this);
        }

        updateIndividual(suite, fitness);

        return fitness;
    }

}
