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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Strategy;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Abstract superclass of genetic algorithms
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class GeneticAlgorithm implements SearchAlgorithm, Serializable {

	private static final long serialVersionUID = 5155609385855093435L;

	private static Logger logger = LoggerFactory.getLogger(GeneticAlgorithm.class);

	/** Fitness function to rank individuals */
	protected FitnessFunction fitnessFunction;

	/** Selection function to select parents */
	protected SelectionFunction selectionFunction = new RankSelection();

	/** CrossOver function */
	protected CrossOverFunction crossoverFunction = new SinglePointCrossOver();

	/** Current population */
	protected List<Chromosome> population = new ArrayList<Chromosome>();

	/** Generator for initial population */
	protected ChromosomeFactory<? extends Chromosome> chromosomeFactory;

	/** Listeners */
	protected Set<SearchListener> listeners = new HashSet<SearchListener>();

	/** List of conditions on which to end the search */
	protected Set<StoppingCondition> stoppingConditions = new HashSet<StoppingCondition>();

	/** Bloat control, to avoid too long chromosomes */
	protected Set<BloatControlFunction> bloatControl = new HashSet<BloatControlFunction>();

	/** Local search might need a different local objective */
	protected LocalSearchObjective localObjective;

	/** The population limit decides when an iteration is done */
	protected PopulationLimit populationLimit = new IndividualPopulationLimit();

	/** Age of the population */
	protected int currentIteration = 0;

	/**
	 * Constructor
	 * 
	 * @param factory
	 */
	public GeneticAlgorithm(ChromosomeFactory<? extends Chromosome> factory) {
		chromosomeFactory = factory;
		addStoppingCondition(new MaxGenerationStoppingCondition());
		addListener(new LocalSearchBudget());
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
		logger.debug("Applying local search");
		LocalSearchBudget.localSearchStarted();

		for (Chromosome individual : population) {
			if (isFinished())
				break;

			if (LocalSearchBudget.isFinished())
				break;

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
	 * Apply dynamic symbolic execution
	 */
	protected void applyDSE() {
		logger.info("Applying DSE at generation " + currentIteration);
		DSEBudget.DSEStarted();

		for (Chromosome individual : population) {
			if (isFinished())
				break;

			if (DSEBudget.isFinished())
				break;

			individual.applyDSE(this);
		}
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
		if (fitnessFunction == null)
			return;
		ChromosomeRecycler recycler = ChromosomeRecycler.getInstance();
		Set<Chromosome> recycables = recycler.getRecycableChromosomes(fitnessFunction);
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
			Chromosome individual = chromosomeFactory.getChromosome();
			if (!fitnessFunction.isMaximizationFunction())
				individual.setFitness(Double.MAX_VALUE);
			else
				individual.setFitness(0.0);
			population.add(individual);
			if (isFinished())
				break;
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
		fitnessFunction = function;
		localObjective = new DefaultLocalSearchObjective(function);
	}

	/**
	 * Get currently used fitness function
	 * 
	 * @return
	 */
	public FitnessFunction getFitnessFunction() {
		return fitnessFunction;
	}

	/**
	 * Set new fitness function (i.e., for new mutation)
	 * 
	 * @param function
	 */
	public void setSelectionFunction(SelectionFunction function) {
		selectionFunction = function;
	}

	/**
	 * Get currently used fitness function
	 * 
	 * @return
	 */
	public SelectionFunction getSelectionFunction() {
		return selectionFunction;
	}

	/**
	 * Set new bloat control function
	 * 
	 * @param bloat_control
	 */
	public void setBloatControl(BloatControlFunction bloat_control) {
		this.bloatControl.clear();
		addBloatControl(bloat_control);
	}

	/**
	 * Set new bloat control function
	 * 
	 * @param bloat_control
	 */
	public void addBloatControl(BloatControlFunction bloat_control) {
		this.bloatControl.add(bloat_control);
	}

	/**
	 * Check whether individual is suitable according to bloat control functions
	 */
	public boolean isTooLong(Chromosome chromosome) {
		for (BloatControlFunction b : bloatControl) {
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
		return currentIteration;
	}

	/**
	 * Calculate fitness for all individuals
	 */
	protected void calculateFitness() {
		logger.debug("Calculating fitness for " + population.size() + " individuals");

		Iterator<Chromosome> iterator = population.iterator();
		while (iterator.hasNext()) {
			Chromosome c = iterator.next();
			if (isFinished()) {
				if (c.isChanged())
					iterator.remove();
			} else {
				fitnessFunction.getFitness(c);
				notifyEvaluation(c);
			}
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
			randoms.add(chromosomeFactory.getChromosome());
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
			if (fitnessFunction.isMaximizationFunction())
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
	public void setChromosomeFactory(ChromosomeFactory<? extends Chromosome> factory) {
		chromosomeFactory = factory;
	}

	/**
	 * Set a new xover function
	 * 
	 * @param crossover
	 */
	public void setCrossOverFunction(CrossOverFunction crossover) {
		this.crossoverFunction = crossover;
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

	/**
	 * Notify all search listeners of search start
	 */
	protected void notifySearchStarted() {
		for (SearchListener listener : listeners) {
			listener.searchStarted(this);
		}
	}

	/**
	 * Notify all search listeners of search end
	 */
	protected void notifySearchFinished() {
		for (SearchListener listener : listeners) {
			listener.searchFinished(this);
		}
	}

	/**
	 * Notify all search listeners of iteration
	 */
	protected void notifyIteration() {
		for (SearchListener listener : listeners) {
			listener.iteration(this);
		}
	}

	/**
	 * Notify all search listeners of fitness evaluation
	 */
	protected void notifyEvaluation(Chromosome chromosome) {
		for (SearchListener listener : listeners) {
			listener.fitnessEvaluation(chromosome);
		}
	}

	/**
	 * Notify all search listeners of a mutation
	 */
	protected void notifyMutation(Chromosome chromosome) {
		for (SearchListener listener : listeners) {
			listener.modification(chromosome);
		}
	}

	/**
	 * Sort the population by fitness
	 */
	protected void sortPopulation() {
		if (Properties.SHUFFLE_GOALS)
			Randomness.shuffle(population);

		if (fitnessFunction.isMaximizationFunction()) {
			Collections.sort(population, Collections.reverseOrder());
		} else {
			Collections.sort(population);
		}
	}

	/**
	 * Accessor for population
	 * 
	 * @return
	 */
	public List<Chromosome> getPopulation() {
		return population;
	}

	/**
	 * Determine if the next generation has reached its size limit
	 * 
	 * @param nextGeneration
	 * @return
	 */
	public boolean isNextPopulationFull(List<Chromosome> nextGeneration) {
		return populationLimit.isPopulationFull(nextGeneration);
	}

	/**
	 * Set a new population limit function
	 * 
	 * @param limit
	 */
	public void setPopulationLimit(PopulationLimit limit) {
		this.populationLimit = limit;
	}

	/**
	 * Determine whether any of the stopping conditions hold
	 * 
	 * @return
	 */
	protected boolean isFinished() {
		for (StoppingCondition c : stoppingConditions) {
			if (c.isFinished())
				return true;
		}
		return false;
	}

	// TODO: Override equals method in StoppingCondition
	public void addStoppingCondition(StoppingCondition condition) {
		Iterator<StoppingCondition> it = stoppingConditions.iterator();
		while (it.hasNext()) {
			if (it.next().getClass().equals(condition.getClass())) {
				return;
			}
		}
		logger.debug("Adding new stopping condition");
		stoppingConditions.add(condition);
		addListener(condition);
	}

	// TODO: Override equals method in StoppingCondition
	public void setStoppingCondition(StoppingCondition condition) {
		stoppingConditions.clear();
		logger.debug("Setting stopping condition");
		stoppingConditions.add(condition);
		addListener(condition);
	}

	public void removeStoppingCondition(StoppingCondition condition) {
		Iterator<StoppingCondition> it = stoppingConditions.iterator();
		while (it.hasNext()) {
			if (it.next().getClass().equals(condition.getClass())) {
				it.remove();
				removeListener(condition);
			}
		}
	}

	public void resetStoppingConditions() {
		for (StoppingCondition c : stoppingConditions) {
			c.reset();
		}
	}

	public void setStoppingConditionLimit(int value) {
		for (StoppingCondition c : stoppingConditions) {
			c.setLimit(value);
		}
	}

	protected boolean isBetterOrEqual(Chromosome chromosome1, Chromosome chromosome2) {
		if (fitnessFunction.isMaximizationFunction()) {
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
		for (StoppingCondition sc : stoppingConditions)
			System.out.println("\t- " + sc.toString());
	}

	public String getBudgetString() {
		String r = "";
		for (StoppingCondition sc : stoppingConditions)
			r += sc.toString() + " ";

		return r;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		if (listeners.contains(SearchStatistics.getInstance())) {
			removeListener(SearchStatistics.getInstance());
			oos.defaultWriteObject();
			oos.writeObject(Boolean.TRUE);
			// Write/save additional fields
			oos.writeObject(SearchStatistics.getInstance());
		} else {
			oos.defaultWriteObject();
			oos.writeObject(Boolean.FALSE);
		}
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();
		boolean addStatistics = (Boolean) ois.readObject();
		if (addStatistics) {
			SearchStatistics.setInstance((SearchStatistics) ois.readObject());
			addListener(SearchStatistics.getInstance());
		}
	}

	/**
	 * Set pause before MA
	 */
	public void pauseGlobalTimeStoppingCondition() {
		for (StoppingCondition c : stoppingConditions) {
			if (c instanceof GlobalTimeStoppingCondition) {
				((GlobalTimeStoppingCondition) c).pause();
			}
		}
	}

	/**
	 * Resume from pause after MA
	 */
	public void resumeGlobalTimeStoppingCondition() {
		for (StoppingCondition c : stoppingConditions) {
			if (c instanceof GlobalTimeStoppingCondition) {
				((GlobalTimeStoppingCondition) c).resume();
			}
		}
	}
}
