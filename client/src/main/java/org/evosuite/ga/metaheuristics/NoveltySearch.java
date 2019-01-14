package org.evosuite.ga.metaheuristics;

import org.evosuite.Properties;
import org.evosuite.coverage.dataflow.Feature;
import org.evosuite.ga.*;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.mosa.AbstractMOSA;
import org.evosuite.ga.operators.ranking.CrowdingDistance;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.*;

public class NoveltySearch<T extends Chromosome> extends  GeneticAlgorithm<T>{

    private final static Logger logger = LoggerFactory.getLogger(NoveltySearch.class);

    private NoveltyFunction<T> noveltyFunction;

    private LocalCompetition<T> lc = new LocalCompetition<>();

    public NoveltySearch(ChromosomeFactory<T> factory) {
        super(factory);
    }

    public void setNoveltyFunction(NoveltyFunction<T> function) {
        this.noveltyFunction = function;
    }

    /**
     * Use Novelty Function to do the calculation
     *
     */
    public void calculateNoveltyAndSortPopulation(){
        logger.debug("Calculating novelty for " + population.size() + " individuals");

        noveltyFunction.calculateNovelty(population, noveltyArchive);
        noveltyFunction.sortPopulation(population);
    }

    /**
     * Sort the population by novelty
     */
    protected void sortPopulation(List<T> population, Map<T, Double> noveltyMap) {
        // TODO: Handle case when no novelty value is stored in map
        // TODO: Use lambdas
        Collections.sort(population, Collections.reverseOrder(new Comparator<T>() {
            @Override
            public int compare(Chromosome c1, Chromosome c2) {
                return Double.compare(noveltyMap.get(c1), noveltyMap.get(c2));
            }
        }));
    }


    protected TestSuiteChromosome generateSuite() {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        Archive.getArchiveInstance().getSolutions().forEach(test -> suite.addTest(test));
        return suite;
    }

    public static TestSuiteChromosome result;

    public TestSuiteChromosome getBestIndividual1() {
        TestSuiteChromosome best = this.generateSuite();
        /*if (best.getTestChromosomes().isEmpty()) {

            for (FitnessFunction suiteFitness : this.fitnessFunctions) {
                best.setCoverage(suiteFitness, 0.0);
                best.setFitness(suiteFitness,  1.0);
            }
            return (TestSuiteChromosome) best;
        }*/

        // compute overall fitness and coverage
        //this.computeCoverageAndFitness(best);
        result = (TestSuiteChromosome) best;
        return result;
    }

    public TestSuiteChromosome getBestIndividual2(){
        return result;
    }


    @Override
    public void initializePopulation() {
        //notifySearchStarted();
        currentIteration = 0;

        // Set up initial population
        generateInitialPopulation(Properties.POPULATION);

        // Determine novelty
        calculateNoveltyAndSortPopulation();

        // Determine fitness
        this.calculateFitness();

        // form sub regions
        // calculate distance w.r.t a fixed point
        // TODO: do only if LC switch is on
        formSubRegions();

        this.notifyIteration();
    }

    public void formSubRegions(){
        lc.formSubRegions(this.population);
    }

    @Override
    protected void evolve() {

        List<T> newGeneration = new ArrayList<T>();

        while (!isNextPopulationFull(newGeneration)) {
            T parent1 = selectionFunction.select(population);
            T parent2 = selectionFunction.select(population);

            T offspring1 = (T)parent1.clone();
            T offspring2 = (T)parent2.clone();

            try {
                if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
                    crossoverFunction.crossOver(offspring1, offspring2);
                }

                notifyMutation(offspring1);
                offspring1.mutate();
                notifyMutation(offspring2);
                offspring2.mutate();

                if(offspring1.isChanged()) {
                    offspring1.updateAge(currentIteration);
                }
                if(offspring2.isChanged()) {
                    offspring2.updateAge(currentIteration);
                }
            } catch (ConstructionFailedException e) {
                logger.info("CrossOver/Mutation failed.");
                continue;
            }

            if (!isTooLong(offspring1))
                newGeneration.add(offspring1);
            else
                newGeneration.add(parent1);

            if (!isTooLong(offspring2))
                newGeneration.add(offspring2);
            else
                newGeneration.add(parent2);
        }

        population = newGeneration;
        //archive
        updateFitnessFunctionsAndValues();
        //
        currentIteration++;
    }

    protected void addUncoveredGoal(FitnessFunction<T> goal) {
        Archive.getArchiveInstance().addTarget((TestFitnessFunction) goal);
    }

    @Override
    public void generateSolution() {
        logger.info("executing generateSolution function");

        // keep track of covered goals
        this.fitnessFunctions.forEach(this::addUncoveredGoal);

        if (population.isEmpty())
            initializePopulation();

        // Calculate dominance ranks and crowding distance
        /*this.rankingFunction.computeRankingAssignment(this.population, this.getUncoveredGoals());
        for (int i = 0; i < this.rankingFunction.getNumberOfSubfronts(); i++) {
            this.distance.fastEpsilonDominanceAssignment(this.rankingFunction.getSubfront(i), this.getUncoveredGoals());
        }*/

        logger.warn("Starting evolution of novelty search algorithm");

        while (!isFinished()) {
            logger.warn("Current population: " + getAge() + "/" + Properties.SEARCH_BUDGET);
            //logger.info("Best fitness: " + getBestIndividual().getFitness());

            evolve();

            // TODO: Sort by novelty
            calculateNoveltyAndSortPopulation();

            this.notifyIteration();
        }

        updateBestIndividualFromArchive();
        notifySearchFinished();

    }
}
