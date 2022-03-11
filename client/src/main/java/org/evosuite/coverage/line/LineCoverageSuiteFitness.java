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
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.archive.Archive;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * Fitness function for a whole test suite for all branches
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class LineCoverageSuiteFitness extends TestSuiteFitnessFunction {

    private static final long serialVersionUID = -6369027784777941998L;

    private final static Logger logger = LoggerFactory.getLogger(LineCoverageSuiteFitness.class);

    // target goals
    private final int numLines;
    private final Map<Integer, TestFitnessFunction> lineGoals = new LinkedHashMap<>();

    private final Set<Integer> removedLines = new LinkedHashSet<>();
    private final Set<Integer> toRemoveLines = new LinkedHashSet<>();

    // Some stuff for debug output
    private int maxCoveredLines = 0;
    private double bestFitness = Double.MAX_VALUE;

    private final Set<Integer> branchesToCoverTrue = new LinkedHashSet<>();
    private final Set<Integer> branchesToCoverFalse = new LinkedHashSet<>();
    private final Set<Integer> branchesToCoverBoth = new LinkedHashSet<>();

    public LineCoverageSuiteFitness() {
        @SuppressWarnings("unused")
        String prefix = Properties.TARGET_CLASS_PREFIX;

        /* TODO: Would be nice to use a prefix here */
//		for(String className : LinePool.getKnownClasses()) {
//			lines.addAll(LinePool.getLines(className));
//		}

        List<LineCoverageTestFitness> goals = new LineCoverageFactory().getCoverageGoals();
        for (LineCoverageTestFitness goal : goals) {
            lineGoals.put(goal.getLine(), goal);
            if (Properties.TEST_ARCHIVE)
                Archive.getArchiveInstance().addTarget(goal);
        }
        this.numLines = lineGoals.size();
        logger.info("Total line coverage goals: " + this.numLines);

        initializeControlDependencies();
    }

    @Override
    public boolean updateCoveredGoals() {
        if (!Properties.TEST_ARCHIVE) {
            return false;
        }

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
        fitness += getControlDependencyGuidance(results);
        logger.info("Branch distances: " + fitness);

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
        suite.setNumOfNotCoveredGoals(this, totalLines - numCoveredLines);

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

    /**
     * Add guidance to the fitness function by including branch distances on
     * all control dependencies
     */
    private void initializeControlDependencies() {
        // In case we target more than one class (context, or inner classes)
        Set<String> targetClasses = new LinkedHashSet<>();
        for (TestFitnessFunction ff : lineGoals.values()) {
            targetClasses.add(ff.getTargetClass());
        }
        for (String className : targetClasses) {
            List<BytecodeInstruction> instructions = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getInstructionsIn(className);

            if (instructions == null) {
                logger.info("No instructions known for class {} (is it an enum?)", className);
                continue;
            }
            for (BytecodeInstruction bi : instructions) {
                if (bi.getBasicBlock() == null) {
                    // Labels get no basic block. TODO - why?
                    continue;
                }

                // The order of CDs may be nondeterminstic
                // TODO: A better solution would be to make the CD order deterministic rather than sorting here
                List<ControlDependency> cds = new ArrayList<>(bi.getControlDependencies());
                Collections.sort(cds);
                for (ControlDependency cd : cds) {
                    if (cd.getBranchExpressionValue()) {
                        branchesToCoverTrue.add(cd.getBranch().getActualBranchId());
                    } else {
                        branchesToCoverFalse.add(cd.getBranch().getActualBranchId());
                    }
                }
            }
        }
        branchesToCoverBoth.addAll(branchesToCoverTrue);
        branchesToCoverBoth.retainAll(branchesToCoverFalse);
        branchesToCoverTrue.removeAll(branchesToCoverBoth);
        branchesToCoverFalse.removeAll(branchesToCoverBoth);

        logger.info("Covering branches true: " + branchesToCoverTrue);
        logger.info("Covering branches false: " + branchesToCoverFalse);
        logger.info("Covering branches both: " + branchesToCoverBoth);
    }

    private double getControlDependencyGuidance(List<ExecutionResult> results) {
        Map<Integer, Integer> predicateCount = new LinkedHashMap<>();
        Map<Integer, Double> trueDistance = new LinkedHashMap<>();
        Map<Integer, Double> falseDistance = new LinkedHashMap<>();

        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                continue;
            }
            for (Entry<Integer, Integer> entry : result.getTrace().getPredicateExecutionCount().entrySet()) {
                if (!predicateCount.containsKey(entry.getKey()))
                    predicateCount.put(entry.getKey(), entry.getValue());
                else {
                    predicateCount.put(entry.getKey(),
                            predicateCount.get(entry.getKey())
                                    + entry.getValue());
                }
            }
            for (Entry<Integer, Double> entry : result.getTrace().getTrueDistances().entrySet()) {
                if (!trueDistance.containsKey(entry.getKey()))
                    trueDistance.put(entry.getKey(), entry.getValue());
                else {
                    trueDistance.put(entry.getKey(),
                            Math.min(trueDistance.get(entry.getKey()),
                                    entry.getValue()));
                }
            }
            for (Entry<Integer, Double> entry : result.getTrace().getFalseDistances().entrySet()) {
                if (!falseDistance.containsKey(entry.getKey()))
                    falseDistance.put(entry.getKey(), entry.getValue());
                else {
                    falseDistance.put(entry.getKey(),
                            Math.min(falseDistance.get(entry.getKey()),
                                    entry.getValue()));
                }
            }
        }

        double distance = 0.0;

        for (Integer branchId : branchesToCoverBoth) {
            if (!predicateCount.containsKey(branchId)) {
                distance += 2.0;
            } else if (predicateCount.get(branchId) == 1) {
                distance += 1.0;
            } else {
                distance += normalize(trueDistance.get(branchId));
                distance += normalize(falseDistance.get(branchId));
            }
        }

        for (Integer branchId : branchesToCoverTrue) {
            if (!trueDistance.containsKey(branchId)) {
                distance += 1;
            } else {
                distance += normalize(trueDistance.get(branchId));
            }
        }

        for (Integer branchId : branchesToCoverFalse) {
            if (!falseDistance.containsKey(branchId)) {
                distance += 1;
            } else {
                distance += normalize(falseDistance.get(branchId));
            }
        }

        return distance;
    }

}
