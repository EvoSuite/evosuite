package org.evosuite.ga.metaheuristics;

import org.evosuite.ProgressMonitor;
import org.evosuite.ShutdownTestWriter;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.bloatcontrol.BloatControlFunction;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.ranking.FastNonDominatedSorting;
import org.evosuite.ga.operators.ranking.RankBasedPreferenceSorting;
import org.evosuite.ga.operators.ranking.RankingFunction;
import org.evosuite.ga.operators.selection.*;
import org.evosuite.ga.populationlimit.PopulationLimit;
import org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.RMIStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import org.evosuite.statistics.StatisticsListener;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ResourceController;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A wrapper class that facilitates the use of genetic algorithms operating on {@code
 * TestChromosome}s in such contexts where {@code TestSuiteChromosome}s are expected.
 *
 * @param <T> the type of adaptee genetic algorithm
 */
public abstract class TestSuiteAdapter<T extends GeneticAlgorithm<TestChromosome, TestFitnessFunction>> extends GeneticAlgorithm<TestSuiteChromosome,
        FitnessFunction<TestSuiteChromosome>> {

    private final T algorithm;

    /**
     * Constructs a new adapter with the given {@code algorithm} (evolving test chromosomes) as
     * adaptee and the specified factory for test suite chromosomes.
     *
     * @param algorithm the algorithm (operating on {@code TestChromosome}s) to adapt (must not be
     *                  {@code null})
     * @param factory factory for {@code TestSuiteChromosome}s
     */
    TestSuiteAdapter(final T algorithm, final ChromosomeFactory<TestSuiteChromosome> factory) {
        super(factory);
        this.algorithm = Objects.requireNonNull(algorithm);
    }

    /**
     * Returns the wrapped genetic algorithm.
     *
     * @return the wrapped genetic algorithm
     */
    T getAlgorithm() {
        return algorithm;
    }

    @Override
    public abstract TestSuiteChromosome getBestIndividual();

    @Override
    public abstract List<TestSuiteChromosome> getBestIndividuals();

    @Override
    protected void evolve() {
        throw new RuntimeException("not implemented");
        // algorithm.evolve();
    }

    @Override
    public void initializePopulation() {
        throw new RuntimeException("not implemented");
//        algorithm.initializePopulation();
    }

    @Override
    public void generateSolution() { // (10)
        algorithm.generateSolution();
    }

    @Override
    public int getAge() { // (11)
        return algorithm.getAge();
    }

    @Override
    protected void notifyMutation(TestSuiteChromosome chromosome) {
        throw new RuntimeException("not implemented");
//        super.notifyMutation(chromosome);
    }

//    protected void notifyMutation(TestChromosome chromosome) {
//        algorithm.notifyEvaluation(chromosome);
//    }

    @Override
    protected void notifyEvaluation(TestSuiteChromosome chromosome) {
        throw new RuntimeException("not implemented");
//        super.notifyEvaluation(chromosome);
    }

//    protected void notifyEvaluation(TestChromosome chromosome) {
//        algorithm.notifyEvaluation(chromosome);
//    }

    @Override
    protected boolean shouldApplyLocalSearch() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void disableFirstSecondaryCriterion() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void enableFirstSecondaryCriterion() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void updateSecondaryCriterion(int starvationCounter) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void applyLocalSearch() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void updateProbability(boolean improvement) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void generateInitialPopulation(int populationSize) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void starveToLimit(int limit) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void starveRandomly(int limit) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void starveByFitness(int limit) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void generateRandomPopulation(int populationSize) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void clearPopulation() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void addFitnessFunction(FitnessFunction<TestSuiteChromosome> function) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public FitnessFunction<TestSuiteChromosome> getFitnessFunction() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getNumberOfFitnessFunctions() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public SelectionFunction<TestSuiteChromosome> getSelectionFunction() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public RankingFunction<TestSuiteChromosome> getRankingFunction() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setBloatControl(BloatControlFunction bcf) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void addBloatControl(BloatControlFunction bloatControl) { // (8)
        algorithm.addBloatControl(bloatControl);
    }

    @Override
    public boolean isTooLong(Chromosome chromosome) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void calculateFitness() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void calculateFitness(TestSuiteChromosome c) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void calculateFitnessAndSortPopulation() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getPopulationSize() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected List<TestSuiteChromosome> elitism() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected List<Chromosome> randomism() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void updateFitnessFunctionsAndValues() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void writeIndividuals(List<TestSuiteChromosome> individuals) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setChromosomeFactory(ChromosomeFactory<TestSuiteChromosome> factory) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setCrossOverFunction(CrossOverFunction crossover) { // (7)
        algorithm.setCrossOverFunction(crossover);
    }

    @Override
    public void addListener(SearchListener<TestSuiteChromosome> listener) { // (1b)
        if (algorithm != null) {
            final SearchListener<TestChromosome> adapteeListener;
            if (listener instanceof StatisticsListener) {
                algorithm.addListener((SearchListener) listener);
                return;
            } else if (listener instanceof RelativeSuiteLengthBloatControl) {
                super.addListener(listener);
                adapteeListener = new RelativeSuiteLengthBloatControl<>();
            } else if (listener instanceof ResourceController) {
                adapteeListener = new ResourceController<>();
            } else if (listener instanceof ProgressMonitor) {
                adapteeListener = new ProgressMonitor<>();
            } else {
                throw new IllegalArgumentException("cannot adapt listener " + listener);
            }
            algorithm.addListener(adapteeListener);
        } else {
            // We're currently in the constructor of GeneticAlgorithm, so do nothing
        }
    }

    @Override
    public void removeListener(SearchListener listener) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void notifySearchStarted() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void notifySearchFinished() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void notifyIteration() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void sortPopulation() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<TestSuiteChromosome> getPopulation() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isNextPopulationFull(List<TestSuiteChromosome> nextGeneration) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setPopulationLimit(PopulationLimit limit) { // (6)
        algorithm.setPopulationLimit(limit);
    }

    @Override
    public boolean isFinished() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void addStoppingCondition(StoppingCondition<TestSuiteChromosome> condition) { // (1a)
        if (algorithm != null) {
            final StoppingCondition<TestChromosome> adapteeCondition;
            if (condition instanceof ZeroFitnessStoppingCondition) {
                adapteeCondition = new ZeroFitnessStoppingCondition<>();
            } else if (condition instanceof ShutdownTestWriter) {
                adapteeCondition = new ShutdownTestWriter<>();
            } else if (condition instanceof RMIStoppingCondition) {
                algorithm.addStoppingCondition((RMIStoppingCondition) condition);
                return;
            } else {
                throw new IllegalArgumentException("cannot adapt stopping condition " + condition);
            }
            algorithm.addStoppingCondition(adapteeCondition);
        } else {
            // We're currently in the constructor of GeneticAlgorithm, so do nothing
        }
    }

    @Override
    public Set<StoppingCondition> getStoppingConditions() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setStoppingCondition(StoppingCondition<TestSuiteChromosome> condition) { // (4)
        final StoppingCondition<TestChromosome> adapteeCondition;
        if (condition instanceof MaxTimeStoppingCondition) {
            adapteeCondition = new MaxTimeStoppingCondition<>();
        } else {
            throw new IllegalArgumentException("cannot adapt stopping condition " + condition);
        }
        algorithm.setStoppingCondition(adapteeCondition);
    }

    @Override
    public void removeStoppingCondition(StoppingCondition condition) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void resetStoppingConditions() { // (5)
        algorithm.resetStoppingConditions();
    }

    @Override
    public void setStoppingConditionLimit(int value) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected void updateBestIndividualFromArchive() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected boolean isBetterOrEqual(Chromosome chromosome1, Chromosome chromosome2) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void printBudget() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getBudgetString() {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected double progress() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setSelectionFunction(SelectionFunction<TestSuiteChromosome> function) { // (2)
        final SelectionFunction<TestChromosome> adapteeFunction;
        if (function instanceof FitnessProportionateSelection) {
            adapteeFunction = new FitnessProportionateSelection<>();
        } else if (function instanceof TournamentSelection) {
            adapteeFunction = new TournamentSelection<>();
        } else if (function instanceof BinaryTournamentSelectionCrowdedComparison) {
            adapteeFunction = new BinaryTournamentSelectionCrowdedComparison<>();
        } else if (function instanceof TournamentSelectionRankAndCrowdingDistanceComparator) {
            adapteeFunction = new BinaryTournamentSelectionCrowdedComparison<>();
        } else if (function instanceof BestKSelection) {
            adapteeFunction = new BestKSelection<>();
        } else if (function instanceof RandomKSelection) {
            adapteeFunction = new RandomKSelection<>();
        } else if (function instanceof RankSelection) {
            adapteeFunction = new RandomKSelection<>();
        } else {
            throw new IllegalArgumentException("cannot adapt selection function " + function);
        }
        algorithm.setSelectionFunction(adapteeFunction);
    }

    @Override
    public void setRankingFunction(RankingFunction<TestSuiteChromosome> function) { // (3)
        final RankingFunction<TestChromosome> adapteeFunction;
        if (function instanceof FastNonDominatedSorting) {
            adapteeFunction = new FastNonDominatedSorting<>();
        } else if (function instanceof RankBasedPreferenceSorting) {
            adapteeFunction = new RankBasedPreferenceSorting<>();
        } else {
            throw new IllegalArgumentException("cannot adapt ranking function " + function);
        }
        algorithm.setRankingFunction(adapteeFunction);
    }

    @Override // (12)
    public List getFitnessFunctions() { // FIXME avoid horrible raw return type!!!
        // This method returns a raw List of fitness functions. This is ugly but (at the time of
        // this writing) nothing bad actually happens because MOSuiteStrategy only invokes size()
        // on the returned list.
        return algorithm.getFitnessFunctions();
    }

    @Override // (9)
    public void addFitnessFunctions(List functions) { // FIXME avoid horrible raw type!!!
        // The following code still circumvents the type system by using unsafe raw types and
        // unchecked casts. The code only works if a certain assumption holds:
        // MOSuiteStrategy will only ever pass a list of TestFitnessFunctions to this method.
        // In all other cases, this code will blow up. This issue should be fixed as soon as
        // possible, e.g., by creating an adapter class for TestFitnessFunctions to dress up as
        // TestSuiteFitnessFunctions.
        List<TestFitnessFunction> fs = (List<TestFitnessFunction>) functions;

        algorithm.addFitnessFunctions(fs);
    }
}
