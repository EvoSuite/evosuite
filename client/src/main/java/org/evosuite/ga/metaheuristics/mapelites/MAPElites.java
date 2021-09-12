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
package org.evosuite.ga.metaheuristics.mapelites;

import org.evosuite.Properties;
import org.evosuite.assertion.Inspector;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.crossover.SinglePointCrossOver;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.IterUtil;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

/**
 * MAP-Elites implementation
 *
 * <p>
 * <b>Reference: </b> Mouret, Jean-Baptiste, and Jeff Clune. "Illuminating search spaces by mapping
 * elites." arXiv preprint arXiv:1504.04909 (2015).
 * </p>
 *
 * @author Felix Prasse
 */
public class MAPElites extends GeneticAlgorithm<TestChromosome> {
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(MAPElites.class);

    private final Map<FitnessFunctionWrapper, Map<FeatureVector, TestChromosome>> populationMap;
    private final Set<FeatureVector> droppedFeatureVectors;

    private final int featureVectorPossibilityCount;
    private final int featureCount;

    private final List<TestChromosome> bestIndividuals;

    private static final List<FeatureVector> IGNORE_VECTORS =
            Arrays.asList(new FeatureVector(new Inspector[0], null));

    private final CrossOverFunction<TestChromosome> crossoverFunction = new SinglePointCrossOver<>();

    public MAPElites(ChromosomeFactory<TestChromosome> factory) {
        super(factory);
        this.bestIndividuals = new LinkedList<>();
        this.droppedFeatureVectors = new LinkedHashSet<>();
        TestResultObserver observer = new TestResultObserver();
        this.featureVectorPossibilityCount = observer.getPossibilityCount();
        this.featureCount = observer.getFeatureVectorLength();
        TestCaseExecutor.getInstance().addObserver(observer);

        this.populationMap = new LinkedHashMap<>();
    }

    public void addTestFitnessFunctions(List<TestFitnessFunction> functions) {
        for (TestFitnessFunction function : functions) {
            this.populationMap.put(new FitnessFunctionWrapper(function), new LinkedHashMap<>());
            this.addFitnessFunction(function);
        }
    }

    /**
     * Mutate one branch on average
     *
     * @return The chromosomes to be mutated
     */
    private Set<TestChromosome> getToMutateWithChance() {
        Set<TestChromosome> toMutate = new LinkedHashSet<>(1);

        List<FitnessFunctionWrapper> minima = getMinimalBranches();

        final double chance = 1.0 / minima.size();

        for (FitnessFunctionWrapper branch : minima) {
            if (Randomness.nextDouble() <= chance) {
                branch.getCounter().increment();

                TestChromosome chromosome = Randomness.choice(this.populationMap.get(branch).values());

                if (chromosome != null) {
                    toMutate.add(chromosome);
                }
            }
        }

        return toMutate;
    }


    /**
     * Mutate every branch
     *
     * @return The chromosomes to be mutated
     */
    private Set<TestChromosome> getToMutateAll() {
        Set<TestChromosome> toMutate = new LinkedHashSet<>(populationMap.values().size());

        for (Map<FeatureVector, TestChromosome> entry : populationMap.values()) {
            TestChromosome chromosome = Randomness.choice(entry.values());

            if (chromosome != null) {
                toMutate.add(chromosome);
            }
        }

        return toMutate;
    }

    private List<FitnessFunctionWrapper> getMinimalBranches() {
        return IterUtil.minList(this.populationMap.keySet(),
                comparing(FitnessFunctionWrapper::getCounter));
    }

    /**
     * Mutate exactly one branch and one chromosome
     *
     * @return The chromosomes to be mutated
     */
    private Set<TestChromosome> getToMutateRandom() {
        Set<TestChromosome> toMutate = new LinkedHashSet<>(1);

        List<FitnessFunctionWrapper> minima = getMinimalBranches();

        FitnessFunctionWrapper selectedBranch = Randomness.choice(minima);

        if (selectedBranch == null) {
            return toMutate;
        }

        selectedBranch.getCounter().increment();
        Map<FeatureVector, TestChromosome> entry = this.populationMap.get(selectedBranch);

        TestChromosome chromosome = Randomness.choice(entry.values());

        if (chromosome != null) {
            toMutate.add(chromosome);
        }

        return toMutate;
    }

    private Set<TestChromosome> getToMutate() {
        switch (Properties.MAP_ELITES_CHOICE) {
            case ALL:
                return this.getToMutateAll();
            case SINGLE:
                return this.getToMutateRandom();
            default:
            case SINGLE_AVG:
                return this.getToMutateWithChance();
        }
    }

    private void applyMutation(TestChromosome chromosome, TestChromosome parent) {
        this.removeUnusedVariables(chromosome);

        if (Properties.MAP_ELITES_MOSA_MUTATIONS) {
            this.mutate(chromosome, parent);
        } else {
            notifyMutation(chromosome);
            chromosome.mutate();
        }

        if (chromosome.isChanged() && !isTooLong(chromosome)) {
            this.analyzeChromosome(chromosome);
        }
    }

    @Override
    protected void evolve() {
        Set<TestChromosome> parents1 = this.getToMutate();
        Set<TestChromosome> parents2 = this.getToMutate();

        Set<TestChromosome> toMutate = new LinkedHashSet<>();

        for (TestChromosome parent1 : parents1) {
            TestChromosome offspring1 = parent1.clone();

            if (parents2.size() > 0 && Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
                TestChromosome parent2 = Randomness.choice(parents2);
                TestChromosome offspring2 = parent2.clone();

                try {
                    this.crossoverFunction.crossOver(offspring1, offspring2);
                } catch (ConstructionFailedException e) {
                    logger.debug("CrossOver failed.");
                    continue;
                }

                applyMutation(offspring2, parent2);
            }
            applyMutation(offspring1, parent1);
        }

        if ((toMutate.isEmpty() && Properties.MAP_ELITES_CHOICE != Properties.MapElitesChoice.SINGLE_AVG)
                || Randomness.nextDouble() <= Properties.MAP_ELITES_RANDOM) {
            this.analyzeChromosome(this.getRandomPopulation(1).get(0));
        }

        ++currentIteration;
    }

    /**
     * Method used to mutate an offspring.
     * <p>
     * Copied from AbstractMOSA
     *
     * @param offspring
     * @param parent
     */
    private void mutate(TestChromosome offspring, TestChromosome parent) {
        offspring.mutate();
        if (!offspring.isChanged()) {
            // if offspring is not changed, we try to mutate it once again
            offspring.mutate();
        }
        if (!this.hasMethodCall(offspring)) {
            offspring.setTestCase(parent.getTestCase().clone());
            boolean changed = offspring.mutationInsert();
            if (changed) {
                for (Statement s : offspring.getTestCase()) {
                    s.isValid();
                }
            }
            offspring.setChanged(changed);
        }
        this.notifyMutation(offspring);
    }

    /**
     * This method checks whether the test has only primitive type statements. Indeed,
     * crossover and mutation can lead to tests with no method calls (methods or constructors
     * call), thus, when executed they will never cover something in the class under test.
     * <p>
     * Copied from AbstractMOSA
     *
     * @param test to check
     * @return true if the test has at least one method or constructor call (i.e., the test may
     * cover something when executed; false otherwise
     */
    private boolean hasMethodCall(TestChromosome test) {
        boolean flag = false;
        TestCase tc = test.getTestCase();
        for (Statement s : tc) {
            if (s instanceof MethodStatement) {
                MethodStatement ms = (MethodStatement) s;
                boolean isTargetMethod = ms.getDeclaringClassName().equals(Properties.TARGET_CLASS);
                if (isTargetMethod) {
                    return true;
                }
            }
            if (s instanceof ConstructorStatement) {
                ConstructorStatement ms = (ConstructorStatement) s;
                boolean isTargetMethod = ms.getDeclaringClassName().equals(Properties.TARGET_CLASS);
                if (isTargetMethod) {
                    return true;
                }
            }
        }
        return flag;
    }

    /**
     * When a test case is changed via crossover and/or mutation, it can contains some
     * primitive variables that are not used as input (or to store the output) of method calls.
     * Thus, this method removes all these "trash" statements.
     * <p>
     * Taken from AbstractMOSA
     *
     * @param chromosome
     * @return true or false depending on whether "unused variables" are removed
     */
    private boolean removeUnusedVariables(TestChromosome chromosome) {
        int sizeBefore = chromosome.size();
        TestCase t = chromosome.getTestCase();
        List<Integer> to_delete = new ArrayList<>(chromosome.size());
        boolean has_deleted = false;

        int num = 0;
        for (Statement s : t) {
            VariableReference var = s.getReturnValue();
            boolean delete = false;
            delete = delete || s instanceof PrimitiveStatement;
            delete = delete || s instanceof ArrayStatement;
            delete = delete || s instanceof StringPrimitiveStatement;
            if (!t.hasReferences(var) && delete) {
                to_delete.add(num);
                has_deleted = true;
            }
            num++;
        }
        to_delete.sort(reverseOrder());
        for (Integer position : to_delete) {
            t.remove(position);
        }
        int sizeAfter = chromosome.size();
        if (has_deleted) {
            logger.debug("Removed {} unused statements", (sizeBefore - sizeAfter));
        }
        return has_deleted;
    }

    private int getFoundVectorCount() {
        Set<FeatureVector> vectors = new LinkedHashSet<>();

        this.populationMap.values().forEach(entry -> vectors.addAll(entry.keySet()));

        vectors.addAll(this.droppedFeatureVectors);

        return vectors.size();
    }

    private void sendFeatureData() {
        int foundVectorCount = this.getFoundVectorCount();
        double density = this.getDensity(foundVectorCount);

        ClientServices.getInstance().getClientNode()
                .trackOutputVariable(RuntimeVariable.DensityTimeline, density);

        ClientServices.getInstance().getClientNode()
                .trackOutputVariable(RuntimeVariable.FeaturesFound, foundVectorCount);

        ClientServices.getInstance().getClientNode()
                .trackOutputVariable(RuntimeVariable.FeaturePartitionCount, this.featureVectorPossibilityCount);

        ClientServices.getInstance().getClientNode()
                .trackOutputVariable(RuntimeVariable.FeatureCount, this.featureCount);
    }

    private double getDensity(int foundVectorCount) {
        int n = this.featureVectorPossibilityCount;
        int z = this.getFoundVectorCount();

        double density = z / (double) n;
        return density;
    }

    private void analyzeChromosome(final TestChromosome chromosome) {
        final Iterator<Entry<FitnessFunctionWrapper, Map<FeatureVector, TestChromosome>>> it =
                this.populationMap.entrySet().iterator();

        while (it.hasNext()) {
            final Entry<FitnessFunctionWrapper, Map<FeatureVector, TestChromosome>> entry = it.next();
            final FitnessFunctionWrapper branchFitness = entry.getKey();
            final Map<FeatureVector, TestChromosome> featureMap = entry.getValue();

            final double fitness = branchFitness.getFitness(chromosome);

            final List<FeatureVector> features;

            if (Properties.MAP_ELITES_IGNORE_FEATURES) {
                features = IGNORE_VECTORS;
            } else {
                features = chromosome.getLastExecutionResult().getFeatureVectors();
            }

            for (FeatureVector feature : features) {
                TestChromosome old = featureMap.get(feature);

                if (old == null || branchFitness.getFitness(old) >= fitness) {
                    featureMap.put(feature, chromosome);
                    branchFitness.getCounter().reset();
                }
            }

            if (branchFitness.isCovered(chromosome)) {
                // Remove from map. Covering chromosomes are stored in Archive.getArchiveInstance() and this.coveringChromosomes.
                it.remove();

                this.droppedFeatureVectors.addAll(featureMap.keySet());
                this.bestIndividuals.add(chromosome);
            }
        }
    }


    @Override
    public void initializePopulation() {
        notifySearchStarted();
        currentIteration = 0;

        // Set up initial population
        List<TestChromosome> population = this.getRandomPopulation(Properties.POPULATION);

        if (population.isEmpty()) {
            throw new IllegalStateException();
        }

        for (TestChromosome chromosome : population) {
            this.analyzeChromosome(chromosome);
        }
    }

    @Override
    public TestChromosome getBestIndividual() {
        if (this.bestIndividuals.isEmpty()) {
            return this.chromosomeFactory.getChromosome();
        }

        return this.bestIndividuals.get(0);
    }

    @Override
    public List<TestChromosome> getBestIndividuals() {
        throw new UnsupportedOperationException();
    }

    private void updateAndSortBest() {
        for (Map<FeatureVector, TestChromosome> branch : this.populationMap.values()) {
            this.bestIndividuals.addAll(branch.values());
        }

        if (isMaximizationFunction()) {
            this.bestIndividuals.sort(reverseOrder());
        } else {
            Collections.sort(this.bestIndividuals);
        }
    }

    @Override
    public List<TestChromosome> getPopulation() {
        return this.bestIndividuals;
    }

    @Override
    public void generateSolution() {
        initializePopulation();

        this.sendFeatureData();

        while (!isFinished()) {
            evolve();

            this.sendFeatureData();

            this.notifyIteration();
        }

        updateAndSortBest();
        notifySearchFinished();
    }

}
