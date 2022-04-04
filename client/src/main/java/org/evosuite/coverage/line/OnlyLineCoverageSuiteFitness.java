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
package org.evosuite.coverage.line;

import org.evosuite.Properties;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OnlyLineCoverageSuiteFitness extends TestSuiteFitnessFunction {

    private static final long serialVersionUID = -6369027784777941998L;

    private final static Logger logger = LoggerFactory.getLogger(OnlyLineCoverageSuiteFitness.class);

    // Coverage targets
    private final Map<Integer, TestFitnessFunction> lineGoals = new LinkedHashMap<>();
    private final int numLines;

    private final Set<Integer> removedLines = new LinkedHashSet<>();
    private final Set<Integer> toRemoveLines = new LinkedHashSet<>();

    // Some stuff for debug output
    private int maxCoveredLines = 0;
    private double bestFitness = Double.MAX_VALUE;

    public OnlyLineCoverageSuiteFitness() {
        @SuppressWarnings("unused")
        String prefix = Properties.TARGET_CLASS_PREFIX;

        /* TODO: Would be nice to use a prefix here */
//		for(String className : LinePool.getKnownClasses()) {		
//			lines.addAll(LinePool.getLines(className));
//		}
//		logger.info("Total line coverage goals: " + lines);

        List<LineCoverageTestFitness> goals = new LineCoverageFactory().getCoverageGoals();
        for (LineCoverageTestFitness goal : goals) {
            lineGoals.put(goal.getLine(), goal);
            if (Properties.TEST_ARCHIVE)
                Archive.getArchiveInstance().addTarget(goal);
        }

        this.numLines = lineGoals.size();
        logger.info("Total line coverage goals: " + this.numLines);
    }

    @Override
    public boolean updateCoveredGoals() {
        if (!Properties.TEST_ARCHIVE)
            return false;

        for (Integer goalID : this.toRemoveLines) {
            TestFitnessFunction ff = this.lineGoals.remove(goalID);
            if (ff != null) {
                this.removedLines.add(goalID);
            } else {
                throw new IllegalStateException("goal to remove not found");
            }
        }

        this.toRemoveLines.clear();
        logger.info("Current state of archive: " + Archive.getArchiveInstance().toString());

        assert this.numLines == this.lineGoals.size() + this.removedLines.size();

        return true;
    }

    /**
     * Iterate over all execution results and summarize statistics
     *
     * @param results
     * @param coveredLines
     * @return
     */
    private boolean analyzeTraces(List<ExecutionResult> results, Set<Integer> coveredLines) {
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

            for (Integer goalID : this.lineGoals.keySet()) {
                TestFitnessFunction goal = this.lineGoals.get(goalID);

                double fit = goal.getFitness(test, result); // archive is updated by the TestFitnessFunction class

                if (fit == 0.0) {
                    coveredLines.add(goalID); // helper to count the number of covered goals
                    this.toRemoveLines.add(goalID); // goal to not be considered by the next iteration of the evolutionary algorithm
                }
            }
        }

        return hasTimeoutOrTestException;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Execute all tests and count covered branches
     */
    @Override
    public double getFitness(TestSuiteChromosome suite) {
        logger.trace("Calculating branch fitness");
        double fitness = 0.0;

        List<ExecutionResult> results = runTestSuite(suite);

        // Collect stats in the traces
        Set<Integer> coveredLines = new LinkedHashSet<>();
        boolean hasTimeoutOrTestException = analyzeTraces(results, coveredLines);

        int totalLines = this.numLines;
        int numCoveredLines = coveredLines.size() + this.removedLines.size();

        logger.debug("Covered " + numCoveredLines + " out of " + totalLines + " lines, " + removedLines.size() + " in archive");
        fitness += normalize(totalLines - numCoveredLines);

        printStatusMessages(suite, numCoveredLines, fitness);

        if (totalLines > 0)
            suite.setCoverage(this, (double) numCoveredLines / (double) totalLines);
        else
            suite.setCoverage(this, 1.0);

        suite.setNumOfCoveredGoals(this, numCoveredLines);

        if (hasTimeoutOrTestException) {
            logger.info("Test suite has timed out, setting fitness to max value " + totalLines);
            fitness = totalLines;
            //suite.setCoverage(0.0);
        }

        updateIndividual(suite, fitness);

        assert (numCoveredLines <= totalLines) : "Covered " + numCoveredLines + " vs total goals " + totalLines;
        assert (fitness >= 0.0);
        assert (fitness != 0.0 || numCoveredLines == totalLines) : "Fitness: " + fitness + ", "
                + "coverage: " + numCoveredLines + "/" + totalLines;
        assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
                + suite.getCoverage(this);

        return fitness;
    }

    /**
     * Some useful debug information
     *
     * @param coveredLines
     * @param fitness
     */
    private void printStatusMessages(TestSuiteChromosome suite,
                                     int coveredLines, double fitness) {
        if (coveredLines > maxCoveredLines) {
            maxCoveredLines = coveredLines;
            logger.info("(Lines) Best individual covers " + coveredLines + "/"
                    + this.numLines + " lines");
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());
        }

        if (fitness < bestFitness) {
            logger.info("(Fitness) Best individual covers " + coveredLines + "/"
                    + this.numLines + " lines");
            bestFitness = fitness;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
    }
}
