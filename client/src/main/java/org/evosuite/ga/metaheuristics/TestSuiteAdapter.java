/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics;

import org.evosuite.ProgressMonitor;
import org.evosuite.ShutdownTestWriter;
import org.evosuite.ga.*;
import org.evosuite.ga.bloatcontrol.BloatControlFunction;
import org.evosuite.ga.bloatcontrol.MaxSizeBloatControl;
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
import org.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import org.evosuite.testsuite.StatementsPopulationLimit;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ResourceController;

import java.util.*;
import java.util.stream.Collectors;

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

    private static final long serialVersionUID = -506409298544885038L;
    private final IdentityHashMap<SearchListener<TestSuiteChromosome>, SearchListener<TestChromosome>> searchListenerMapping = new IdentityHashMap<>();

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
        algorithm.addListener(new AdapteeListener(this));
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
    final protected void evolve() {
        algorithm.evolve();
    }

    @Override
    final public void initializePopulation() {
        algorithm.initializePopulation();
    }

    @Override
    public void generateSolution() {
        algorithm.generateSolution();
    }

    @Override
    public int getAge() {
        return algorithm.getAge();
    }

    @Override
    final protected void notifyMutation(TestSuiteChromosome chromosome)
            throws UnsupportedOperationException {
        // In contrast to the adaptee, the adapter does not mutate any chromosomes.
        // Therefore, only the adaptee notifies about mutations
        throw new UnsupportedOperationException("The adaptee should notify about mutations");
    }

    @Override
    final protected void notifyEvaluation(TestSuiteChromosome chromosome)
            throws UnsupportedOperationException {
        // In contrast to the adaptee, the adapter does not evaluate any chromosomes.
        // Therefore, only the adaptee notifies about evaluations.
        throw new UnsupportedOperationException("The adaptee should notify about evaluations");
    }

    @Override
    final protected boolean shouldApplyLocalSearch() {
        return algorithm.shouldApplyLocalSearch();
    }

    @Override
    final protected void disableFirstSecondaryCriterion() {
        algorithm.disableFirstSecondaryCriterion();
    }

    @Override
    final protected void enableFirstSecondaryCriterion() {
        algorithm.enableFirstSecondaryCriterion();
    }

    @Override
    final protected void updateSecondaryCriterion(int starvationCounter) {
        algorithm.updateSecondaryCriterion(starvationCounter);
    }

    @Override
    final protected void applyLocalSearch() {
        algorithm.applyLocalSearch();
    }

    @Override
    final protected void updateProbability(boolean improvement) {
        algorithm.updateProbability(improvement);
    }

    @Override
    final protected void generateInitialPopulation(int populationSize) {
        algorithm.generateInitialPopulation(populationSize);
    }

    @Override
    final protected void starveToLimit(int limit) {
        algorithm.starveToLimit(limit);
    }

    @Override
    final protected void starveRandomly(int limit) {
        algorithm.starveRandomly(limit);
    }

    @Override
    final protected void starveByFitness(int limit) {
        algorithm.starveByFitness(limit);
    }

    @Override
    final protected void generateRandomPopulation(int populationSize) {
        algorithm.generateRandomPopulation(populationSize);
    }

    @Override
    final public void clearPopulation() {
        algorithm.clearPopulation();
    }

    @Override
    public void addFitnessFunction(final FitnessFunction<TestSuiteChromosome> function) {
        algorithm.addFitnessFunction(mapFitnessFunctionToTestCaseLevel(function));
    }

    @Override
    final public FitnessFunction<TestSuiteChromosome> getFitnessFunction() {
        return new TestSuiteFitnessFunctionWrapper(algorithm.getFitnessFunction());
    }

    @Override
    final public int getNumberOfFitnessFunctions() {
        return algorithm.getNumberOfFitnessFunctions();
    }

    @Override
    final public SelectionFunction<TestSuiteChromosome> getSelectionFunction() {
        return mapSelectionFunction(algorithm.getSelectionFunction());
    }

    @Override
    public void setSelectionFunction(SelectionFunction<TestSuiteChromosome> function)
            throws IllegalArgumentException {
        final SelectionFunction<TestChromosome> adapteeFunction = mapSelectionFunction(function);
        adapteeFunction.setMaximize(function.isMaximize());
        algorithm.setSelectionFunction(adapteeFunction);
    }

    /**
     * Converts a selection function from either TestSuite or Test case level to the other
     *
     * @param function The function to be converted
     * @param <T>      ToType of the conversion
     * @param <X>      FromType of the conversion
     * @return The converted selection function.
     */
    private static <T extends Chromosome<T>, X extends Chromosome<X>> SelectionFunction<T> mapSelectionFunction(SelectionFunction<X> function) {
        if (function instanceof FitnessProportionateSelection) {
            return new FitnessProportionateSelection<>((FitnessProportionateSelection<?>) function);
        } else if (function instanceof TournamentSelection) {
            return new TournamentSelection<>((TournamentSelection<?>) function);
        } else if (function instanceof BinaryTournamentSelectionCrowdedComparison) {
            return new BinaryTournamentSelectionCrowdedComparison<>(
                    (BinaryTournamentSelectionCrowdedComparison<?>) function);
        } else if (function instanceof TournamentSelectionRankAndCrowdingDistanceComparator) {
            return new TournamentSelectionRankAndCrowdingDistanceComparator<>(
                    (TournamentSelectionRankAndCrowdingDistanceComparator<?>) function);
        } else if (function instanceof BestKSelection) {
            return new BestKSelection<>((BestKSelection<?>) function);
        } else if (function instanceof RandomKSelection) {
            return new RandomKSelection<>((RandomKSelection<?>) function);
        } else if (function instanceof RankSelection) {
            return new RankSelection<>((RankSelection<?>) function);
        } else {
            throw new IllegalArgumentException("cannot adapt selection function " + function);
        }
    }

    @Override
    final public RankingFunction<TestSuiteChromosome> getRankingFunction() {
        return mapRankingFunction(algorithm.getRankingFunction());
    }

    @Override
    public void setRankingFunction(RankingFunction<TestSuiteChromosome> function) {
        final RankingFunction<TestChromosome> adapteeFunction = mapRankingFunction(function);
        algorithm.setRankingFunction(adapteeFunction);
    }

    private static <T extends Chromosome<T>, X extends Chromosome<X>> RankingFunction<T> mapRankingFunction(RankingFunction<X> function) {
        if (function instanceof FastNonDominatedSorting) {
            return new FastNonDominatedSorting<>();
        } else if (function instanceof RankBasedPreferenceSorting) {
            return new RankBasedPreferenceSorting<>();
        } else {
            throw new IllegalArgumentException("cannot adapt ranking function " + function);
        }
    }

    @Override
    final public void setBloatControl(BloatControlFunction<TestSuiteChromosome> bcf) {
        algorithm.setBloatControl(mapBloatControlToTestLevel(bcf));
    }

    @Override
    public void addBloatControl(BloatControlFunction<TestSuiteChromosome> bloatControl) {
        if (algorithm != null) {
            algorithm.setBloatControl(mapBloatControlToTestLevel(bloatControl));
        }
    }

    private BloatControlFunction<TestChromosome> mapBloatControlToTestLevel(
            BloatControlFunction<TestSuiteChromosome> bloatControl) {
        if (bloatControl instanceof RelativeSuiteLengthBloatControl) {
            final RelativeSuiteLengthBloatControl<?> bcf =
                    (RelativeSuiteLengthBloatControl<?>) bloatControl;
            return new RelativeSuiteLengthBloatControl<>(bcf);
        } else if (bloatControl instanceof MaxSizeBloatControl) {
            final MaxSizeBloatControl<?> bcf =
                    (MaxSizeBloatControl<?>) bloatControl;
            return new MaxSizeBloatControl<>(bcf);
        } else {
            throw new IllegalArgumentException("cannot adapt bloat control function " + bloatControl);
        }
    }

    @Override
    final public boolean isTooLong(TestSuiteChromosome chromosome) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("TestSuiteChromosome to TestChromosome conversion for this function not supported");
    }

    @Override
    final protected void calculateFitness() {
        algorithm.calculateFitness();
    }

    @Override
    final protected void calculateFitness(TestSuiteChromosome c)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("TestSuiteChromosome to TestChromosome conversion for this function not supported");
    }

    @Override
    final protected void calculateFitnessAndSortPopulation() {
        algorithm.calculateFitnessAndSortPopulation();
    }

    @Override
    final public int getPopulationSize() {
        return algorithm.getPopulationSize();
    }

    @Override
    final protected List<TestSuiteChromosome> elitism() {
        return algorithm.elitism().stream().map(TestChromosome::toSuite).collect(toList());
    }

    @Override
    final protected List<TestSuiteChromosome> randomism() {
        return algorithm.randomism().stream().map(TestChromosome::toSuite).collect(Collectors.toList());
    }

    @Override
    final public void updateFitnessFunctionsAndValues() {
        algorithm.updateFitnessFunctionsAndValues();
    }

    @Override
    final public void writeIndividuals(List<TestSuiteChromosome> individuals)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("TestSuiteChromosome to TestChromosome conversion for this function not supported");
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
        } else {
            // When we hit this branch, this TestSuiteAdapter object is currently being
            // constructed, and this method was invoked by the constructor of the super class
            // (i.e., GeneticAlgorithm). We simply do nothing.
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void setCrossOverFunction(CrossOverFunction<TestSuiteChromosome> crossover)
            throws IllegalArgumentException {
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
        } else {
            // When we hit this branch, this TestSuiteAdapter object is currently being
            // constructed, and this method was invoked by the constructor of the super class
            // (i.e., GeneticAlgorithm). We simply do nothing.
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void addListener(SearchListener<TestSuiteChromosome> listener)
            throws IllegalArgumentException {
        if (algorithm != null) {
            if (listener instanceof StatisticsListener) {
                super.addListener(listener);
            } else if (listener instanceof RelativeSuiteLengthBloatControl) {
                super.addListener(listener);
            } else if (listener instanceof ResourceController) {
                if (!searchListenerMapping.containsKey(listener)) {
                    ResourceController<TestChromosome> adapteeListener = new ResourceController<>();
                    searchListenerMapping.put(listener, adapteeListener);
                    algorithm.addListener(adapteeListener);
                }
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
    final public void removeListener(SearchListener<TestSuiteChromosome> listener) {
        super.removeListener(listener);
        if (algorithm != null) {
            if (listener instanceof StatisticsListener) {
                super.removeListener(listener);
            } else if (listener instanceof RelativeSuiteLengthBloatControl) {
                super.removeListener(listener);
            } else if (listener instanceof ResourceController) {
                if (searchListenerMapping.containsKey(listener))
                    algorithm.removeListener(searchListenerMapping.get(listener));
            } else if (listener instanceof ProgressMonitor) {
                super.removeListener(listener);
            } else if (listener instanceof ZeroFitnessStoppingCondition) {
                super.removeListener(listener);
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
        algorithm.sortPopulation();
    }

    @Override
    final public List<TestSuiteChromosome> getPopulation() {
        return Collections.singletonList(algorithm.getPopulation().stream().collect(TestChromosome.toTestSuiteCollector));
    }

    @Override
    final public boolean isNextPopulationFull(List<TestSuiteChromosome> nextGeneration)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("TestSuiteChromosome to TestChromosome conversion for this function not supported");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void setPopulationLimit(PopulationLimit<TestSuiteChromosome> limit)
            throws IllegalArgumentException {
        if (algorithm != null) {
            algorithm.setPopulationLimit(mapPopulationLimit(limit));
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
    private static <T extends Chromosome<T>> PopulationLimit<T> mapPopulationLimit(PopulationLimit<?> limit) {
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
    final public boolean isFinished() {
        return algorithm.isFinished();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void addStoppingCondition(StoppingCondition<TestSuiteChromosome> condition)
            throws IllegalArgumentException {
        if (algorithm != null) {
            algorithm.addStoppingCondition(mapStoppingCondition(condition));
        } else {
            // When we hit this branch, this TestSuiteAdapter object is currently being
            // constructed, and this method was invoked by the constructor of the super class
            // (i.e., GeneticAlgorithm). We simply do nothing.
        }
    }

    @Override
    final public Set<StoppingCondition<TestSuiteChromosome>> getStoppingConditions() {
        return algorithm.getStoppingConditions().stream()
                .map(TestSuiteAdapter::<TestSuiteChromosome>mapStoppingCondition)
                .collect(toSet());
    }

    /**
     * Exchanges the generic parameters of a Stopping condition (if possible).
     *
     * @param stoppingCondition the stopping condition with "wrong" generic parameters.
     * @param <T>               the desired target chromosome type
     * @return
     */
    private static <T extends Chromosome<T>> StoppingCondition<T>
    mapStoppingCondition(StoppingCondition<?> stoppingCondition) {
        if (stoppingCondition instanceof MaxTimeStoppingCondition) {
            return new MaxTimeStoppingCondition<>((MaxTimeStoppingCondition<?>) stoppingCondition);
        } else if (stoppingCondition instanceof TimeDeltaStoppingCondition) {
            return new TimeDeltaStoppingCondition<>((TimeDeltaStoppingCondition<?>) stoppingCondition);
        } else if (stoppingCondition instanceof MaxGenerationStoppingCondition) {
            return new MaxGenerationStoppingCondition<>((MaxGenerationStoppingCondition<?>) stoppingCondition);
        } else if (stoppingCondition instanceof RMIStoppingCondition) {
            return RMIStoppingCondition.getInstance();
        } else if (stoppingCondition instanceof ShutdownTestWriter) {
            return new ShutdownTestWriter<>((ShutdownTestWriter<?>) stoppingCondition);
        } else if (stoppingCondition instanceof MaxStatementsStoppingCondition) {
            return new MaxStatementsStoppingCondition<>((MaxStatementsStoppingCondition<?>) stoppingCondition);
        } else if (stoppingCondition instanceof GlobalTimeStoppingCondition) {
            return new GlobalTimeStoppingCondition<>((GlobalTimeStoppingCondition<?>) stoppingCondition);
        } else if (stoppingCondition instanceof SocketStoppingCondition) {
            return SocketStoppingCondition.getInstance();
        } else if (stoppingCondition instanceof ZeroFitnessStoppingCondition) {
            return new ZeroFitnessStoppingCondition<>((ZeroFitnessStoppingCondition<?>) stoppingCondition);
        } else {
            throw new IllegalArgumentException("cannot adapt stopping condition: " + stoppingCondition);
        }
    }

    @Override
    public void setStoppingCondition(StoppingCondition<TestSuiteChromosome> condition) {
        algorithm.setStoppingCondition(mapStoppingCondition(condition));
    }

    @Override
    final public void removeStoppingCondition(StoppingCondition<TestSuiteChromosome> condition) {
        algorithm.removeStoppingCondition(mapStoppingCondition(condition));
    }

    @Override
    public void resetStoppingConditions() {
        algorithm.resetStoppingConditions();
    }

    @Override
    final public void setStoppingConditionLimit(int value) {
        algorithm.setStoppingConditionLimit(value);
    }

    @Override
    final protected void updateBestIndividualFromArchive() {
        algorithm.updateBestIndividualFromArchive();
    }

    @Override
    final protected boolean isBetterOrEqual(TestSuiteChromosome chromosome1,
                                            TestSuiteChromosome chromosome2) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    final public void printBudget() {
        algorithm.printBudget();
    }

    @Override
    final public String getBudgetString() {
        return algorithm.getBudgetString();
    }

    @Override
    final protected double progress() {
        return algorithm.progress();
    }

    @Override
    public List getFitnessFunctions() {
        // This method returns a raw List of fitness functions. This is ugly but (at the time of
        // this writing) nothing bad actually happens because MOSuiteStrategy only invokes size()
        // on the returned list.
        return algorithm.getFitnessFunctions();
    }

    private static FitnessFunction<TestChromosome> mapFitnessFunctionToTestCaseLevel(
            FitnessFunction<TestSuiteChromosome> fitnessFunction) throws IllegalArgumentException {
        if (fitnessFunction instanceof TestSuiteFitnessFunctionMock) {
            return ((TestSuiteFitnessFunctionMock) fitnessFunction).getWrapped();
        }

        throw new IllegalArgumentException("Unsupported type of fitness function: " + fitnessFunction.getClass());
    }

    @Override
    public void addFitnessFunctions(Collection<? extends FitnessFunction<TestSuiteChromosome>> functions) {
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
        private static final long serialVersionUID = 5136258490569674883L;

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
