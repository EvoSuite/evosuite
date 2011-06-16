/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * GA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * GA. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Strategy;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Abstract superclass of genetic algorithms
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class GeneticAlgorithm implements SearchAlgorithm, Serializable {

	private static final long serialVersionUID = 5155609385855093435L;

	protected static Logger logger = Logger.getLogger(GeneticAlgorithm.class);

	/**
	 * Fitness function to rank individuals
	 */
	protected FitnessFunction fitness_function;

	/**
	 * Selection function to select parents
	 */
	protected SelectionFunction selection_function = new RankSelection();

	/**
	 * CrossOver function
	 */
	protected CrossOverFunction crossover_function = new SinglePointCrossOver();

	/**
	 * Current population
	 */
	protected List<Chromosome> population = new ArrayList<Chromosome>();

	/**
	 * Generator for initial population
	 */
	protected ChromosomeFactory<? extends Chromosome> chromosome_factory;

	/**
	 * Listeners
	 */
	protected Set<SearchListener> listeners = new HashSet<SearchListener>();

	/**
	 * List of conditions on which to end the search
	 */
	protected Set<StoppingCondition> stopping_conditions = new HashSet<StoppingCondition>();

	/**
	 * Bloat control, to avoid too long chromosomes
	 */
	protected Set<BloatControlFunction> bloat_control = new HashSet<BloatControlFunction>();

	/** Secondary objectives used during replacement */
	protected final List<SecondaryObjective> secondaryObjectives = new ArrayList<SecondaryObjective>();

	/** Local search might need a different local objective */
	protected LocalSearchObjective localObjective;

	private final boolean shuffleBeforeSort = Properties.SHUFFLE_GOALS;

	/**
	 * Age of the population
	 */
	protected int current_iteration = 0;

	/**
	 * Constructor
	 * 
	 * @param factory
	 */
	public GeneticAlgorithm(ChromosomeFactory<? extends Chromosome> factory) {
		chromosome_factory = factory;
		addStoppingCondition(new MaxGenerationStoppingCondition());
		// addBloatControl(new MaxSizeBloatControl());
	}

	/**
	 * Generate one new generation
	 */
	protected abstract void evolve();

	/**
	 * Local search is only applied every X generations
	 * 
	 * @return
	 */
	protected boolean shouldApplyLocalSearch() {
		if (Properties.LOCAL_SEARCH_RATE <= 0)
			return false;

		return (getAge() % Properties.LOCAL_SEARCH_RATE == 0);
	}

	/**
	 * Apply local search
	 */
	protected void applyLocalSearch() {
		logger.info("Applying local search");
		for (Chromosome individual : population) {
			individual.localSearch(localObjective);
		}
	}

	/**
	 * DSE is only applied every X generations
	 * 
	 * @return
	 */
	protected boolean shouldApplyDSE() {
		if (Properties.DSE_RATE <= 0)
			return false;

		return (getAge() % Properties.DSE_RATE == 0);
	}

	/**
	 * Apply local search
	 */
	protected void applyDSE() {
		logger.info("Applying DSE");
		getBestIndividual().applyDSE();
		//		for (Chromosome individual : population) {
		//			individual.applyDSE();
		//		}
	}

	/**
	 * Set up initial population
	 */
	public abstract void initializePopulation();

	/**
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
	 */
	protected void generateInitialPopulation(int population_size) {
		boolean recycle = Properties.RECYCLE_CHROMOSOMES;
		// FIXME: Possible without reference to strategy?
		if (Properties.STRATEGY == Strategy.EVOSUITE) // recycling only makes sense for single test generation
			recycle = false;
		if (recycle)
			recycleChromosomes(population_size);

		generateRandomPopulation(population_size - population.size());
		// TODO: notifyIteration? calculateFitness?
	}

	/**
	 * Adds to the current population all chromosomes that had a good
	 * performance on a goal that was similar to the current fitness_function.
	 * 
	 * For more information look at ChromosomeRecycler and
	 * TestFitnessFunction.isSimilarTo()
	 */
	protected void recycleChromosomes(int population_size) {
		if (fitness_function == null)
			return;
		ChromosomeRecycler recycler = ChromosomeRecycler.getInstance();
		Set<Chromosome> recycables = recycler.getRecycableChromosomes(fitness_function);
		for (Chromosome recycable : recycables) {
			population.add(recycable);
		}
		double enforced_Randomness = Properties.INITIALLY_ENFORCED_RANDOMNESS;
		if (enforced_Randomness < 0.0 || enforced_Randomness > 1.0) {
			logger.warn("property \"initially_enforced_Randomness\" is supposed to be a percentage in [0.0,1.0]");
			logger.warn("retaining to default");
			enforced_Randomness = 0.4;
		}
		enforced_Randomness = 1 - enforced_Randomness;
		population_size *= enforced_Randomness;
		starveToLimit(population_size);
	}

	/**
	 * This method can be used to kick out chromosomes when the population is
	 * possibly overcrowded
	 * 
	 * Depending on the Property "starve_by_fitness" chromosome are either
	 * kicked out randomly or according to their fitness
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
	 */
	protected void starveByFitness(int limit) {
		calculateFitness();
		for (int i = population.size() - 1; i >= limit; i--) {
			population.remove(i);
		}
	}

	/**
	 * Generate random population of given size
	 * 
	 * @param population_size
	 */
	protected void generateRandomPopulation(int population_size) {
		logger.debug("Creating random population");
		for (int i = 0; i < population_size; i++) {
			population.add(chromosome_factory.getChromosome());
		}
		logger.debug("Created " + population.size() + " individuals");
	}

	/**
	 * Delete all current individuals
	 */
	public void clearPopulation() {
		logger.debug("Resetting population");
		population.clear();
	}

	/**
	 * Set new fitness function (i.e., for new mutation)
	 * 
	 * @param function
	 */
	public void setFitnessFunction(FitnessFunction function) {
		fitness_function = function;
		localObjective = new DefaultLocalSearchObjective(function);
	}

	/**
	 * Get currently used fitness function
	 * 
	 * @return
	 */
	public FitnessFunction getFitnessFunction() {
		return fitness_function;
	}

	/**
	 * Set new fitness function (i.e., for new mutation)
	 * 
	 * @param function
	 */
	public void setSelectionFunction(SelectionFunction function) {
		selection_function = function;
	}

	/**
	 * Get currently used fitness function
	 * 
	 * @return
	 */
	public SelectionFunction getSelectionFunction() {
		return selection_function;
	}

	/**
	 * Set new bloat control function
	 * 
	 * @param bloat_control
	 */
	public void setBloatControl(BloatControlFunction bloat_control) {
		this.bloat_control.clear();
		addBloatControl(bloat_control);
	}

	/**
	 * Set new bloat control function
	 * 
	 * @param bloat_control
	 */
	public void addBloatControl(BloatControlFunction bloat_control) {
		this.bloat_control.add(bloat_control);
	}

	/**
	 * Check whether individual is suitable according to bloat control functions
	 */
	public boolean isTooLong(Chromosome chromosome) {
		for (BloatControlFunction b : bloat_control) {
			if (b.isTooLong(chromosome))
				return true;
		}
		return false;
	}

	/**
	 * Get number of iterations
	 * 
	 * @return Number of iterations
	 */
	public int getAge() {
		return current_iteration;
	}

	/**
	 * Calculate fitness for all individuals
	 */
	protected void calculateFitness() {
		logger.debug("Calculating fitness for " + population.size() + " individuals");

		for (Chromosome c : population) {
			fitness_function.getFitness(c);
			notifyEvaluation(c);
		}

		// Sort population
		sortPopulation();
	}

	/**
	 * Copy best individuals
	 * 
	 * @return
	 */
	protected List<Chromosome> elitism() {
		logger.debug("Elitism");

		List<Chromosome> elite = new ArrayList<Chromosome>();

		for (int i = 0; i < Properties.ELITE; i++) {
			logger.trace("Copying individual " + i + " with fitness "
			        + population.get(i).getFitness());
			elite.add(population.get(i).clone());
		}
		logger.trace("Done.");
		return elite;
	}

	/**
	 * Create random individuals
	 * 
	 * @return
	 */
	protected List<Chromosome> randomism() {
		logger.debug("Randomism");

		List<Chromosome> randoms = new ArrayList<Chromosome>();

		for (int i = 0; i < Properties.ELITE; i++) {
			randoms.add(chromosome_factory.getChromosome());
		}
		return randoms;
	}

	/**
	 * Penalty if individual is not unique
	 * 
	 * @param individual
	 * @param generation
	 */
	protected void kinCompensation(Chromosome individual, List<Chromosome> generation) {

		if (Properties.KINCOMPENSATION >= 1.0)
			return;

		boolean unique = true;

		for (Chromosome other : generation) {
			if (other == individual)
				continue;

			if (other.equals(individual)) {
				unique = false;
				break;
			}
		}

		if (!unique) {
			logger.debug("Applying kin compensation");
			if (selection_function.maximize)
				individual.setFitness(individual.getFitness()
				        * Properties.KINCOMPENSATION);
			else
				individual.setFitness(individual.getFitness()
				        * (2.0 - Properties.KINCOMPENSATION));
		}
	}

	/**
	 * Return the individual with the highest fitness
	 * 
	 * @return
	 */
	public Chromosome getBestIndividual() {
		// Assume population is sorted
		return population.get(0);
	}

	/**
	 * Set a new factory method
	 * 
	 * @param factory
	 */
	public void setChromosomeFactory(ChromosomeFactory<Chromosome> factory) {
		chromosome_factory = factory;
	}

	public void setCrossOverFunction(CrossOverFunction crossover) {
		this.crossover_function = crossover;
	}

	/**
	 * Add a new search listener
	 * 
	 * @param listener
	 */
	public void addListener(SearchListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a search listener
	 * 
	 * @param listener
	 */
	public void removeListener(SearchListener listener) {
		listeners.remove(listener);
	}

	protected void notifySearchStarted() {
		for (SearchListener listener : listeners) {
			listener.searchStarted(this);
		}
	}

	protected void notifySearchFinished() {
		for (SearchListener listener : listeners) {
			listener.searchFinished(this);
		}
	}

	protected void notifyIteration() {
		for (SearchListener listener : listeners) {
			listener.iteration(this);
		}
	}

	protected void notifyEvaluation(Chromosome chromosome) {
		for (SearchListener listener : listeners) {
			listener.fitnessEvaluation(chromosome);
		}
	}

	protected void notifyMutation(Chromosome chromosome) {
		for (SearchListener listener : listeners) {
			listener.modification(chromosome);
		}
	}

	protected void sortPopulation() {
		if (shuffleBeforeSort)
			Randomness.shuffle(population);

		if (selection_function.maximize) {
			Collections.sort(population, Collections.reverseOrder());
		} else {
			Collections.sort(population);
		}
	}

	public List<Chromosome> getPopulation() {
		return population;
	}

	protected boolean isFinished() {
		for (StoppingCondition c : stopping_conditions) {
			if (c.isFinished())
				return true;
		}
		return false;
	}

	// TODO: Override equals method in StoppingCondition
	public void addStoppingCondition(StoppingCondition condition) {
		Iterator<StoppingCondition> it = stopping_conditions.iterator();
		while (it.hasNext()) {
			if (it.next().getClass().equals(condition.getClass())) {
				return;
			}
		}
		logger.debug("Adding new stopping condition");
		stopping_conditions.add(condition);
		addListener(condition);
	}

	// TODO: Override equals method in StoppingCondition
	public void setStoppingCondition(StoppingCondition condition) {
		stopping_conditions.clear();
		logger.debug("Setting stopping condition");
		stopping_conditions.add(condition);
		addListener(condition);
	}

	public void removeStoppingCondition(StoppingCondition condition) {
		Iterator<StoppingCondition> it = stopping_conditions.iterator();
		while (it.hasNext()) {
			if (it.next().getClass().equals(condition.getClass())) {
				it.remove();
				removeListener(condition);
			}
		}
	}

	public void resetStoppingConditions() {
		for (StoppingCondition c : stopping_conditions) {
			c.reset();
		}
	}

	public void setStoppingConditionLimit(int value) {
		for (StoppingCondition c : stopping_conditions) {
			c.setLimit(value);
		}
	}

	/**
	 * Add an additional secondary objective to the end of the list of
	 * objectives
	 * 
	 * @param objective
	 */
	public void addSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.add(objective);
	}

	/**
	 * Remove secondary objective from list, if it is there
	 * 
	 * @param objective
	 */
	public void removeSecondaryObjective(SecondaryObjective objective) {
		secondaryObjectives.remove(objective);
	}

	public void clearSecondaryObjectives() {
		secondaryObjectives.clear();
		Chromosome.clearSecondaryObjectives();
	}

	protected boolean isBetterOrEqual(Chromosome chromosome1, Chromosome chromosome2) {
		if (selection_function.isMaximize()) {
			return chromosome1.compareTo(chromosome2) >= 0;
		} else {
			return chromosome1.compareTo(chromosome2) <= 0;
		}

	}

	protected Chromosome getBest(Chromosome chromosome1, Chromosome chromosome2) {
		if (isBetterOrEqual(chromosome1, chromosome2))
			return chromosome1;
		else
			return chromosome2;
	}

	/**
	 * Prints out all information regarding this GAs stopping conditions
	 * 
	 * So far only used for testing purposes in TestSuiteGenerator
	 */
	public void printBudget() {
		System.out.println("* GA-Budget:");
		for (StoppingCondition sc : stopping_conditions)
			System.out.println("  - " + sc.toString());
	}
}
