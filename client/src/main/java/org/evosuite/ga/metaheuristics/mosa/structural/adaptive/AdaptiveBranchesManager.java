package org.evosuite.ga.metaheuristics.mosa.structural.adaptive;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.mosa.structural.BranchesManager;
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
 * @author Giovanni Grano
 */
public class AdaptiveBranchesManager<T extends Chromosome> extends BranchesManager<T> {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveBranchesManager.class);
    private ArchiveUpdate updater;

    // stores the best values for check heuristic stagnation
    private Map<FitnessFunction<T>, Double> bestValues;
    private boolean hasBetterObjectives = false;
    protected List<AbstractIndicator> indicators;

    public AdaptiveBranchesManager(List<FitnessFunction<T>> fitnessFunctions) {
        super(fitnessFunctions);
        this.bestValues = new HashMap<>();
        updater = new PerformanceUpdate();
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void calculateFitness(T c) {
        /* run the test */
        TestCase test = ((TestChromosome) c).getTestCase();
        ExecutionResult result = TestCaseExecutor.runTest(test);
        ((TestChromosome) c).setLastExecutionResult(result);
        c.setChanged(false);

        computePerformanceMetrics(c);

        /* check exceptions */
        if (result.hasTimeout() || result.hasTestException()){
            for (FitnessFunction<T> f : currentGoals)
                c.setFitness(f, Double.MAX_VALUE);
            return;
        }

        /* ------------------------------------- update of best values ----------------------------------- */
        for (FitnessFunction<T> fitnessFunction: this.uncoveredGoals) { // covered goals still not updated

            double value = fitnessFunction.getFitness(c);
            Double b = bestValues.get(fitnessFunction);
            if (b==null || b > value) {
                bestValues.put(fitnessFunction, value);
                hasBetterObjectives = true;
                logger.debug("Map Update! {} => {}", b, value);
            }
            // the map is removed if value == 0 below
        }
        /* ------------------------------------- update of best values ----------------------------------- */

        /* update the targets */
        Set<FitnessFunction<T>> visitedStatements = new HashSet<>(uncoveredGoals.size()*2);
        LinkedList<FitnessFunction<T>> targets = new LinkedList<>();
        targets.addAll(this.currentGoals);

        while (targets.size() > 0){
            FitnessFunction<T> fitnessFunction = targets.poll();

            int past_size = visitedStatements.size();
            visitedStatements.add(fitnessFunction);
            if (past_size == visitedStatements.size())
                continue;

            double value = fitnessFunction.getFitness(c);
            if (value == 0.0) {
                this.bestValues.remove(fitnessFunction);
                updateCoveredGoals(fitnessFunction, c);
                for (FitnessFunction<T> child : graph.getStructuralChildren(fitnessFunction))
                    targets.addLast(child);
            } else
                currentGoals.add(fitnessFunction);

        }
        currentGoals.removeAll(coveredGoals.keySet());


        /* update of the archives */
        for (Integer branchid : result.getTrace().getCoveredFalseBranches()){
            FitnessFunction<T> branch = this.branchCoverageFalseMap.get(branchid);
            if (branch == null)
                continue;
            updateCoveredGoals(branch, c);
        }
        for (Integer branchid : result.getTrace().getCoveredTrueBranches()){
            FitnessFunction<T> branch = this.branchCoverageTrueMap.get(branchid);
            if (branch == null)
                continue;
            updateCoveredGoals(branch, c);
        }
        for (String method : result.getTrace().getCoveredBranchlessMethods()){
            FitnessFunction<T> branch = this.branchlessMethodCoverageMap.get(method);
            if (branch == null)
                continue;
            updateCoveredGoals(branch, c);
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
        boolean toArchive = false;
        T best = coveredGoals.get(f);
        if (best == null){
            toArchive = true;
            coveredGoals.put(f, tc);
            uncoveredGoals.remove(f);
            currentGoals.remove(f);
        } else {
            boolean toUpdate = updater.isBetterSolution(best, tc);
            if (toUpdate) {
                toArchive = true;
                coveredGoals.put(f, tc);
                archive.get(best).remove(f);
                if (archive.get(best).size() == 0)
                    archive.remove(best);
            }
        }

        // update archive
        if (toArchive){
            List<FitnessFunction<T>> coveredTargets = archive.get(tc);
            if (coveredTargets == null){
                List<FitnessFunction<T>> list = new ArrayList<>();
                list.add(f);
                archive.put(tc, list);
            } else {
                coveredTargets.add(f);
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

    private void computePerformanceMetrics(T test) {
        for (AbstractIndicator indicator : this.indicators)
            indicator.getIndicatorValue(test);
    }
}
