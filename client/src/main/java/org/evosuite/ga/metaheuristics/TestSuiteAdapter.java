package org.evosuite.ga.metaheuristics;

import org.evosuite.ProgressMonitor;
import org.evosuite.ShutdownTestWriter;
import org.evosuite.ga.*;
import org.evosuite.ga.bloatcontrol.BloatControlFunction;
import org.evosuite.ga.operators.crossover.*;
import org.evosuite.ga.operators.ranking.FastNonDominatedSorting;
import org.evosuite.ga.operators.ranking.RankBasedPreferenceSorting;
import org.evosuite.ga.operators.ranking.RankingFunction;
import org.evosuite.ga.operators.selection.*;
import org.evosuite.ga.populationlimit.IndividualPopulationLimit;
import org.evosuite.ga.populationlimit.PopulationLimit;
import org.evosuite.ga.populationlimit.SizePopulationLimit;
import org.evosuite.ga.stoppingconditions.*;
import org.evosuite.statistics.StatisticsListener;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import org.evosuite.testsuite.StatementsPopulationLimit;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ResourceController;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * A wrapper class that facilitates the use of genetic algorithms operating on {@code
 * TestChromosome}s in such contexts where {@code TestSuiteChromosome}s are expected.
 *
 * @param <A> the type of adaptee genetic algorithm
 */
public abstract class TestSuiteAdapter<A extends GeneticAlgorithm<TestChromosome>>
        extends GeneticAlgorithm<TestSuiteChromosome> {

    private final A algorithm;

    /**
     * Constructs a new adapter with the given non-null {@code algorithm} (evolving
     * {@code TestChromosomes}) as adaptee.
     *
     * @param algorithm the algorithm (operating on {@code TestChromosome}s) to adapt
     */
    protected TestSuiteAdapter(final A algorithm) {
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
    protected A getAlgorithm() {
        return algorithm;
    }

    @Override
    public abstract TestSuiteChromosome getBestIndividual();

    @Override
    public abstract List<TestSuiteChromosome> getBestIndividuals();

    @Override
    final protected void evolve() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void initializePopulation() throws UnsupportedOperationException {
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
    final protected void notifyMutation(TestSuiteChromosome chromosome)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void notifyEvaluation(TestSuiteChromosome chromosome)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected boolean shouldApplyLocalSearch()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void disableFirstSecondaryCriterion() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void enableFirstSecondaryCriterion() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void updateSecondaryCriterion(int starvationCounter)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void applyLocalSearch() throws UnsupportedOperationException {
        // throw new UnsupportedOperationException("not implemented");
        super.applyLocalSearch();
    }

    @Override
    final protected void updateProbability(boolean improvement)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void generateInitialPopulation(int populationSize)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void starveToLimit(int limit) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void starveRandomly(int limit) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void starveByFitness(int limit) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void generateRandomPopulation(int populationSize)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void clearPopulation() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void addFitnessFunction(final FitnessFunction< TestSuiteChromosome> function)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public FitnessFunction<TestSuiteChromosome> getFitnessFunction() {
        return new TestSuiteFitnessFunctionWrapper(algorithm.getFitnessFunction());
    }

    @Override
    final public int getNumberOfFitnessFunctions() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public SelectionFunction<TestSuiteChromosome> getSelectionFunction()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setSelectionFunction(SelectionFunction<TestSuiteChromosome> function)
            throws IllegalArgumentException { // (2)
        final SelectionFunction<TestChromosome> adapteeFunction;
        if (function instanceof FitnessProportionateSelection) {
            adapteeFunction = new FitnessProportionateSelection<>();
        } else if (function instanceof TournamentSelection) {
            adapteeFunction = new TournamentSelection<>();
        } else if (function instanceof BinaryTournamentSelectionCrowdedComparison) {
            adapteeFunction = new BinaryTournamentSelectionCrowdedComparison<>();
        } else if (function instanceof TournamentSelectionRankAndCrowdingDistanceComparator) {
            adapteeFunction = new TournamentSelectionRankAndCrowdingDistanceComparator<>();
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
    final public RankingFunction<TestSuiteChromosome> getRankingFunction()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setRankingFunction(RankingFunction<TestSuiteChromosome> function)
            throws IllegalArgumentException { // (3)
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
    final public void setBloatControl(BloatControlFunction<TestSuiteChromosome> bcf)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void addBloatControl(BloatControlFunction<TestSuiteChromosome> bloatControl)
            throws UnsupportedOperationException { // (8)
        throw new UnsupportedOperationException("unimplemented during refactoring");
        // algorithm.addBloatControl(bloatControl);
    }

    @Override
    final public boolean isTooLong(TestSuiteChromosome chromosome)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void calculateFitness() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void calculateFitness(TestSuiteChromosome c)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void calculateFitnessAndSortPopulation() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public int getPopulationSize() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected List<TestSuiteChromosome> elitism() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected List<TestSuiteChromosome> randomism() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void updateFitnessFunctionsAndValues() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void writeIndividuals(List<TestSuiteChromosome> individuals)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    final public void setChromosomeFactory(ChromosomeFactory<TestSuiteChromosome> factory)
            throws IllegalArgumentException {
        if (algorithm != null) {
            if (factory instanceof TestSuiteChromosomeFactoryMock) {
                TestSuiteChromosomeFactoryMock tcfw = (TestSuiteChromosomeFactoryMock) factory;
                algorithm.setChromosomeFactory(tcfw.getWrapped());
            } else {
                throw new IllegalArgumentException("factory not supported: " + factory);
            }
        } else  {
            // When we hit this branch, this TestSuiteAdapter object is currently being
            // constructed, and this method was invoked by the constructor of the super class
            // (i.e., GeneticAlgorithm). We simply do nothing.
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void setCrossOverFunction(CrossOverFunction<TestSuiteChromosome> crossover)
            throws IllegalArgumentException { // (7)
        if (algorithm != null) {
            if (crossover instanceof SinglePointRelativeCrossOver) {
                algorithm.setCrossOverFunction(new SinglePointRelativeCrossOver<>());
            } else if (crossover instanceof CoverageCrossOver) {
                // CoverageCrossOver is defined on TestSuiteChromosome, thus cannot adapt to
                // TestChromosomes.
                throw new IllegalArgumentException("CoverageCrossOver not supported");
            } else if (crossover instanceof UniformCrossOver) {
                algorithm.setCrossOverFunction(new UniformCrossOver<>());
            } else if (crossover instanceof SinglePointFixedCrossOver) {
                algorithm.setCrossOverFunction(new SinglePointFixedCrossOver<>());
            } else if (crossover instanceof SinglePointCrossOver) {
                algorithm.setCrossOverFunction(new SinglePointCrossOver<>());
            } else {
                throw new IllegalArgumentException("cannot adapt crossover " + crossover);
            }
        } else  {
            // When we hit this branch, this TestSuiteAdapter object is currently being
            // constructed, and this method was invoked by the constructor of the super class
            // (i.e., GeneticAlgorithm). We simply do nothing.
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void addListener(SearchListener<TestSuiteChromosome> listener)
            throws IllegalArgumentException { // (1b)
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
    final public void removeListener(SearchListener<TestSuiteChromosome> listener)
            throws UnsupportedOperationException {
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
    final protected void sortPopulation() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public List<TestSuiteChromosome> getPopulation() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public boolean isNextPopulationFull(List<TestSuiteChromosome> nextGeneration)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void setPopulationLimit(PopulationLimit<TestSuiteChromosome> limit)
            throws IllegalArgumentException { // (6)
        if (algorithm != null) {
            algorithm.setPopulationLimit(mapPopulationLimitToTestLevel(limit));
        } else {
            // When we hit this branch, this TestSuiteAdapter object is currently being
            // constructed, and this method was invoked by the constructor of the super class
            // (i.e., GeneticAlgorithm). We simply do nothing.
        }
    }

    /**
     * This function converts
     *
     * @param limit
     * @return
     */
    private PopulationLimit<TestChromosome> mapPopulationLimitToTestLevel(PopulationLimit<TestSuiteChromosome> limit){
        if (limit instanceof IndividualPopulationLimit) {
            return new IndividualPopulationLimit<>((IndividualPopulationLimit<?>) limit);
        } else if (limit instanceof StatementsPopulationLimit) {
            return new StatementsPopulationLimit<>((StatementsPopulationLimit<?>) limit);
        } else if (limit instanceof SizePopulationLimit) {
            return new SizePopulationLimit<>((SizePopulationLimit<?>) limit);
        } else {
            throw new IllegalArgumentException("cannot adapt population limit " + limit);
        }
    }

    @Override
    final public boolean isFinished() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void addStoppingCondition(StoppingCondition<TestSuiteChromosome> condition)
            throws IllegalArgumentException { // (1a)
        if (algorithm != null) {
            final StoppingCondition<TestChromosome> adapteeCondition;
            if (condition instanceof ZeroFitnessStoppingCondition) {
                super.addStoppingCondition(condition);
                return;
            } else if (condition instanceof ShutdownTestWriter) {
                adapteeCondition = new ShutdownTestWriter<>();
            } else if (condition instanceof RMIStoppingCondition) {
                // TODO voglseb: This can break something? Looks so
                algorithm.addStoppingCondition(RMIStoppingCondition.getInstance());
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
    final public Set<StoppingCondition<TestSuiteChromosome>> getStoppingConditions() {
        return algorithm.getStoppingConditions().stream()
                .map(TestSuiteAdapter::<TestSuiteChromosome,TestChromosome>mapStoppingCondition)
                .collect(toSet());
    }

    /**
     * Exchanges the generic parameters of a Stopping condition (if possible).
     *
     * @param stoppingCondition the stopping condition with "wrong" generic parameters.
     * @param <T> the desired target chromosome type
     * @param <U> the given source chromosome type
     * @return
     */
    private static <T extends Chromosome<T>, U extends Chromosome<U>> StoppingCondition<T>
            mapStoppingCondition(StoppingCondition<U> stoppingCondition) {
        if (stoppingCondition instanceof MaxTimeStoppingCondition) {
            return new MaxTimeStoppingCondition<>();
        } else if (stoppingCondition instanceof MaxGenerationStoppingCondition) {
            return new MaxGenerationStoppingCondition<>();
        } else
            throw new IllegalArgumentException("Cannot map stopping condition from test suite level to test case level");
    }

    @Override
    public void setStoppingCondition(StoppingCondition<TestSuiteChromosome> condition) { // (4)
        algorithm.setStoppingCondition(mapStoppingCondition(condition));
    }

    @Override
    final public void removeStoppingCondition(StoppingCondition<TestSuiteChromosome> condition) {
        algorithm.removeStoppingCondition(mapStoppingCondition(condition));
    }

    @Override
    public void resetStoppingConditions() { // (5)
        algorithm.resetStoppingConditions();
    }

    @Override
    final public void setStoppingConditionLimit(int value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected void updateBestIndividualFromArchive() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected boolean isBetterOrEqual(TestSuiteChromosome chromosome1,
                                            TestSuiteChromosome chromosome2)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void printBudget() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public String getBudgetString() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final protected double progress() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override // (12)
    public List<FitnessFunction<TestSuiteChromosome>> getFitnessFunctions() {
        // This method returns a raw List of fitness functions. This is ugly but (at the time of
        // this writing) nothing bad actually happens because MOSuiteStrategy only invokes size()
        // on the returned list.
        return algorithm.getFitnessFunctions().stream()
                .map(TestSuiteAdapter::mapFitnessFunctionToTestSuiteLevel)
                .collect(toList());
    }

    private static TestSuiteFitnessFunction mapFitnessFunctionToTestSuiteLevel(FitnessFunction<TestChromosome> fitnessFunction){
        throw new IllegalArgumentException("Unsupported type of fitness function: " + fitnessFunction.getClass());
    }

    private static FitnessFunction<TestChromosome> mapFitnessFunctionToTestCaseLevel(FitnessFunction<TestSuiteChromosome> fitnessFunction){
        if (fitnessFunction instanceof TestSuiteFitnessFunctionMock) {
            return ((TestSuiteFitnessFunctionMock) fitnessFunction).getWrapped();
        }

        throw new IllegalArgumentException("Unsupported type of fitness function: " + fitnessFunction.getClass());
    }

    @Override // (9)
    public void addFitnessFunctions(Collection<? extends FitnessFunction<TestSuiteChromosome>> functions) {
        // The following code still circumvents the type system by using unsafe raw types and
        // unchecked casts. The code only works if a certain assumption holds:
        // MOSuiteStrategy will only ever pass a list of TestFitnessFunctions to this method.
        // In all other cases, this code will blow up. This issue should be fixed as soon as
        // possible, e.g., by creating an adapter class for TestFitnessFunctions to dress up as
        // TestSuiteFitnessFunctions.

        // List<TestFitnessFunction> fs = (List<TestFitnessFunction>) functions
        Collection<FitnessFunction<TestChromosome>> fs = functions.stream()
                .map(TestSuiteAdapter::mapFitnessFunctionToTestCaseLevel)
                .collect(toList());

        algorithm.addFitnessFunctions(fs);
    }

    @Override
    public String toString() {
        if (algorithm == null) { // avoids NPE for debuggers automatically invoking toString()
            return "TestSuiteAdapter under construction";
        }

        return algorithm.toString();
    }

    /**
     * A wrapper class for fitness functions of test chromosomes to make them (sort of) work
     * in environments where fitness functions of test suite chromosomes are needed. This is mainly
     * useful for StatisticsListener. In its searchStarted() method, it only checks if we have
     * minimization or maximization functions. It does nothing else with those fitness functions.
     * So, recording this information in a wrapper and returning it properly to StatisticsListener
     * is fine.
     */
    private static class TestSuiteFitnessFunctionWrapper extends TestSuiteFitnessFunction {
        private final boolean maximizationFunction;

        TestSuiteFitnessFunctionWrapper(FitnessFunction<TestChromosome> fitnessFunction) {
            super();
            maximizationFunction = fitnessFunction.isMaximizationFunction();
        }

        @Override
        public double getFitness(TestSuiteChromosome individual)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException("cannot apply wrapped TestFitnessFunction to " +
                    "TestSuiteChromosome");
        }

        @Override
        public boolean isMaximizationFunction() {
            return maximizationFunction;
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
