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
package org.evosuite.coverage.dataflow;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness.DefUsePairType;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.LoggingUtils;

import java.util.*;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toCollection;

/**
 * Evaluate fitness of a test suite with respect to all of its def-use pairs
 * <p>
 * First simple and naive idea: Just take each DUGoal, calculate the minimal
 * fitness over all results in the suite once a goal is covered don't check for
 * it again in the end sum up all those fitness and that is s the resulting
 * suite-fitness
 *
 * @author Andre Mis
 */
public class DefUseCoverageSuiteFitness extends TestSuiteFitnessFunction {
    private static final long serialVersionUID = 1L;

    List<DefUseCoverageTestFitness> goals = DefUseCoverageFactory.getDUGoals();

    /**
     * Constant <code>totalGoals</code>
     */
    public Map<DefUsePairType, Integer> totalGoals = initTotalGoals();
    /**
     * Constant <code>mostCoveredGoals</code>
     */
    public final static Map<DefUsePairType, Integer> mostCoveredGoals = new HashMap<>();

    public Map<DefUsePairType, Integer> coveredGoals = new HashMap<>();

    // TODO: Need readObject?
    private transient final Map<Definition, Integer> maxDefinitionCount = new HashMap<>();

    private final Map<String, Integer> maxMethodCount = new HashMap<>();

    protected final BranchCoverageSuiteFitness branchFitness;

    public DefUseCoverageSuiteFitness() {
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;
        branchFitness = new BranchCoverageSuiteFitness();
        Properties.TEST_ARCHIVE = archive;

        for (DefUseCoverageTestFitness defUse : goals) {
            if (defUse.isParameterGoal()) {
                String methodName = defUse.getGoalUse().getMethodName();
                if (!maxMethodCount.containsKey(methodName)) {
                    maxMethodCount.put(methodName, 0);
                }

                maxMethodCount.put(methodName, maxMethodCount.get(methodName) + 1);
            } else {

                Definition def = defUse.getGoalDefinition();
                if (def == null) {
                    // TODO: For this def we need to count method invocations
                    logger.warn("Def is null for " + defUse);
                }
                if (!maxDefinitionCount.containsKey(def)) {
                    maxDefinitionCount.put(def, 0);
                }
                maxDefinitionCount.put(def, maxDefinitionCount.get(def) + 1);
            }
        }
        // Why should this be a warning??
        //		for (Definition def : maxDefinitionCount.keySet()) {
        //			logger.warn("Known definition: " + def + ", " + maxDefinitionCount.get(def));
        //		}
    }

    // Not working yet
    //@Override
    public double getFitnessAlternative(TestSuiteChromosome suite) {
        List<ExecutionResult> results = runTestSuite(suite);
        if (DefUseCoverageFactory.detectAliasingGoals(results)) {
            logger.debug("New total number of goals: " + goals.size());
            totalGoals = initTotalGoals();
            for (DefUsePairType type : totalGoals.keySet()) {
                logger.info(type + ":" + totalGoals.get(type));
            }
        }

        Map<Definition, Set<TestChromosome>> passedDefinitions = new HashMap<>();
        Map<Definition, Integer> passedDefinitionCount = new HashMap<>();
        Map<String, Set<TestChromosome>> executedMethods = new HashMap<>();
        Map<String, Integer> executedMethodsCount = new HashMap<>();

        for (Definition def : maxDefinitionCount.keySet()) {
            passedDefinitionCount.put(def, 0);
        }
        for (String methodName : maxMethodCount.keySet()) {
            executedMethodsCount.put(methodName, 0);
        }

        for (TestChromosome test : suite.getTestChromosomes()) {
            ExecutionResult result = test.getLastExecutionResult();

            if (result.hasTimeout()) {
                logger.debug("Skipping test with timeout");
                double fitness = goals.size() * 100;
                updateIndividual(suite, fitness);
                suite.setCoverage(this, 0.0);
                logger.debug("Test case has timed out, setting fitness to max value "
                        + fitness);
                return fitness;
            }

            for (Entry<Integer, Integer> entry : result.getTrace().getDefinitionExecutionCount().entrySet()) {
                Definition def = DefUsePool.getDefinitionByDefId(entry.getKey());
                if (def == null) {
                    logger.warn("Could not find def " + entry.getKey());
                    continue;
                }
                if (!passedDefinitions.containsKey(def))
                    passedDefinitions.put(def, new HashSet<>());

                if (!passedDefinitionCount.containsKey(def)) {
                    //logger.warn("Weird, definition is not known: " + def);
                    passedDefinitionCount.put(def, 0);
                }
                passedDefinitions.get(def).add(test);
                passedDefinitionCount.put(def,
                        passedDefinitionCount.get(def)
                                + entry.getValue());
            }

            for (Entry<String, Integer> entry : result.getTrace().getMethodExecutionCount().entrySet()) {
                if (executedMethodsCount.containsKey(entry.getKey()))
                    executedMethodsCount.put(entry.getKey(),
                            executedMethodsCount.get(entry.getKey())
                                    + entry.getValue());
                if (!executedMethods.containsKey(entry.getKey())) {
                    executedMethods.put(entry.getKey(), new HashSet<>());
                }
                executedMethods.get(entry.getKey()).add(test);
            }
			/*
			for (Integer id : result.getTrace().getPassedDefIDs()) {
				Definition def = DefUsePool.getDefinitionByDefId(id);
				if (!passedDefinitions.containsKey(def))
					passedDefinitions.put(def, new HashSet<TestChromosome>());

				passedDefinitions.get(def).add(test);
				passedDefinitionCount.put(def, passedDefinitionCount.get(def) + 1);
			}
			*/
        }

        // 1. Need to reach each definition
        double fitness = branchFitness.getFitness(suite);
        // logger.info("Branch fitness: " + fitness);

        // 3. For all covered defs, calculate minimal use distance
        //Set<DefUseCoverageTestFitness> coveredGoalsSet = DefUseExecutionTraceAnalyzer.getCoveredGoals(results);
        Set<DefUseCoverageTestFitness> coveredGoalsSet = new HashSet<>();//DefUseExecutionTraceAnalyzer.getCoveredGoals(results);

        initCoverageMaps();
        Set<Definition> notFullyCoveredDefs = new HashSet<>();
        boolean methodIsNotFullyCovered = false;

        for (DefUseCoverageTestFitness goal : goals) {
            if (coveredGoalsSet.contains(goal)) {
                continue;
            }

            double goalFitness = 2.0;
            Set<TestChromosome> coveringTests = new HashSet<>();

            if (goal.isParameterGoal()) {
                String methodKey = goal.getGoalUse().getClassName() + "."
                        + goal.getGoalUse().getMethodName();
                if (executedMethods.containsKey(methodKey)) {
                    coveringTests.addAll(executedMethods.get(methodKey));
                }
            } else {
                if (passedDefinitions.containsKey(goal.getGoalDefinition())) {
                    coveringTests.addAll(passedDefinitions.get(goal.getGoalDefinition()));
                }
            }
            if (coveringTests.isEmpty()) {
                logger.debug("No tests cover " + goal);
            } else {
                logger.debug("Checking " + coveringTests.size() + " tests covering "
                        + goal);

            }
            //			for (TestChromosome test : passedDefinitions.get(goal.getGoalDefinition())) {
            for (TestChromosome test : coveringTests) {
                // for (TestChromosome test : suite.getTestChromosomes()) {

                ExecutionResult result = test.getLastExecutionResult();
                DefUseFitnessCalculator calculator = new DefUseFitnessCalculator(goal,
                        test, result);
                //double resultFitness = goal.getFitness(test, result);
                double resultFitness = calculator.calculateDUFitness();

                if (resultFitness < goalFitness)
                    goalFitness = resultFitness;
                if (goalFitness == 0.0) {
                    result.test.addCoveredGoal(goal);
                    coveredGoalsSet.add(goal);
                    break;
                }
            }
            if (goalFitness > 0.0) {
                if (goal.isParameterGoal())
                    notFullyCoveredDefs.add(goal.getGoalDefinition());
                else
                    methodIsNotFullyCovered = true;
            }

            fitness += goalFitness;

        }

        // 2. Need to execute each definition X times
        // TODO ...unless all defuse pairs are covered?
        for (Entry<Definition, Integer> defCount : maxDefinitionCount.entrySet()) {
            if (notFullyCoveredDefs.contains(defCount.getKey())) {
                int executionCount = passedDefinitionCount.get(defCount.getKey());
                int max = defCount.getValue();
                if (executionCount < max) {
                    fitness += normalize(max - executionCount);
                }
            }
        }
        if (methodIsNotFullyCovered) {
            for (Entry<String, Integer> methodCount : maxMethodCount.entrySet()) {
                int executionCount = executedMethodsCount.get(methodCount.getKey());
                int max = methodCount.getValue();
                if (executionCount < max) {
                    fitness += normalize(max - executionCount);
                }
            }
        }

        countCoveredGoals(coveredGoalsSet);
        trackCoverageStatistics(suite);
        updateIndividual(suite, fitness);

        int coveredGoalCount = countCoveredGoals();
        int totalGoalCount = countTotalGoals();
        if (fitness == 0.0 && coveredGoalCount < totalGoalCount)
            throw new IllegalStateException("Fitness 0 implies 100% coverage "
                    + coveredGoalCount + " / " + totalGoals + " (covered / total)");

        return fitness;

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
     */
    @Override
    public double getFitness(TestSuiteChromosome suite) {
        // Deactivate coverage archive while measuring fitness, as auxiliar fitness functions
        // could attempt to claim coverage for it in the archive
        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;

        double fit = 0.0;
        if (Properties.ENABLE_ALTERNATIVE_SUITE_FITNESS)
            fit = getFitnessAlternative(suite);
        else
            fit = getFitnessOld(suite);

        Properties.TEST_ARCHIVE = archive;

        return fit;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.ga.FitnessFunction#getFitness(org.
     * evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     */
    //@Override
    public double getFitnessOld(TestSuiteChromosome suite) {
        logger.trace("Calculating defuse fitness");

        List<ExecutionResult> results = runTestSuite(suite);
        double fitness = 0.0;

        if (DefUseCoverageFactory.detectAliasingGoals(results)) {
            goals = DefUseCoverageFactory.getDUGoals();
            logger.debug("New total number of goals: " + goals.size());
            totalGoals = initTotalGoals();
            for (DefUsePairType type : totalGoals.keySet()) {
                logger.info(type + ":" + totalGoals.get(type));
            }
        }

        Set<DefUseCoverageTestFitness> coveredGoalsSet = DefUseExecutionTraceAnalyzer.getCoveredGoals(results);

        initCoverageMaps();

        for (DefUseCoverageTestFitness goal : goals) {
            if (coveredGoalsSet.contains(goal)) {
                goal.setCovered(true);
                continue;
            }

            double goalFitness = 2.0;
            for (ExecutionResult result : results) {
                TestChromosome tc = new TestChromosome();
                tc.setTestCase(result.test);
                double resultFitness = goal.getFitness(tc, result);
                if (resultFitness < goalFitness)
                    goalFitness = resultFitness;
                if (goalFitness == 0.0) {
                    result.test.addCoveredGoal(goal);
                    coveredGoalsSet.add(goal);
                    goal.setCovered(true);
                    break;
                }
            }
            fitness += goalFitness;
        }

        countCoveredGoals(coveredGoalsSet);
        trackCoverageStatistics(suite);
        updateIndividual(suite, fitness);

        int coveredGoalCount = countCoveredGoals();
        int totalGoalCount = countTotalGoals();
        if (fitness == 0.0 && coveredGoalCount < totalGoalCount)
            throw new IllegalStateException("Fitness 0 implies 100% coverage "
                    + coveredGoalCount + " / " + totalGoals + " (covered / total)");

        return fitness;
    }

    public static Map<DefUsePairType, Integer> initTotalGoals() {
        Map<DefUsePairType, Integer> r = new HashMap<>();

        // init map
        for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType.values())
            r.put(type, 0);

        int num = 0;
        // count total goals according to type
        for (DefUseCoverageTestFitness goal : DefUseCoverageFactory.getDUGoals()) {
            logger.info("Goal " + num);
            num++;
            r.put(goal.getType(), r.get(goal.getType()) + 1);
        }

        return r;
    }

    private void initCoverageMaps() {
        for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType.values()) {
            coveredGoals.put(type, 0);
            mostCoveredGoals.putIfAbsent(type, 0);
        }
    }

    private int countCoveredGoals() {
        return countGoalsIn(coveredGoals);
    }

    /**
     * <p>
     * countMostCoveredGoals
     * </p>
     *
     * @return a int.
     */
    public static int countMostCoveredGoals() {
        return countGoalsIn(mostCoveredGoals);
    }

    private int countTotalGoals() {
        return countGoalsIn(totalGoals);
    }

    private static int countGoalsIn(Map<DefUsePairType, Integer> goalMap) {
        int r = 0;
        for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType.values()) {
            if (goalMap.get(type) != null)
                r += goalMap.get(type);
        }
        return r;
    }

    private void trackCoverageStatistics(TestSuiteChromosome suite) {

        setMostCovered();
        setSuiteCoverage(suite);
    }

    private void countCoveredGoals(Set<DefUseCoverageTestFitness> coveredGoalsSet) {
        for (DefUseCoverageTestFitness goal : coveredGoalsSet) {
            coveredGoals.put(goal.getType(), coveredGoals.get(goal.getType()) + 1);

        }
    }

    private void setSuiteCoverage(TestSuiteChromosome suite) {

        if (goals.size() > 0)
            suite.setCoverage(this, countCoveredGoals() / (double) goals.size());
        else
            suite.setCoverage(this, 1.0);
    }

    private void setMostCovered() {

        for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType.values()) {
            if (mostCoveredGoals.get(type) < coveredGoals.get(type)) {
                mostCoveredGoals.put(type, coveredGoals.get(type));
                if (mostCoveredGoals.get(type) > totalGoals.get(type))
                    throw new IllegalStateException(
                            "Can't cover more goals than there exist of type " + type
                                    + " " + mostCoveredGoals.get(type) + " / "
                                    + totalGoals.get(type) + " (mostCovered / total)");
            }
        }
    }

    /**
     * <p>
     * printCoverage
     * </p>
     */
    public static void printCoverage() {

        LoggingUtils.getEvoLogger().info("* Time spent optimizing covered goals analysis: "
                + DefUseExecutionTraceAnalyzer.timeGetCoveredGoals
                + "ms");
        Map<DefUsePairType, Integer> totalGoals = initTotalGoals();
        for (DefUsePairType type : DefUseCoverageTestFitness.DefUsePairType.values()) {
            LoggingUtils.getEvoLogger().info("* Covered goals of type " + type + ": "
                    + mostCoveredGoals.get(type) + " / "
                    + totalGoals.get(type));
            for (DefUseCoverageTestFitness pair : getPairsOfType(type)) {
                if (pair.isCovered()) {
                    LoggingUtils.getEvoLogger().info("*(X) " + pair);
                } else {
                    LoggingUtils.getEvoLogger().info("*( ) " + pair);
                }
            }

        }

        LoggingUtils.getEvoLogger().info("* Covered " + countMostCoveredGoals() + "/"
                + countGoalsIn(totalGoals) + " goals");
    }

    /**
     * Returns a list of du pairs of the specific type.
     *
     * @param type the type of pairs. See
     *             DefUseCoverageTestFitness.DefUsePairType
     * @return
     */
    private static ArrayList<DefUseCoverageTestFitness> getPairsOfType(DefUsePairType type) {
        return DefUseCoverageFactory.getDUGoals().stream()
                .filter(pair -> pair.getType() == type)
                .collect(toCollection(ArrayList::new));
    }
}
