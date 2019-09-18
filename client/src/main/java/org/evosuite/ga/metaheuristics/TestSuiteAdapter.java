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
import org.evosuite.ga.stoppingconditions.*;
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
public abstract class TestSuiteAdapter<T extends GeneticAlgorithm<TestChromosome,
        TestFitnessFunction>> extends GeneticAlgorithm<TestSuiteChromosome,
        FitnessFunction<TestSuiteChromosome>> {

    private final T algorithm;

    /**
     * Constructs a new adapter with the given non-null {@code algorithm} (evolving
     * {@code TestChromosomes}) as adaptee.
     *
     * @param algorithm the algorithm (operating on {@code TestChromosome}s) to adapt
     */
    protected TestSuiteAdapter(final T algorithm) {
        super(null);
        this.algorithm = Objects.requireNonNull(algorithm);
        clear();
    }

    /**
     * This method clears all fields of this wrapper class by setting them to {@code null}.
     * <p>
     * This class may seem harmless and unsuspecting, but it's really not! It can produce obscure,
     * plain stupid and hard to chase down bugs. The intent of this method is to deliberately
     * produce {@code NullPointerException}s should we have forgotten to properly forward some
     * methods to the adaptee algorithm. If a method is invoked that tries to access some of the
     * fields of this wrapper class (e.g., the non-existing population) the method will fail
     * horribly during runtime, which hopefully makes the entire program crash (or at least
     * produces some nasty output on the console). This way, we know that something must be wrong
     * with the implementation of this wrapper class. This is a much better scenario than a
     * non-crashing but at the same time incorrectly operating program that contains mentioned bugs.
     */
    private void clear() {
        this.population = null;
        this.fitnessFunctions = null;
        this.selectionFunction = null;
        this.crossoverFunction = null;
        this.chromosomeFactory = null;
        this.bloatControl = null;
        this.localObjective = null;
        this.populationLimit = null;
        this.rankingFunction = null;
        this.currentIteration = Integer.MIN_VALUE;
        this.localSearchProbability = Double.NaN;
        // The following fields are NOT cleared. They are actually useful because MOSA also uses
        // some listeners that operate on test suite chromosomes. The adapter now manages those
        // listeners for the wrapped algorithms.
        // this.stoppingConditions = null;
        // this.listeners = null;
    }

    /**
     * Returns the wrapped genetic algorithm.
     *
     * @return the wrapped genetic algorithm
     */
    protected T getAlgorithm() {
        return algorithm;
    }

    @Override
    public abstract TestSuiteChromosome getBestIndividual();

    @Override
    public abstract List<TestSuiteChromosome> getBestIndividuals();

    @Override
    final protected void evolve() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void initializePopulation() {
        throw new UnsupportedOperationException("not implemented");
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
    final protected void notifyMutation(TestSuiteChromosome chromosome) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void notifyEvaluation(TestSuiteChromosome chromosome) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected boolean shouldApplyLocalSearch() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void disableFirstSecondaryCriterion() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void enableFirstSecondaryCriterion() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void updateSecondaryCriterion(int starvationCounter) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void applyLocalSearch() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void updateProbability(boolean improvement) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void generateInitialPopulation(int populationSize) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void starveToLimit(int limit) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void starveRandomly(int limit) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void starveByFitness(int limit) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void generateRandomPopulation(int populationSize) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void clearPopulation() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void addFitnessFunction(FitnessFunction<TestSuiteChromosome> function) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public FitnessFunction<TestSuiteChromosome> getFitnessFunction() {
        return new TestSuiteFitnessFunctionWrapper(algorithm.getFitnessFunction());
    }

    @Override
    final public int getNumberOfFitnessFunctions() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public SelectionFunction<TestSuiteChromosome> getSelectionFunction() {
        throw new UnsupportedOperationException("not implemented");
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
            adapteeFunction = new RankSelection<>();
        } else {
            throw new IllegalArgumentException("cannot adapt selection function " + function);
        }
        adapteeFunction.setMaximize(function.isMaximize());
        algorithm.setSelectionFunction(adapteeFunction);
    }

    @Override
    final public RankingFunction<TestSuiteChromosome> getRankingFunction() {
        throw new UnsupportedOperationException("not implemented");
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

    @Override
    final public void setBloatControl(BloatControlFunction bcf) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void addBloatControl(BloatControlFunction bloatControl) { // (8)
        algorithm.addBloatControl(bloatControl);
    }

    @Override
    final public boolean isTooLong(Chromosome chromosome) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void calculateFitness() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void calculateFitness(TestSuiteChromosome c) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void calculateFitnessAndSortPopulation() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public int getPopulationSize() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected List<TestSuiteChromosome> elitism() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected List<Chromosome> randomism() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void updateFitnessFunctionsAndValues() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void writeIndividuals(List<TestSuiteChromosome> individuals) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void setChromosomeFactory(ChromosomeFactory<TestSuiteChromosome> factory) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setCrossOverFunction(CrossOverFunction crossover) { // (7)
        algorithm.setCrossOverFunction(crossover);
    }

    @Override
    public void addListener(SearchListener<TestSuiteChromosome> listener) { // (1b)
        if (algorithm != null) {
            if (listener instanceof StatisticsListener) {
                super.addListener(listener);
            } else if (listener instanceof RelativeSuiteLengthBloatControl) {
                super.addListener(listener);
            } else if (listener instanceof ResourceController) {
                algorithm.addListener(new ResourceController<>());
            } else if (listener instanceof ProgressMonitor) {
                super.addListener(listener);
            } else if (listener instanceof ZeroFitnessStoppingCondition) {
                super.addListener(listener);
            } else {
                throw new IllegalArgumentException("cannot adapt listener " + listener);
            }
        } else {
            // When we hit this branch, this TestSuiteAdapter object is currently being
            // constructed, and this method was invoked by the constructor of the super class
            // (i.e., GeneticAlgorithm). We simply do nothing.
        }
    }

    @Override
    final public void removeListener(SearchListener listener) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void notifySearchStarted() { // called by the adaptee
        super.notifySearchStarted();
    }

    @Override
    final public void notifySearchFinished() { // called by the adaptee
        super.notifySearchFinished();
    }

    @Override
    final public void notifyIteration() { // called by the adaptee
        super.notifyIteration();
    }

    @Override
    final protected void sortPopulation() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public List<TestSuiteChromosome> getPopulation() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public boolean isNextPopulationFull(List<TestSuiteChromosome> nextGeneration) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setPopulationLimit(PopulationLimit limit) { // (6)
        algorithm.setPopulationLimit(limit);
    }

    @Override
    final public boolean isFinished() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void addStoppingCondition(StoppingCondition<TestSuiteChromosome> condition) { // (1a)
        if (algorithm != null) {
            final StoppingCondition<TestChromosome> adapteeCondition;
            if (condition instanceof ZeroFitnessStoppingCondition) {
                super.addStoppingCondition(condition);
                return;
            } else if (condition instanceof ShutdownTestWriter) {
                adapteeCondition = new ShutdownTestWriter<>();
            } else if (condition instanceof RMIStoppingCondition) {
                algorithm.addStoppingCondition((RMIStoppingCondition) condition);
                return;
            } else if (condition instanceof GlobalTimeStoppingCondition) {
                adapteeCondition = new GlobalTimeStoppingCondition<>();
            } else {
                throw new IllegalArgumentException("cannot adapt stopping condition " + condition);
            }
            algorithm.addStoppingCondition(adapteeCondition);
        } else {
            // When we hit this branch, this TestSuiteAdapter object is currently being
            // constructed, and this method was invoked by the constructor of the super class
            // (i.e., GeneticAlgorithm). We simply do nothing.
        }
    }

    @Override
    final public Set<StoppingCondition> getStoppingConditions() {
        return algorithm.getStoppingConditions();
    }

    @Override
    public void setStoppingCondition(StoppingCondition<TestSuiteChromosome> condition) { // (4)
        final StoppingCondition<TestChromosome> adapteeCondition;
        if (condition instanceof MaxTimeStoppingCondition) {
            adapteeCondition = new MaxTimeStoppingCondition<>();
        } else if (condition instanceof MaxGenerationStoppingCondition) {
            adapteeCondition = new MaxGenerationStoppingCondition<>();
        } else {
            throw new IllegalArgumentException("cannot adapt stopping condition " + condition);
        }
        algorithm.setStoppingCondition(adapteeCondition);
    }

    @Override
    final public void removeStoppingCondition(StoppingCondition condition) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void resetStoppingConditions() { // (5)
        algorithm.resetStoppingConditions();
    }

    @Override
    final public void setStoppingConditionLimit(int value) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void updateBestIndividualFromArchive() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected boolean isBetterOrEqual(Chromosome chromosome1, Chromosome chromosome2) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void printBudget() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public String getBudgetString() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected double progress() {
        throw new UnsupportedOperationException("not implemented");
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

    @Override
    public String toString() {
        if (algorithm == null) {
            return "TestSuiteAdapter";
        }

        return algorithm.toString(); // avoids NPE for debuggers automatically invoking toString()
    }

    /**
     * A wrapper class for fitness functions of test chromosomes to make them (sort of) work
     * in environments where fitness functions of test suite chromosomes are needed. This is mainly
     * useful for StatisticsListener. In its searchStarted() method, it only checks if we have
     * minimization or maximization functions. It does nothing else with those fitness functions.
     * So, recording this information in a wrapper and returning it properly to StatisticsListener
     * is fine.
     */
    private static class TestSuiteFitnessFunctionWrapper extends FitnessFunction<TestSuiteChromosome> {
        private final boolean maximizationFunction;

        TestSuiteFitnessFunctionWrapper(TestFitnessFunction fitnessFunction) {
            super();
            maximizationFunction = fitnessFunction.isMaximizationFunction();
        }

        @Override
        public double getFitness(TestSuiteChromosome individual) {
            throw new UnsupportedOperationException("cannot apply wrapped TestFitnessFunction to " +
                    "TestSuiteChromosome");
        }

        @Override
        public boolean isMaximizationFunction() {
            return maximizationFunction;
        }
    }
}
