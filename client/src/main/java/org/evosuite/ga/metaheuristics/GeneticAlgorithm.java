/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.bloatcontrol.BloatControlFunction;
import org.evosuite.ga.localsearch.DefaultLocalSearchObjective;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.crossover.SinglePointCrossOver;
import org.evosuite.ga.operators.ranking.RankBasedPreferenceSorting;
import org.evosuite.ga.operators.ranking.RankingFunction;
import org.evosuite.ga.operators.selection.RankSelection;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.ga.populationlimit.IndividualPopulationLimit;
import org.evosuite.ga.populationlimit.PopulationLimit;
import org.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Abstract superclass of genetic algorithms
 * 
 * @author Gordon Fraser
 */
public abstract class GeneticAlgorithm<T extends Chromosome> implements SearchAlgorithm,
		Serializable {

	private static final long serialVersionUID = 5155609385855093435L;

	private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithm.class);

	/** Fitness function to rank individuals */
	protected List<FitnessFunction<T>> fitnessFunctions = new ArrayList<>();

	/** Selection function to select parents */
	protected SelectionFunction<T> selectionFunction = new RankSelection<>();

	/** CrossOver function */
	protected CrossOverFunction crossoverFunction = new SinglePointCrossOver();

	/** Current population */
	protected List<T> population = new ArrayList<>();

	/** Generator for initial population */
	protected ChromosomeFactory<T> chromosomeFactory;

	/** Listeners */
	protected transient Set<SearchListener> listeners = new HashSet<>();

	/** List of conditions on which to end the search */
	protected transient Set<StoppingCondition> stoppingConditions = new HashSet<>();

	/** Bloat control, to avoid too long chromosomes */
	protected Set<BloatControlFunction> bloatControl = new HashSet<>();

	/** Local search might need a different local objective */
	protected LocalSearchObjective<T> localObjective = new DefaultLocalSearchObjective<>();

	/** The population limit decides when an iteration is done */
	protected PopulationLimit populationLimit = new IndividualPopulationLimit();

	/** Age of the population */
	protected int currentIteration = 0;

	protected double localSearchProbability = Properties.LOCAL_SEARCH_PROBABILITY;

	/** Selected ranking strategy **/
	protected RankingFunction<T> rankingFunction = new RankBasedPreferenceSorting<>();

	/**
	 * Constructor
	 * 
	 * @param factory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public GeneticAlgorithm(ChromosomeFactory<T> factory) {
		chromosomeFactory = factory;
		addStoppingCondition(new MaxGenerationStoppingCondition());
		if (Properties.LOCAL_SEARCH_RATE > 0)
			addListener(LocalSearchBudget.getInstance());
		// addBloatControl(new MaxSizeBloatControl());
	}

	/**
	 * Generate one new generation
	 */
	protected abstract void evolve();

	/**
	 * Local search is only applied every X generations
	 * 
	 * @return a boolean.
	 */
	protected boolean shouldApplyLocalSearch() {
		// If local search is not set to a rate, then we don't use it at all
		if (Properties.LOCAL_SEARCH_RATE <= 0)
			return false;

		if (getAge() % Properties.LOCAL_SEARCH_RATE == 0) {
            return Randomness.nextDouble() <= localSearchProbability;
		}
		return false;
	}

	protected void disableFirstSecondaryCriterion() {
		if (TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {
			TestSuiteChromosome.disableFirstSecondaryObjective();
			if (ArrayUtil.contains(Properties.SECONDARY_OBJECTIVE, Properties.SecondaryObjective.IBRANCH)) {
				ExecutionTracer.disableContext();
			}
			logger.info("second secondary criterion enabled");
		}
	}

	protected void enableFirstSecondaryCriterion() {
		if (TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {
			TestSuiteChromosome.enableFirstSecondaryObjective();
			if (ArrayUtil.contains(Properties.SECONDARY_OBJECTIVE, Properties.SecondaryObjective.IBRANCH)) {
				ExecutionTracer.enableContext();
			}
			logger.info("first secondary criterion enabled");
		}
	}

	/**
	 * enable and disable secondary criteria according to the strategy defined
	 * in the Properties file.
	 * 
	 * @param starvationCounter
	 */
	protected void updateSecondaryCriterion(int starvationCounter) {

		if (Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER > 0
				&& TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {

			double progress = this.progress() * 100.0;

			if (progress > Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER) {
				if (Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION) {
					updateSecondaryObjectiveStarvation(starvationCounter);
				} else {
					enableFirstSecondaryCriterion();
					Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER = 0;
				}
			}
		} else if (Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION
				&& Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER == 0
				&& TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {
			updateSecondaryObjectiveStarvation(starvationCounter);
		}
	}

	private void updateSecondaryObjectiveStarvation(int starvationCounter) {
		if (starvationCounter > Properties.STARVATION_AFTER_GENERATION && !TestSuiteChromosome.isFirstSecondaryObjectiveEnabled()) {
			enableFirstSecondaryCriterion();
        } else if (starvationCounter == 0
                && TestSuiteChromosome.isFirstSecondaryObjectiveEnabled()
                && TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {
            disableFirstSecondaryCriterion();
        }
    }

	/**
	 * Apply local search, starting from the best individual and continue
	 * applying it to all individuals until the local search budget is used up.
	 * 
	 * The population list is re-ordered if needed.
	 */
	protected void applyLocalSearch() {
		if (!shouldApplyLocalSearch())
			return;

		logger.debug("Applying local search");
		LocalSearchBudget.getInstance().localSearchStarted();

		boolean improvement = false;

		for (Chromosome individual : population) {
			if (isFinished())
				break;

			if (LocalSearchBudget.getInstance().isFinished()) {
				logger.debug("Local search budget used up, exiting local search");
				break;
			}

			if (individual.localSearch(localObjective)) {
				improvement = true;
			}
		}
		
		if (improvement) {
			DSEStats.getInstance().reportNewIncrease();
			updateProbability(true);
			logger.debug("Increasing probability of applying LS to " + localSearchProbability);
		} else {
			DSEStats.getInstance().reportNewDecrease();
			updateProbability(false);
			logger.debug("Decreasing probability of applying LS to " + localSearchProbability);
		}

		// If an improvement occurred to one of the individuals, it could
		// be the case that the improvement was so good, that the individual
		// has surpassed to the previous individual, which makes the population
		// list not sorted any more.
        if (improvement && !populationIsSorted()) {
            this.sortPopulation();
        }
    }

	/**
	 * Returns true if the population is sorted according to the fitness 
	 * values.
	 * 
	 * @return true if the population is sorted (or empty)
	 */
	private boolean populationIsSorted() {
        Chromosome previous = null;
        for (Chromosome current : this.population) {
            if (previous != null && !isBetterOrEqual(previous, current)) {
                // at least two individuals are not sorted
                return false;
            }
            previous = current;
		}
		// the population is sorted (or empty)
		return true;
	}

	/**
	 * Returns true if the fitness functions are maximization functions
	 * or false if all fitness functions are minimisation functions.
	 * It expects that all fitnessFunctions are minimising or maximising,
	 * it cannot happen that minimization and maximization functions are
	 * together.
	 * 
	 * @return
	 */
	protected final boolean isMaximizationFunction() {
		return fitnessFunctions.get(0).isMaximizationFunction();
	}

	protected void updateProbability(boolean improvement){
		if (improvement) {
			localSearchProbability *= Properties.LOCAL_SEARCH_ADAPTATION_RATE;
			localSearchProbability = Math.min(localSearchProbability, 1.0);
		} else {
			localSearchProbability /= Properties.LOCAL_SEARCH_ADAPTATION_RATE;
			localSearchProbability = Math.max(localSearchProbability, Double.MIN_VALUE);
		}
		// localSearchProbability = Math.pow(
		// 1.0 + ((1.0 - localSearchProbability) / localSearchProbability) *
		// Math.exp(delta), -1.0);
	}
	
	
	/**
	 * Apply dynamic symbolic execution
	 */
	/*
	 * @Deprecated protected void applyDSE() {
	 * logger.info("Applying DSE at generation " + currentIteration);
	 * DSEBudget.DSEStarted();
	 * 
	 * boolean success = false;
	 * 
	 * for (Chromosome individual : population) { if (isFinished()) break;
	 * 
	 * if (DSEBudget.isFinished()) break;
	 * 
	 * boolean result = individual.applyDSE(this); if(result) success = true; }
	 * 
	 * if(Properties.DSE_ADAPTIVE_PROBABILITY > 0.0) { if(success) {
	 * Properties.DSE_ADAPTIVE_PROBABILITY *= Properties.DSE_ADAPTIVE_RATE;
	 * Properties.DSE_ADAPTIVE_PROBABILITY =
	 * Math.min(Properties.DSE_ADAPTIVE_PROBABILITY, 1.0); } else {
	 * Properties.DSE_ADAPTIVE_PROBABILITY /= Properties.DSE_ADAPTIVE_RATE;
	 * Properties.DSE_ADAPTIVE_PROBABILITY =
	 * Math.max(Properties.DSE_ADAPTIVE_PROBABILITY, Double.MIN_VALUE); }
	 * logger.
	 * info("Updating DSE probability to "+Properties.DSE_ADAPTIVE_PROBABILITY);
	 * } }
	 */
	/**
	 * Set up initial population
	 */
	public abstract void initializePopulation();

	/**
	 * {@inheritDoc}
	 * 
	 * Generate solution
	 */
	@Override
	public abstract void generateSolution();

	/**
	 * Fills the population at first with recycled chromosomes - for more
	 * information see recycleChromosomes() and ChromosomeRecycler - and after
	 * that, the population is filled with random chromosomes.
	 * 
	 * This method guarantees at least a proportion of
	 * Properties.initially_enforeced_Randomness % of random chromosomes
	 * 
	 * @param population_size
	 *            a int.
	 */
	protected void generateInitialPopulation(int population_size) {
		generateRandomPopulation(population_size - population.size());
	}

	/**
	 * This method can be used to kick out chromosomes when the population is
	 * possibly overcrowded
	 * 
	 * Depending on the Property "starve_by_fitness" chromosome are either
	 * kicked out randomly or according to their fitness
	 * 
	 * @param limit
	 *            a int.
	 */
	protected void starveToLimit(int limit) {
		if (Properties.STARVE_BY_FITNESS)
			starveByFitness(limit);
		else
			starveRandomly(limit);
	}

	/**
	 * This method can be used to kick out random chromosomes in the current
	 * population until the given limit is reached again.
	 * 
	 * @param limit
	 *            a int.
	 */
	protected void starveRandomly(int limit) {
		while (population.size() > limit) {
			int removePos = Randomness.nextInt() % population.size();
			population.remove(removePos);
		}
	}

	/**
	 * This method can be used to kick out the worst chromosomes in the current
	 * population until the given limit is reached again.
	 * 
	 * @param limit
	 *            a int.
	 */
	protected void starveByFitness(int limit) {
		calculateFitnessAndSortPopulation();
        if (population.size() > limit) {
            population.subList(limit, population.size()).clear();
		}
	}

	/**
	 * Generate random population of given size
	 * 
	 * @param population_size
	 *            a int.
	 */
	protected void generateRandomPopulation(int population_size) {
	    population.addAll(this.getRandomPopulation(population_size));
	}

	protected List<T> getRandomPopulation(int population_size) {
      logger.debug("Creating random population");

      List<T> newPopulation = new ArrayList<>(population_size);

      for (int i = 0; i < population_size; i++) {
          T individual = chromosomeFactory.getChromosome();
            fitnessFunctions.forEach(individual::addFitness);

          newPopulation.add(individual);
		  //logger.error("Created a new individual");
          if (isFinished())
              break;
      }
      logger.debug("Created " + newPopulation.size() + " individuals");

      return newPopulation;
  }

	/**
	 * Delete all current individuals
	 */
	public void clearPopulation() {
		logger.debug("Resetting population");
		population.clear();
	}

	/**
	 * Add new fitness function (i.e., for new mutation)
	 * 
	 * @param function
	 *            a {@link org.evosuite.ga.FitnessFunction} object.
	 */
	public void addFitnessFunction(FitnessFunction<T> function) {
		fitnessFunctions.add(function);
		localObjective.addFitnessFunction(function);
	}

	public void addFitnessFunctions(List<FitnessFunction<T>> functions) {
        functions.forEach(this::addFitnessFunction);
	}

	/**
	 * Get currently used fitness function
	 * 
	 * @return a {@link org.evosuite.ga.FitnessFunction} object.
	 */
	public FitnessFunction<T> getFitnessFunction() {
		return fitnessFunctions.get(0);
	}

	/**
	 * Get all used fitness function
	 * 
	 * @return a {@link org.evosuite.ga.FitnessFunction} object.
	 */
	public List<FitnessFunction<T>> getFitnessFunctions() {
		return fitnessFunctions;
	}

	public int getNumberOfFitnessFunctions() {
		return fitnessFunctions.size();
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		int i = 0;
		for (Chromosome c : population) {
			str.append("\n  - test " + i);

			for (FitnessFunction<T> ff : this.fitnessFunctions) {
				DecimalFormat df = new DecimalFormat("#.#####");
				str.append(", " + ff.getClass().getSimpleName().replace("CoverageSuiteFitness", "")
						+ " " + df.format(c.getFitness(ff)));
			}

			i++;
		}

		return str.toString();
	}

	
	/**
	 * Set new fitness function (i.e., for new mutation)
	 * 
	 * @param function
	 *            a
	 *            {@link org.evosuite.ga.operators.selection.SelectionFunction}
	 *            object.
	 */
	public void setSelectionFunction(SelectionFunction<T> function) {
		selectionFunction = function;
	}

	/**
	 * Get currently used fitness function
	 * 
	 * @return a {@link org.evosuite.ga.operators.selection.SelectionFunction}
	 *         object.
	 */
	public SelectionFunction<T> getSelectionFunction() {
		return selectionFunction;
	}

	/**
	 * Set the new ranking function (only used by MOO algorithms)
	 * 
	 * @param function a {@link org.evosuite.ga.operators.ranking.RankingFunction} object
	 */
	public void setRankingFunction(RankingFunction<T> function) {
		this.rankingFunction = function;
	}

	/**
	 * Get currently used ranking function (only used by MOO algorithms)
	 * 
	 * @return a {@link org.evosuite.ga.operators.ranking.RankingFunction} object
	 */
	public RankingFunction<T> getRankingFunction() {
		return this.rankingFunction;
	}

	/**
	 * Set new bloat control function
	 * 
	 * @param bloat_control
	 *            a {@link org.evosuite.ga.bloatcontrol.BloatControlFunction}
	 *            object.
	 */
	public void setBloatControl(BloatControlFunction bloat_control) {
		this.bloatControl.clear();
		addBloatControl(bloat_control);
	}

	/**
	 * Set new bloat control function
	 * 
	 * @param bloat_control
	 *            a {@link org.evosuite.ga.bloatcontrol.BloatControlFunction}
	 *            object.
	 */
	public void addBloatControl(BloatControlFunction bloat_control) {
		this.bloatControl.add(bloat_control);
	}

	/**
	 * Check whether individual is suitable according to bloat control functions
	 * 
	 * @param chromosome
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a boolean.
	 */
	public boolean isTooLong(Chromosome chromosome) {
        return bloatControl.stream().anyMatch(b -> b.isTooLong(chromosome));
	}

	/**
	 * Get number of iterations
	 * 
	 * @return Number of iterations
	 */
	public int getAge() {
		return currentIteration;
	}

	/**
	 * Calculate fitness for all individuals
	 */
	protected void calculateFitness() {
		logger.debug("Calculating fitness for " + population.size() + " individuals");

		Iterator<T> iterator = this.population.iterator();
		while (iterator.hasNext()) {
			T c = iterator.next();
            
			if (isFinished()) {
				break;
			} else {
				this.calculateFitness(c);
			}
		}
	}

	/**
	 * Calculate fitness for an individual
	 * 
	 * @param c
	 */
	protected void calculateFitness(T c) {
        fitnessFunctions.forEach(ff -> {
            ff.getFitness(c);
			notifyEvaluation(c);
        });
	}

	/**
	 * Calculate fitness for all individuals and sort them
	 */
	protected void calculateFitnessAndSortPopulation() {
		this.calculateFitness();
		// Sort population
		this.sortPopulation();
	}

	/**
	 * <p>
	 * getPopulationSize
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getPopulationSize() {
		return population.size();
	}

	/**
	 * Copy best individuals
	 * 
	 * @return a {@link java.util.List} object.
	 */
	@SuppressWarnings("unchecked")
	protected List<T> elitism() {
		logger.debug("Elitism with ELITE = " + Properties.ELITE);

        List<T> elite = population.stream().limit(Properties.ELITE)
                .peek(c -> logger.trace("Copying individual with fitness " + c.getFitness()))
                .map(c -> (T) c.clone())
                .collect(toList());

		logger.trace("Done.");
		return elite;
	}

	/**
	 * Create random individuals
	 * 
	 * @return a {@link java.util.List} object.
	 */
	protected List<Chromosome> randomism() {
		logger.debug("Randomism");

        return Stream.generate(chromosomeFactory::getChromosome)
                .limit(Properties.ELITE)
                .collect(toList());
	}

	/**
	 * update archive fitness functions
	 */
	public void updateFitnessFunctionsAndValues() {
         fitnessFunctions.forEach(FitnessFunction::updateCoveredGoals);

		// Do we actually have to perform yet another fitness evaluation?
		// Yes, if ARCHIVE has been updated, No otherwise.
		if (!Archive.getArchiveInstance().hasBeenUpdated()) {
			return;
		}

        population.forEach(t -> fitnessFunctions.forEach(ff -> ff.getFitness(t)));

		Archive.getArchiveInstance().setHasBeenUpdated(false);
	}

	/**
	 * Penalty if individual is not unique
	 * 
	 * @param individual
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param generation
	 *            a {@link java.util.List} object.
	 */
	/*
	 * protected void kinCompensation(Chromosome individual, List<Chromosome>
	 * generation) {
	 * 
	 * if (Properties.KINCOMPENSATION >= 1.0) return;
	 * 
	 * boolean unique = true;
	 * 
	 * for (Chromosome other : generation) { if (other == individual) continue;
	 * 
	 * if (other.equals(individual)) { unique = false; break; } }
	 * 
	 * if (!unique) { logger.debug("Applying kin compensation"); if
	 * (fitnessFunction.isMaximizationFunction())
	 * individual.setFitness(individual.getFitness()
	 * Properties.KINCOMPENSATION); else
	 * individual.setFitness(individual.getFitness() (2.0 -
	 * Properties.KINCOMPENSATION)); } }
	 */

	/**
	 * Return the individual with the highest fitChromosomeess
	 * 
	 * @return a {@link org.evosuite.ga.Chromosome} object.
	 */
	public T getBestIndividual() {

		if (population.isEmpty()) {
			return this.chromosomeFactory.getChromosome();
		}

		// Assume population is sorted
		return population.get(0);
	}

	/**
     * Return the individual(s) with the highest fitness
	 *
	 * @return a list of {@link org.evosuite.ga.Chromosome} object(s).
	 */
	public List<T> getBestIndividuals() {

		List<T> bestIndividuals = new ArrayList<>();

		if (this.population.isEmpty()) {
			bestIndividuals.add(this.chromosomeFactory.getChromosome());
			return bestIndividuals;
		}

		if (Properties.ALGORITHM == Algorithm.NSGAII ||
		    Properties.ALGORITHM == Algorithm.SPEA2)
			return population;

		// Assume population is sorted
		bestIndividuals.add(population.get(0));
		return bestIndividuals;
	}

    /**
     * Write to a file all fitness values of each individuals.
     *
     * @param individuals a list of {@link org.evosuite.ga.Chromosome} object(s).
     */
    public void writeIndividuals(List<T> individuals) {
      if (!Properties.WRITE_INDIVIDUALS) {
        return;
      }

      File dir = new File(Properties.REPORT_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Cannot create report dir: " + Properties.REPORT_DIR);
        }

      try {
        File populationFile = new File(
            Properties.REPORT_DIR + File.separator + "pareto_" + this.currentIteration + ".csv");
        populationFile.createNewFile();

        FileWriter fw = new FileWriter(populationFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);

        // header
            final String header = fitnessFunctions.stream()
                    .map(ff -> ff.getClass().getSimpleName())
                    .collect(joining(","));

        if (Properties.ALGORITHM == Algorithm.NSGAII) {
                out.println("rank," + header);
        } else if (Properties.ALGORITHM == Algorithm.SPEA2) {
                out.println("strength," + header);
        }

        // content
            for (T t : individuals) {
                final String content = fitnessFunctions.stream()
                        .map(ff -> Double.toString(t.getFitness(ff)))
                        .collect(joining(","));

          if (Properties.ALGORITHM == Algorithm.NSGAII) {
                    out.println(t.getRank() + "," + content);
          } else if (Properties.ALGORITHM == Algorithm.SPEA2) {
                    out.println(t.getDistance() + "," + content);
          }
        }

        out.close();
        bw.close();
        fw.close();

      } catch (IOException e) {
        e.printStackTrace();
      }
    }

	/**
	 * Set a new factory method
	 * 
	 * @param factory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public void setChromosomeFactory(ChromosomeFactory<T> factory) {
		chromosomeFactory = factory;
	}

	/**
	 * Set a new xover function
	 * 
	 * @param crossover
	 *            a
	 *            {@link org.evosuite.ga.operators.crossover.CrossOverFunction}
	 *            object.
	 */
	public void setCrossOverFunction(CrossOverFunction crossover) {
		this.crossoverFunction = crossover;
	}

	/**
	 * Add a new search listener
	 * 
	 * @param listener
	 *            a {@link org.evosuite.ga.metaheuristics.SearchListener}
	 *            object.
	 */
	public void addListener(SearchListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a search listener
	 * 
	 * @param listener
	 *            a {@link org.evosuite.ga.metaheuristics.SearchListener}
	 *            object.
	 */
	public void removeListener(SearchListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notify all search listeners of search start
	 */
	protected void notifySearchStarted() {
        listeners.forEach(l -> l.searchStarted(this));
	}

	/**
	 * Notify all search listeners of search end
	 */
	protected void notifySearchFinished() {
        listeners.forEach(l -> l.searchFinished(this));
	}

	/**
	 * Notify all search listeners of iteration
	 */
	protected void notifyIteration() {
        listeners.forEach(l -> l.iteration(this));
	}

	/**
	 * Notify all search listeners of fitness evaluation
	 * 
	 * @param chromosome
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 */
	protected void notifyEvaluation(Chromosome chromosome) {
        listeners.forEach(l -> l.fitnessEvaluation(chromosome));
	}

	/**
	 * Notify all search listeners of a mutation
	 * 
	 * @param chromosome
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 */
	protected void notifyMutation(Chromosome chromosome) {
        listeners.forEach(l -> l.modification(chromosome));
	}

	/**
	 * Sort the population by fitness
	 * 
	 * WARN: used only with singular objective algorithms, multi-objective
	 * algorithms should implement their own 'sort'
	 */
	protected void sortPopulation() {
		if (Properties.SHUFFLE_GOALS)
			Randomness.shuffle(population);

		if (isMaximizationFunction()) {
            population.sort(Collections.reverseOrder());
		} else {
			Collections.sort(population);
		}
	}

	/**
	 * Accessor for population Chromosome *
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public List<T> getPopulation() {
		return population;
	}

	/**
	 * Determine if the next generation has reached its size limit
	 * 
	 * @param nextGeneration
	 *            a {@link java.util.List} object.
	 * @return a boolean.
	 */
	public boolean isNextPopulationFull(List<T> nextGeneration) {
		return populationLimit.isPopulationFull(nextGeneration);
	}

	/**
	 * Set a new population limit function
	 * 
	 * @param limit
	 *            a {@link org.evosuite.ga.populationlimit.PopulationLimit}
	 *            object.
	 */
	public void setPopulationLimit(PopulationLimit limit) {
		this.populationLimit = limit;
	}

	/**
	 * Determine whether any of the stopping conditions hold
	 * 
	 * @return a boolean.
	 */
	public boolean isFinished() {
        // logger.error(c + " "+ c.getCurrentValue());
        return stoppingConditions.stream().anyMatch(StoppingCondition::isFinished);
	}

	// TODO: Override equals method in StoppingCondition
	/**
	 * <p>
	 * addStoppingCondition
	 * </p>
	 * 
	 * @param condition
	 *            a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
	 *            object.
	 */
	public void addStoppingCondition(StoppingCondition condition) {
        final boolean contained = stoppingConditions.stream()
                .anyMatch(obj -> obj.getClass().equals(condition.getClass()));

        if (contained) {
            return;
        }

		logger.debug("Adding new stopping condition");
		stoppingConditions.add(condition);
		addListener(condition);
	}
	
	public Set<StoppingCondition> getStoppingConditions() {
		return stoppingConditions;
	}

	// TODO: Override equals method in StoppingCondition
	/**
	 * <p>
	 * setStoppingCondition
	 * </p>
	 * 
	 * @param condition
	 *            a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
	 *            object.
	 */
	public void setStoppingCondition(StoppingCondition condition) {
		stoppingConditions.clear();
		logger.debug("Setting stopping condition");
		stoppingConditions.add(condition);
		addListener(condition);
	}

	/**
	 * <p>
	 * removeStoppingCondition
	 * </p>
	 * 
	 * @param condition
	 *            a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
	 *            object.
	 */
	public void removeStoppingCondition(StoppingCondition condition) {
        final boolean removed = stoppingConditions
				.removeIf(sc -> sc.getClass().equals(condition.getClass()));
        if (removed) removeListener(condition);
	}

	/**
	 * <p>
	 * resetStoppingConditions
	 * </p>
	 */
	public void resetStoppingConditions() {
        stoppingConditions.forEach(StoppingCondition::reset);
	}

	/**
	 * <p>
	 * setStoppingConditionLimit
	 * </p>
	 * 
	 * @param value
	 *            a int.
	 */
	public void setStoppingConditionLimit(int value) {
        stoppingConditions.forEach(c -> c.setLimit(value));
	}

	protected void updateBestIndividualFromArchive() {
		if (!Properties.TEST_ARCHIVE)
			return;

		T best = Archive.getArchiveInstance().mergeArchiveAndSolution(getBestIndividual());

		// The archive may contain tests evaluated with a fitness function
		// that is not part of the optimization (e.g. ibranch secondary objective)
        best.getCoverageValues().keySet().removeIf(ff -> !fitnessFunctions.contains(ff));
		population.add(0, best);
	}

	/**
	 * Returns true if the <code>chromosome1</code> is better or equal than 
	 * <code>chromosome2</code> according to the compound fitness function.
	 * 
	 * @param chromosome1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param chromosome2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a boolean.
	 */
	protected boolean isBetterOrEqual(Chromosome chromosome1, Chromosome chromosome2) {
		// if (fitnessFunction.isMaximizationFunction()) {
		if (getFitnessFunction().isMaximizationFunction()) {
			return chromosome1.compareTo(chromosome2) >= 0;
		} else {
			return chromosome1.compareTo(chromosome2) <= 0;
		}
	}

	/*
	 * protected boolean isBetter(Chromosome chromosome1, Chromosome
	 * chromosome2) { if (fitnessFunction.isMaximizationFunction()) { return
	 * chromosome1.compareTo(chromosome2) > 0; } else { return
	 * chromosome1.compareTo(chromosome2) < 0; } }
	 */

	/**
	 * <p>
	 * getBest
	 * </p>
	 * 
	 * @param chromosome1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param chromosome2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a {@link org.evosuite.ga.Chromosome} object.
	 */
	/*
	 * protected Chromosome getBest(Chromosome chromosome1, Chromosome
	 * chromosome2) { if (isBetterOrEqual(chromosome1, chromosome2)) return
	 * chromosome1; else return chromosome2; }
	 */

	/**
	 * Prints out all information regarding this GAs stopping conditions
	 * 
	 * So far only used for testing purposes in TestSuiteGenerator
	 */
	public void printBudget() {
        final Logger logger = LoggingUtils.getEvoLogger();
        logger.info("* GA-Budget:");
        stoppingConditions.stream()
                .map(sc -> "\t- " + sc.toString())
                .forEach(logger::info);
	}

	/**
	 * <p>
	 * getBudgetString
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getBudgetString() {
        return stoppingConditions.stream()
                .map(StoppingCondition::toString)
                .collect(joining(" "));
	}

	/**
	 * Returns the progress of the search.
	 * 
	 * @return a value [0.0, 1.0]
	 */
	protected double progress() {
		long totalbudget = 0;
		long currentbudget = 0;

		for (StoppingCondition sc : this.stoppingConditions) {
			if (sc.getLimit() != 0) {
				totalbudget += sc.getLimit();
				currentbudget += sc.getCurrentValue();
			}
		}

		return (double) currentbudget / (double) totalbudget;
	}

	/*
	 * private void writeObject(ObjectOutputStream oos) throws IOException { if
	 * (listeners.contains(SearchStatistics.getInstance())) {
	 * removeListener(SearchStatistics.getInstance()); oos.defaultWriteObject();
	 * oos.writeObject(Boolean.TRUE); // Write/save additional fields
	 * oos.writeObject(SearchStatistics.getInstance()); } else {
	 * oos.defaultWriteObject(); oos.writeObject(Boolean.FALSE); } }
	 * 
	 * // assumes "static java.util.Date aDate;" declared private void
	 * readObject(ObjectInputStream ois) throws ClassNotFoundException,
	 * IOException { ois.defaultReadObject(); listeners = new
	 * HashSet<SearchListener>(); stoppingConditions = new
	 * HashSet<StoppingCondition>();
	 * 
	 * boolean addStatistics = (Boolean) ois.readObject(); if (addStatistics) {
	 * SearchStatistics.setInstance((SearchStatistics) ois.readObject());
	 * addListener(SearchStatistics.getInstance()); } }
	 */
}
