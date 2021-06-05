package org.evosuite.ga.metaheuristics.mosa.structural.adaptive;

import org.apache.commons.lang3.ArrayUtils;
import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageFactory;
import org.evosuite.coverage.mutation.WeakMutationTestFitness;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.mosa.structural.MultiCriteriaManager;
import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The version of the budget manager needed for the adaptive version (implement the different calculation of the
 * fitness function)
 *
 * @author Annibale Panichella, Giovanni Grano
 */
public class AdaptiveGoalManager extends MultiCriteriaManager {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveGoalManager.class);

    protected final Map<Integer, TestFitnessFunction> lineMap = new LinkedHashMap<>();
    protected final Map<Integer, TestFitnessFunction> weakMutationMap = new LinkedHashMap<>();

    /**
     * Stores the best values to check for heuristic stagnation
     */
    private final Map<TestFitnessFunction, Double> bestValues;
    private boolean hasBetterObjectives = false;
    protected List<AbstractIndicator> indicators;

    public AdaptiveGoalManager(List<TestFitnessFunction> fitnessFunctions) {
        super(fitnessFunctions);
        this.bestValues = new HashMap<>();
        for (TestFitnessFunction f : fitnessFunctions) {
            if (f instanceof LineCoverageTestFitness)
                lineMap.put(((LineCoverageTestFitness) f).getLine(), f);
            else if (f instanceof WeakMutationTestFitness) {
                weakMutationMap.put(((WeakMutationTestFitness) f).getMutation().getId(), f);
            }
        }
    }

    public void runTest(TestChromosome c) {
        if (c.getLastExecutionResult() == null) {
            // run the test
            TestCase test = c.getTestCase();
            ExecutionResult result = TestCaseExecutor.runTest(test);
            c.setLastExecutionResult(result);
            c.setChanged(false);
        }
    }

    @Override
    public void calculateFitness(TestChromosome c, GeneticAlgorithm<TestChromosome> ga) {
        TestCase test = c.getTestCase();
        ExecutionResult result = TestCaseExecutor.runTest(test);
        c.setLastExecutionResult(result);
        c.setChanged(false);

        computePerformanceMetrics(c);

        if (result.hasTimeout() || result.hasTestException() || result.getTrace().getCoveredLines().size() == 0) {
            currentGoals.forEach(f -> c.setFitness(f, Double.MAX_VALUE)); // assume minimization
            c.setPerformanceScore(Double.MAX_VALUE);
            return;
        }

        Set<TestFitnessFunction> visitedTargets = new LinkedHashSet<>(getUncoveredGoals().size() * 2);
        LinkedList<TestFitnessFunction> targets = new LinkedList<>(this.currentGoals);

        boolean toArchive = false;

        while (targets.size() > 0 && !ga.isFinished()) {
            TestFitnessFunction target = targets.poll();

            int past_size = visitedTargets.size();
            visitedTargets.add(target);
            if (past_size == visitedTargets.size())
                continue;

            assert target != null;
            double fitness = target.getFitness(c);
            if (bestValues.get(target) == null || fitness < bestValues.get(target)) {
                bestValues.put(target, fitness);
                this.hasBetterObjectives = true;
                toArchive = true;
            }

            if (fitness == 0.0) {
                toArchive = true;
                updateCoveredGoals(target, c);
                this.bestValues.remove(target);
                if (target instanceof BranchCoverageTestFitness) {
                    for (TestFitnessFunction child : graph.getStructuralChildren(target)) {
                        targets.addLast(child);
                    }
                    for (TestFitnessFunction dependentTarget : dependencies.get(target)) {
                        targets.addLast(dependentTarget);
                    }
                }
            } else {
                currentGoals.add(target);
            }

        }
        currentGoals.removeAll(this.getCoveredGoals());

        /* update of the archives */
        if (toArchive)
            updateArchive(c, result);
    }

    @Override
    public void updateArchive(TestChromosome c, ExecutionResult result) {
        super.updateArchive(c, result);
        if (ArrayUtils.contains(Properties.CRITERION, Properties.Criterion.LINE)) {
            for (Integer line : result.getTrace().getCoveredLines()) {
                updateCoveredGoals(this.lineMap.get(line), c);
            }
        }
        if (ArrayUtils.contains(Properties.CRITERION, Properties.Criterion.WEAKMUTATION)) {
            for (Integer id : result.getTrace().getInfectedMutants()) {
                if (this.weakMutationMap.containsKey(id))
                    updateCoveredGoals(this.weakMutationMap.get(id), c);
            }
        }
        if (ArrayUtils.contains(Properties.CRITERION, Properties.Criterion.METHOD)) {
            for (String id : result.getTrace().getCoveredMethods()) {
                TestFitnessFunction ff = MethodCoverageFactory.createMethodTestFitness(Properties.TARGET_CLASS, id);
                updateCoveredGoals(ff, c);
            }
        }
    }

    /**
     * Updates the best value map
     *
     * @param function the fitness function
     * @param fitness  the currently best value
     */
    public void updateBestValue(TestFitnessFunction function, Double fitness) {
        this.bestValues.put(function, fitness);
    }

    public Map<TestFitnessFunction, Double> getBestValues() {
        return bestValues;
    }

    /**
     * Returns the flag for better or worst objective reached
     */
    public boolean hasBetterObjectives() {
        return hasBetterObjectives;
    }

    public void setHasBetterObjectives(boolean hasBetterObjectives) {
        this.hasBetterObjectives = hasBetterObjectives;
    }

    public void setIndicators(List<AbstractIndicator> indicators) {
        this.indicators = indicators;
    }

    /**
     * Computes the set of performance indicator on a given test
     *
     * @param test the test to compute the values for
     */
    public void computePerformanceMetrics(TestChromosome test) {
        if (test.getIndicatorValues().size() > 0)
            return;

        double sum = 0.0;
        for (AbstractIndicator indicator : this.indicators) {
            double value = indicator.getIndicatorValue(test);
            sum += value / (value + 1);
        }
        test.setPerformanceScore(sum);
        logger.debug("performance score for {} = {}", test.hashCode(), test.getPerformanceScore());
    }

    /**
     * Returns the current solutions in the archive
     *
     * @return a set of {@link TestChromosome}
     */
    public Set<TestChromosome> getArchive() {
        return this.archive.getSolutions();
    }
}
