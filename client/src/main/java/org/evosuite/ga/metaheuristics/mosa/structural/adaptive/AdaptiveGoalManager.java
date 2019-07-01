package org.evosuite.ga.metaheuristics.mosa.structural.adaptive;

import org.apache.commons.lang3.ArrayUtils;
import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageFactory;
import org.evosuite.coverage.mutation.WeakMutationTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.CoverageArchive;
import org.evosuite.ga.comparators.PerformanceScoreComparator;
import org.evosuite.ga.metaheuristics.mosa.structural.MultiCriteriatManager;
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
public class AdaptiveGoalManager<T extends Chromosome> extends MultiCriteriatManager<T> {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveGoalManager.class);

    protected final Map<Integer, FitnessFunction<T>> lineMap = new LinkedHashMap<Integer, FitnessFunction<T>>();
    protected final Map<Integer, FitnessFunction<T>> weakMutationMap = new LinkedHashMap<Integer, FitnessFunction<T>>();

    private PerformanceScoreComparator comparator = new PerformanceScoreComparator();

    // stores the best values for check heuristic stagnation
    private Map<FitnessFunction<T>, Double> bestValues;
    private boolean hasBetterObjectives = false;
    protected List<AbstractIndicator> indicators;

    public AdaptiveGoalManager(List<FitnessFunction<T>> fitnessFunctions) {
        super(fitnessFunctions);
        this.bestValues = new HashMap<>();
        for (FitnessFunction f : fitnessFunctions) {
            if (f instanceof LineCoverageTestFitness)
                lineMap.put(((LineCoverageTestFitness) f).getLine(), f);
            else if (f instanceof WeakMutationTestFitness) {
                weakMutationMap.put(((WeakMutationTestFitness) f).getMutation().getId(), f);
            }
        }
    }

    public void runTest(T c) {
        TestChromosome tch = (TestChromosome) c;
        if (tch.getLastExecutionResult() == null) {
            // run the test
            TestCase test = ((TestChromosome) c).getTestCase();
            ExecutionResult result = TestCaseExecutor.runTest(test);
            ((TestChromosome) c).setLastExecutionResult(result);
            c.setChanged(false);
        }
    }

    @Override
    public void calculateFitness(T c){
        this.runTest(c);

        ExecutionResult result = ((TestChromosome) c).getLastExecutionResult();
        computePerformanceMetrics(c);

        /* check exceptions and if the test does not cover anything */
        if (result.hasTimeout() || result.hasTestException() || result.getTrace().getCoveredLines().size() == 0){
            for (FitnessFunction<T> f : currentGoals)
                c.setFitness(f, Double.MAX_VALUE);

            c.setPerformanceScore(Double.MAX_VALUE);
            return;
        }

        /* ------------------------------------- update of best values ----------------------------------- */
        // 1) we update the set of currents goals
        Set<FitnessFunction<T>> visitedTargets = new LinkedHashSet<FitnessFunction<T>>(uncoveredGoals.size() * 2);
        LinkedList<FitnessFunction<T>> targets = new LinkedList<FitnessFunction<T>>();
        targets.addAll(this.currentGoals);

        boolean toArchive = false;

        while (targets.size() > 0) {
            FitnessFunction<T> fitnessFunction = targets.poll();

            int past_size = visitedTargets.size();
            visitedTargets.add(fitnessFunction);
            if (past_size == visitedTargets.size())
                continue;

            double value = fitnessFunction.getFitness(c);
            if (bestValues.get(fitnessFunction) == null || value < bestValues.get(fitnessFunction)) {
                bestValues.put(fitnessFunction, value);
                this.hasBetterObjectives = true;
                toArchive = true;
            }

            if (value == 0.0) {
                toArchive = true;
                updateCoveredGoals(fitnessFunction, c);
                this.bestValues.remove(fitnessFunction);
                if (fitnessFunction instanceof BranchCoverageTestFitness) {
                    for (FitnessFunction<T> child : graph.getStructuralChildren(fitnessFunction)) {
                        targets.addLast(child);
                    }
                    for (FitnessFunction<T> dependentTarget : dependencies.get(fitnessFunction)) {
                        targets.addLast(dependentTarget);
                    }
                }
            } else {
                currentGoals.add(fitnessFunction);
            }

        }
        currentGoals.removeAll(coveredGoals.keySet());
        /* update of the archives */
        if (toArchive)
            updateArchive(c, result);
    }

    @Override
    public void updateArchive(T c, ExecutionResult result) {
        super.updateArchive(c, result);
        if (ArrayUtils.contains(Properties.CRITERION, Properties.Criterion.LINE)) {
            for (Integer line : result.getTrace().getCoveredLines()) {
                updateCoveredGoals(this.lineMap.get(line), c);
            }
        }
        if (ArrayUtils.contains(Properties.CRITERION, Properties.Criterion.WEAKMUTATION)) {
            for (Integer id : result.getTrace().getInfectedMutants()) {
                if (this.weakMutationMap.keySet().contains(id))
                    updateCoveredGoals(this.weakMutationMap.get(id), c);
            }
        }
        if (ArrayUtils.contains(Properties.CRITERION, Properties.Criterion.METHOD)) {
            for (String id : result.getTrace().getCoveredMethods()) {
                FitnessFunction ff = MethodCoverageFactory.createMethodTestFitness(Properties.TARGET_CLASS, id);
                updateCoveredGoals(ff, c);
            }
        }
    }

    /**
     * We overrides here the default behavior that looks at the size for the update of the archive!
     * Here, we look at the min-max normalized performance score; the higher the better.
     * Consider to use the base implementation providing some comparators.
     */
    @Override
    @SuppressWarnings("Duplicates")
    protected void updateCoveredGoals(FitnessFunction<T> f, T tc) {
        TestChromosome tch = (TestChromosome) tc;
        tch.getTestCase().getCoveredGoals().add((TestFitnessFunction) f);

        // update covered targets
        T best = coveredGoals.get(f);
        if (best == null){
            coveredGoals.put(f, tc);
            uncoveredGoals.remove(f);
            currentGoals.remove(f);
            CoverageArchive.getArchiveInstance().updateArchive((TestFitnessFunction) f, tch, tc.getFitness(f));
        } else {
            boolean toUpdate = comparator.compare(tc, best) == -1;
            if (toUpdate) {
                coveredGoals.put(f, tc);
                CoverageArchive.getArchiveInstance().updateArchive((TestFitnessFunction) f, tch, tc.getFitness(f));
            }
        }
    }

    /**
     * Updates the best value map
     * @param function the fitness function
     * @param fitness the currently best value
     */
    public void updateBestValue(FitnessFunction<T> function, Double fitness) {
        this.bestValues.put(function, fitness);
    }

    public Map<FitnessFunction<T>, Double> getBestValues() {
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

    public void computePerformanceMetrics(T test) {
        if (test.getIndicatorValues().size()>0)
            return;

        double sum = 0.0;
        for (AbstractIndicator indicator : this.indicators) {
            double value = indicator.getIndicatorValue(test);
            sum += value/(value+1);
        }
        test.setPerformanceScore(sum);
        logger.debug("performance score for {} = {}", test.hashCode(), test.getPerformanceScore());
    }

    @Override
    public Set<T> getArchive(){
        Set<T> set = new HashSet<>();
        for (TestChromosome tch : CoverageArchive.getArchiveInstance().getSolutions()){
            set.add((T) tch);
        }
        return set;
    }
}
