package org.evosuite.ga.metaheuristics.mosa.structural.adaptive;

import org.evosuite.ga.metaheuristics.mosa.structural.MultiCriteriaManager;
import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The version of the budget manager needed for the adaptive version (implement the different calculation of the
 * fitness function)
 *
 * @author Annibale Panichella, Giovanni Grano
 */
public class AdaptiveGoalManager extends MultiCriteriaManager {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveGoalManager.class);

    /**
     * Stores the best values to check for heuristic stagnation
     */
    private final Map<TestFitnessFunction, Double> bestValues;
    private boolean hasBetterObjectives = false;
    protected List<AbstractIndicator<TestChromosome>> indicators;

    public AdaptiveGoalManager(List<TestFitnessFunction> fitnessFunctions) {
        super(fitnessFunctions);
        this.bestValues = new HashMap<>();
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
    protected ExecutionResult executeTest(TestChromosome chromosome) {
        ExecutionResult result = super.executeTest(chromosome);
        computePerformanceMetrics(chromosome);
        return result;
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

    public void setIndicators(List<AbstractIndicator<TestChromosome>> indicators) {
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
        for (AbstractIndicator<TestChromosome> indicator : this.indicators) {
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
