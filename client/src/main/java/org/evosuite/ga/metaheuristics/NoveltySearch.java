package org.evosuite.ga.metaheuristics;

import org.evosuite.Properties;
import org.evosuite.ga.*;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NoveltySearch<T extends Chromosome> extends GeneticAlgorithm<T>  {

    private final static Logger logger = LoggerFactory.getLogger(NoveltySearch.class);

    private NoveltyFunction<T> noveltyFunction;

    public NoveltySearch(ChromosomeFactory<T> factory) {
        super(factory);

        noveltyFunction = null; //(NoveltyFunction<T>) new BranchNoveltyFunction();
        // setReplacementFunction(new FitnessReplacementFunction());
    }

    public void setNoveltyFunction(NoveltyFunction<T> function) {
        this.noveltyFunction = function;
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

    /**
     * Calculate fitness for all individuals
     */
    protected void calculateNoveltyAndSortPopulation() {
        logger.debug("Calculating novelty for " + population.size() + " individuals");

        Iterator<T> iterator = population.iterator();
        Map<T, Double> noveltyMap = new LinkedHashMap<>();

        while (iterator.hasNext()) {
            T c = iterator.next();
            if (isFinished()) {
                if (c.isChanged())
                    iterator.remove();
            } else {
                // TODO: This needs to take the archive into account
                double novelty = noveltyFunction.getNovelty(c, population);
                noveltyMap.put(c, novelty);
            }
        }

        // Sort population
        sortPopulation(population, noveltyMap);
    }

    @Override
    public void initializePopulation() {
        notifySearchStarted();
        currentIteration = 0;

        // Set up initial population
        generateInitialPopulation(Properties.POPULATION);

        // Determine novelty
        calculateNoveltyAndSortPopulation();
        this.notifyIteration();
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

    @Override
    public void generateSolution() {

        if (population.isEmpty())
            initializePopulation();

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
