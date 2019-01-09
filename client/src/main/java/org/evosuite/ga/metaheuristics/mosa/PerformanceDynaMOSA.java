package org.evosuite.ga.metaheuristics.mosa;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.mosa.structural.adaptive.AdaptiveBranchesManager;
import org.evosuite.ga.operators.ranking.CrowdingDistance;
import org.evosuite.performance.PerformanceScore;
import org.evosuite.performance.strategies.PerformanceStrategy;
import org.evosuite.performance.strategies.PerformanceStrategyFactory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Performance version of DynaMOSA, according to the same implementation we already implemented for PerformanceMOSA
 * @author Giovanni Grano
 */
@SuppressWarnings("Duplicates")
public class PerformanceDynaMOSA<T extends Chromosome> extends DynaMOSA<T> {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceDynaMOSA.class);

    /** Manager to determine the test goals to consider at each generation */
    protected AdaptiveBranchesManager<T> goalsManager = null;

    protected CrowdingDistance<T> distance = new CrowdingDistance<T>();

    /* -------------------------------------- performance instance variables -------------------------------------- */
    private PerformanceStrategy<T> strategy;

    private PerformanceScore<T> score = new PerformanceScore();

    private enum Heuristics {CROWDING, PERFORMANCE}

    private Heuristics last_heuristic;

    private int crowdingStagnation=0, performanceStagnation=0;

    /* -------------------------------------- performance instance variables -------------------------------------- */


    /**
     * Constructor based on the abstract class {@link AbstractMOSA}.
     *
     * @param factory
     */
    public PerformanceDynaMOSA(ChromosomeFactory<T> factory) {
        super(factory);

        /* --------------------------------- instantiate performance variables --------------------------------- */
        strategy = PerformanceStrategyFactory.getPerformanceStrategy();

        LoggingUtils.getEvoLogger().info("* Running Performance DynaMOSA with indicator {}",
                indicators.stream().map(i -> i.toString()).collect(Collectors.joining(",")));
        LoggingUtils.getEvoLogger().info("* Combination Strategy for indicators = {}", Properties.P_COMBINATION_STRATEGY.toString());

        /* --------------------------------- instantiate performance variables --------------------------------- */
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("Duplicates")
    protected void evolve() {
        List<T> offspringPopulation = this.breedNextGeneration();

        /* --------------------------------- select heuristic --------------------------------- */
        logger.debug("Last Heuristic = {}", last_heuristic);
        if (Properties.P_STRATEGY == Properties.PerformanceMOSAStrategy.CROWDING_DISTANCE) {
            last_heuristic = checkStagnation();
        } else {
            last_heuristic = Heuristics.CROWDING;
        }
        logger.debug("Current Heuristic = {}", last_heuristic);
        /* --------------------------------- select heuristic --------------------------------- */

        // Create the union of parents and offSpring
        List<T> union = new ArrayList<>(this.population.size() + offspringPopulation.size());
        union.addAll(this.population);
        union.addAll(offspringPopulation);

        score.assignPerformanceScore(union);

        // Ranking the union
        logger.debug("Union Size = {}", union.size());

        // Ranking the union using the best rank algorithm (modified version of the non dominated sorting algorithm)
        ranking.computeRankingAssignment(union, this.goalsManager.getCurrentGoals());

        // let's form the next population using "preference sorting and non-dominated sorting" on the
        // updated set of goals
        int remain = Math.max(Properties.POPULATION, this.ranking.getSubfront(0).size());
        int index = 0;
        List<T> front;
        this.population.clear();

        // Obtain the next front
        front = ranking.getSubfront(index);

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
                front = ranking.getSubfront(index);

        }

        if (remain > 0 && !front.isEmpty()) {

            applySecondaryCriterion(front);

            logger.debug("Distance = {}, Score ={} ", front.get(0).getDistance(), front.get(0).getPerformanceScore());

            strategy.sort(front);

            for (int k = 0; k < remain; k++)
                this.population.add(front.get(k));

        }

        this.currentIteration++;
        //logger.debug("N. fronts = {}", ranking.getNumberOfSubfronts());
        //logger.debug("1* front size = {}", ranking.getSubfront(0).size());
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

        this.goalsManager = new AdaptiveBranchesManager<>(this.fitnessFunctions);
        this.goalsManager.setIndicators(this.indicators);

        LoggingUtils.getEvoLogger().info("* Initial Number of Goals in PerformanceDynaMOSA = " +
                this.goalsManager.getCurrentGoals().size() +" / "+ this.getUncoveredGoals().size());

        logger.debug("Initial Number of Goals = " + this.goalsManager.getCurrentGoals().size());

        //initialize population
        if (this.population.isEmpty()) {
            this.initializePopulation();
        }

        // update current goals
        this.calculateFitness();

        // Calculate dominance ranks and crowding distance
        this.ranking.computeRankingAssignment(this.population, this.goalsManager.getCurrentGoals());

        for (int i = 0; i < this.ranking.getNumberOfSubfronts(); i++){
            distance.fastEpsilonDominanceAssignment(ranking.getSubfront(i), goalsManager.getCurrentGoals());
        }

        // update best values
        for (T t : population)
            for (FitnessFunction<T> f : goalsManager.getUncoveredGoals())
                goalsManager.updateBestValue(f, t.getFitness(f));
        last_heuristic = Heuristics.PERFORMANCE;

        // next generations
        while (!isFinished() && this.goalsManager.getUncoveredGoals().size() > 0) {
            this.evolve();
            this.notifyIteration();
        }

        /* -------------------------- calculate the performance indicators to save them ---------------------------*/
        Set<T> archive = goalsManager.getArchive();
        computePerformanceMetrics(archive);
        printPerformanceMetrics(archive);

        this.notifySearchFinished();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<FitnessFunction<T>> getCoveredGoals() {
        return this.goalsManager.getCoveredGoals().keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNumberOfCoveredGoals() {
        return this.getCoveredGoals().size();
    }

    /**
     * {@inheritDoc}
     */
    protected Set<FitnessFunction<T>> getUncoveredGoals() {
        return this.goalsManager.getUncoveredGoals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNumberOfUncoveredGoals() {
        return this.getUncoveredGoals().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getTotalNumberOfGoals() {
        return this.getNumberOfCoveredGoals() + this.getNumberOfUncoveredGoals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<T> getSolutions() {
        List<T> suite = new ArrayList<>(this.goalsManager.getArchive());
        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TestSuiteChromosome generateSuite() {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        for (T t : this.getSolutions()) {
            TestChromosome test = (TestChromosome) t;
            suite.addTest(test);
        }
        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void calculateFitness(T c) {
        this.goalsManager.calculateFitness(c);
        /* --------------------------------- calculate performance indicators --------------------------------- */
        computePerformanceMetrics(new HashSet<>(Arrays.asList((T)c)));
        /* --------------------------------- calculate performance indicators --------------------------------- */
        this.notifyEvaluation(c);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> getBestIndividuals() {
        TestSuiteChromosome bestTestCases = this.generateSuite();

        if (bestTestCases.getTestChromosomes().isEmpty()) {
            // trivial case where there are no branches to cover or the archive is empty
            for (T test : this.population) {
                bestTestCases.addTest((TestChromosome) test);
            }
        }

        // compute overall fitness and coverage
        this.computeCoverageAndFitness(bestTestCases);

        List<T> bests = new ArrayList<T>(1);
        bests.add((T) bestTestCases);

        return bests;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T getBestIndividual() {
        TestSuiteChromosome best = this.generateSuite();
        if (best.getTestChromosomes().isEmpty()) {
            for (T test : this.population) {
                best.addTest((TestChromosome) test);
            }
            for (TestSuiteFitnessFunction suiteFitness : this.suiteFitnessFunctions.keySet()) {
                best.setCoverage(suiteFitness, 0.0);
                best.setFitness(suiteFitness,  1.0);
            }
            return (T) best;
        }

        // compute overall fitness and coverage
        this.computeCoverageAndFitness(best);
        return (T) best;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void computeCoverageAndFitness(TestSuiteChromosome suite) {

        for (Map.Entry<TestSuiteFitnessFunction, Class<?>> entry : this.suiteFitnessFunctions.entrySet()) {
            TestSuiteFitnessFunction suiteFitnessFunction = entry.getKey();
            Class<?> testFitnessFunction = entry.getValue();

            int numberCoveredTargets = this.goalsManager.getNumberOfCoveredTargets(testFitnessFunction);
            int numberUncoveredTargets = this.goalsManager.getNumberOfUncoveredTargets(testFitnessFunction);
            int totalNumberTargets = numberCoveredTargets + numberUncoveredTargets;

            double coverage = totalNumberTargets == 0 ? 0.0
                    : ((double) numberCoveredTargets)
                    / ((double) (numberCoveredTargets + numberUncoveredTargets));

            suite.setFitness(suiteFitnessFunction, ((double) numberUncoveredTargets));
            suite.setCoverage(suiteFitnessFunction, coverage);
            suite.setNumOfCoveredGoals(suiteFitnessFunction, numberCoveredTargets);
            suite.setNumOfNotCoveredGoals(suiteFitnessFunction, numberUncoveredTargets);
        }
    }

    /**
     * Checks for the stagnation and eventually changes the heuristic
     * @return
     */
    private Heuristics checkStagnation() {

        if (!goalsManager.hasBetterObjectives()){
            goalsManager.setHasBetterObjectives(false);
            if (last_heuristic == Heuristics.CROWDING)
                crowdingStagnation++;
            else
                performanceStagnation++;

            if (performanceStagnation > crowdingStagnation)
                return Heuristics.CROWDING;
            else
                return Heuristics.PERFORMANCE;
        } else {
            goalsManager.setHasBetterObjectives(false);

            if (last_heuristic == Heuristics.CROWDING)
                crowdingStagnation=0;
            else
                performanceStagnation=0;

            return last_heuristic;
        }
    }

    protected void applySecondaryCriterion(List<T> front){
        if (last_heuristic == Heuristics.CROWDING)
            distance.fastEpsilonDominanceAssignment(front, goalsManager.getUncoveredGoals());
        else if (last_heuristic == Heuristics.PERFORMANCE) {
            score.assignPerformanceScore(front);
            strategy.setDistances(front, goalsManager.getUncoveredGoals());
        } else {
            for (T t : front)
                t.setDistance(0);
        }
    }
}
