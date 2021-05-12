package org.evosuite.ga.metaheuristics.mosa;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.mosa.structural.adaptive.AdaptiveGoalManager;
import org.evosuite.ga.operators.ranking.CrowdingDistance;
import org.evosuite.performance.AbstractIndicator;
import org.evosuite.performance.indicator.IndicatorsFactory;
import org.evosuite.performance.strategies.PerformanceStrategy;
import org.evosuite.performance.strategies.PerformanceStrategyFactory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of aDynaMOSA (Adaptive DynaMOSA) described in the paper "Testing with Fewer Resources: An Adaptive
 * Approach to Performance-Aware Test Case Generation".
 */
public class AdaptiveDynaMOSA extends DynaMOSA {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveDynaMOSA.class);

    protected CrowdingDistance<TestChromosome> distance = new CrowdingDistance<>();

    /*
     * Instance variables needed to handle the adaptive approach
     */
    private final PerformanceStrategy strategy;

    private final List<AbstractIndicator> indicators;

    private enum Heuristics {CROWDING, PERFORMANCE}

    private Heuristics last_heuristic;

    private int crowdingStagnation = 0, performanceStagnation = 0;

    /**
     * Constructor based on the abstract class {@link AbstractMOSA}.
     */
    public AdaptiveDynaMOSA(ChromosomeFactory<TestChromosome> factory) {
        super(factory);

        strategy = PerformanceStrategyFactory.getPerformanceStrategy();
        indicators = IndicatorsFactory.getPerformanceIndicator();

        LoggingUtils.getEvoLogger().info("* Running Performance DynaMOSA with indicator {}",
                indicators.stream().map(AbstractIndicator::toString).collect(Collectors.joining(",")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void evolve() {
        List<TestChromosome> offspringPopulation = this.breedNextGeneration();

        for (int i = offspringPopulation.size() - 1; i >= 0; i--) {
            TestChromosome off = offspringPopulation.get(i);
            if (off.getPerformanceScore() == Double.MAX_VALUE) {
                offspringPopulation.remove(i);
            }
        }

        /* select the heuristic */
        //logger.error("Last Heuristic = {}", last_heuristic);
        last_heuristic = checkStagnation();
        //logger.error("Current Heuristic = {}", last_heuristic);

        // Create the union of parents and offSpring
        List<TestChromosome> union = new ArrayList<>(this.population.size() + offspringPopulation.size());
        union.addAll(this.population);
        union.addAll(offspringPopulation);

        // Ranking the union
        logger.debug("Union Size = {}", union.size());

        // Ranking the union using the best rank algorithm (modified version of the non dominated sorting algorithm)
        this.rankingFunction.computeRankingAssignment(union, this.goalsManager.getCurrentGoals());

        // let's form the next population using "preference sorting and non-dominated sorting" on the
        // updated set of goals
        int remain = Math.max(Properties.POPULATION, this.rankingFunction.getSubfront(0).size());
        int index = 0;
        List<TestChromosome> front;
        this.population.clear();

        // Obtain the next front
        front = this.rankingFunction.getSubfront(index);

        while ((remain > 0) && (remain >= front.size()) && !front.isEmpty()) {

            applySecondaryCriterion(front);

            logger.debug("Distance = {}, Score = {} ", front.get(0).getDistance(), front.get(0).getPerformanceScore());

            // Add the individuals of this front
            this.population.addAll(front);

            // Decrement remain
            remain = remain - front.size();

            // Obtain the next front
            index++;
            if (remain > 0)
                front = this.rankingFunction.getSubfront(index);

        }

        if (remain > 0 && !front.isEmpty()) {

            applySecondaryCriterion(front);

            logger.debug("Distance = {}, Score ={} ", front.get(0).getDistance(), front.get(0).getPerformanceScore());
            strategy.sort(front);

            for (int k = 0; k < remain; k++)
                this.population.add(front.get(k));

        }

        this.currentIteration++;
//        logger.debug("N. fronts = {}", this.rankingFunction.getNumberOfSubfronts());
//        logger.debug("1* front size = {}", this.rankingFunction.getSubfront(0).size());
//        logger.debug("Best Values Map size = {}", goalsManager.getBestValues().size());
        logger.debug("Covered goals = {}", goalsManager.getCoveredGoals().size());
        logger.debug("Current goals = {}", goalsManager.getCurrentGoals().size());
        logger.debug("Uncovered goals = {}", goalsManager.getUncoveredGoals().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateSolution() {
        logger.debug("executing generateSolution function");

        this.goalsManager = new AdaptiveGoalManager(this.fitnessFunctions);
        ((AdaptiveGoalManager)this.goalsManager).setIndicators(this.indicators);

        LoggingUtils.getEvoLogger().info("* Initial Number of Goals in PerformanceDynaMOSA = " +
                this.goalsManager.getCurrentGoals().size() + " / " + this.getUncoveredGoals().size());

        logger.debug("Initial Number of Goals = " + this.goalsManager.getCurrentGoals().size());

        //initialize population
        if (this.population.isEmpty()) {
            this.initializePopulation();
        }

        // update current goals
        this.calculateFitness();

        // Calculate dominance ranks and crowding distance
        this.rankingFunction.computeRankingAssignment(this.population, this.goalsManager.getCurrentGoals());

        for (int i = 0; i < this.rankingFunction.getNumberOfSubfronts(); i++) {
            distance.fastEpsilonDominanceAssignment(this.rankingFunction.getSubfront(i), goalsManager.getCurrentGoals());
        }

        // update best values
        for (TestChromosome t : population)
            for (TestFitnessFunction f : goalsManager.getUncoveredGoals())
                ((AdaptiveGoalManager)this.goalsManager).updateBestValue(f, t.getFitness(f));
        last_heuristic = Heuristics.PERFORMANCE;

        // next generations
        while (!isFinished() && this.goalsManager.getUncoveredGoals().size() > 0) {
            this.evolve();
            this.notifyIteration();
        }

        /* calculate the performance indicators of the archive*/
        Set<TestChromosome> archive = ((AdaptiveGoalManager)this.goalsManager).getArchive();
        computePerformanceMetrics(archive);

        this.notifySearchFinished();
    }

    /*
     * Checks for the stagnation and eventually changes the heuristic
     */
    private Heuristics checkStagnation() {
        Heuristics choice;

        if (!((AdaptiveGoalManager)this.goalsManager).hasBetterObjectives()) {
            ((AdaptiveGoalManager)this.goalsManager).setHasBetterObjectives(false);
            if (last_heuristic == Heuristics.CROWDING)
                crowdingStagnation++;
            else
                performanceStagnation++;

            if (performanceStagnation > crowdingStagnation + 1)
                choice = Heuristics.CROWDING;
            else if (performanceStagnation <= crowdingStagnation)
                choice = Heuristics.PERFORMANCE;
            else
                choice = this.last_heuristic;
        } else {
            ((AdaptiveGoalManager)this.goalsManager).setHasBetterObjectives(false);

            if (last_heuristic == Heuristics.CROWDING)
                crowdingStagnation = 0;
            else
                performanceStagnation = 0;

            choice = last_heuristic;
        }

//        logger.error("crowdingStagnation = {}", crowdingStagnation);
//        logger.error("performanceStagnation = {}", performanceStagnation);
        LoggingUtils.getEvoLogger().info("returning " + choice);
        return choice;
    }

    /**
     * Computes the indicators used for the adaptive approach
     */
    protected void computePerformanceMetrics(Set<TestChromosome> tests) {
        tests.forEach(t -> indicators.forEach(i -> i.getIndicatorValue(t)));
    }

    @Override
    protected void clearCachedResults(TestChromosome chromosome) {
        super.clearCachedResults(chromosome);
        // resets the values for the performance indicators
        chromosome.getIndicatorValues().clear();
        chromosome.setPerformanceScore(Double.MAX_VALUE);
    }

    /**
     * Application of the secondary criterion depending on the heuristic to use for this generation
     */
    protected void applySecondaryCriterion(List<TestChromosome> front) {
        if (last_heuristic == Heuristics.CROWDING) {
            distance.fastEpsilonDominanceAssignment(front, goalsManager.getCurrentGoals());
        } else if (last_heuristic == Heuristics.PERFORMANCE) {
            for (TestChromosome t : front)
                ((AdaptiveGoalManager)this.goalsManager).computePerformanceMetrics(t);
            strategy.setDistances(front);
        } else {
            for (TestChromosome t : front)
                t.setDistance(0);
        }
    }
}
