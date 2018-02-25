package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.FitnessReplacementFunction;
import org.evosuite.ga.Neighbourhood;
import org.evosuite.ga.ReplacementFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of cellular GA
 * 
 * @author Nasser Albunian
 */
public class CellularGA<T extends Chromosome> extends GeneticAlgorithm<T>{
	
	private static final long serialVersionUID = 7846967347821123201L;
	
	private static final Logger logger = LoggerFactory.getLogger(CellularGA.class);

	/** An object of ReplacementFunction **/
	protected ReplacementFunction replacementFunction;
	
	/** Constructing the neighbourhood **/
	private Neighbourhood<T> neighb;
	
	/** Constructing the temporary grid */
	private List<T> temp_cells = new ArrayList<>();
	
	private static final double DELTA = 0.000000001;
	
	
	public CellularGA (Properties.CGA_Models model, ChromosomeFactory<T> factory){
		
		super(factory);
		
		neighb = new Neighbourhood<T>(Properties.POPULATION);
		
		setReplacementFunction(new FitnessReplacementFunction());
		
		LoggingUtils.getEvoLogger().info("* Running the Cellular GA with the '" + Properties.MODEL + "' neighbourhoods model ");
	}
	
	/**
	 * Launching the algorithm
	 */
	public void run(){

		evolve();

		replacePopulations(population, temp_cells);
		
		for(T individual : population) {
			assert(((TestSuiteChromosome)individual).totalLengthOfTestCases() < Properties.MAX_SIZE * Properties.CHROMOSOME_LENGTH);
		}
		
		updateFitnessFunctionsAndValues();

		currentIteration++;
	}
	
	/**
	 * Evolution process on individuals in the grid
	 */
	public void evolve(){
		// elitism has been shown to positively affect the convergence speed of GAs in various optimisation problems
		temp_cells = this.elitism();
		
		for (int i = 0; i < this.population.size() - temp_cells.size(); i++) {

			List<T> neighbors = neighb.getNeighbors(population, i);
			
			if (getFitnessFunction().isMaximizationFunction()) {
				Collections.sort(neighbors, Collections.reverseOrder());
			} else {
				Collections.sort(neighbors);
			}

			
			List<T> parents = selectionFunction.select(neighbors,2);
			
			T parent1 = parents.get(0);
			T parent2 = parents.get(1);
			
			@SuppressWarnings("unchecked")
			T offspring1 = (T)parent1.clone();
			@SuppressWarnings("unchecked")
			T offspring2 = (T)parent2.clone();
			
			
			try {
				// Crossover
				if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
					crossoverFunction.crossOver(offspring1, offspring2);
				}

			} catch (ConstructionFailedException e) {
				logger.info("CrossOver failed");
				continue;
			}
			
			T bestOffspring = getBestOffspring(offspring1, offspring2);
			
			notifyMutation(bestOffspring);
			bestOffspring.mutate();
			
			if (bestOffspring.isChanged()) {
				bestOffspring.updateAge(currentIteration);
			}
			
			if (bestOffspring.size() > 0 && !isTooLong(bestOffspring))
				temp_cells.add(bestOffspring);
			else
				temp_cells.add(population.get(i));
		}
	}
	
	/**
	 * Replace the current individuals with better individuals in the temporary grid
	 * @param main The main grid
	 * @param temp The temporary grid
	 */
	public void replacePopulations(List<T> main, List<T> temp){
		assert main.size() == temp.size();
		for(int i=0; i<Properties.POPULATION; i++){
			
			T mainIndividual = main.get(i);
			T tempIndividual = temp.get(i);
			
			for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
				fitnessFunction.getFitness(mainIndividual);
				notifyEvaluation(mainIndividual);
				fitnessFunction.getFitness(tempIndividual);
				notifyEvaluation(tempIndividual);
			}
			
			// replace-if-better policy
			if (isBetterOrEqual(tempIndividual, mainIndividual)) {
				if (tempIndividual.size() > 0 && !isTooLong(tempIndividual)) {
					main.set(i, tempIndividual);
				}
			}
		}
	}
	
	/**
	 * Get the best offspring
	 * @param offspring1
	 * @param offspring2
	 * @return better offspring
	 */
	public T getBestOffspring(T offspring1, T offspring2){
		
		for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
			fitnessFunction.getFitness(offspring1);
			notifyEvaluation(offspring1);
			fitnessFunction.getFitness(offspring2);
			notifyEvaluation(offspring2);
		}
		
		if(isBetterOrEqual(offspring1, offspring2))
			return offspring1;
		else
			return offspring2;
	}


	/**
	 * Initialise the population
	 */
	public void initializePopulation() {
		
		notifySearchStarted();
		
		currentIteration = 0;

		generateInitialPopulation(Properties.POPULATION);
		
		logger.debug("Calculating fitness of initial population");
		
		calculateFitnessAndSortPopulation();
		
		this.notifyIteration();
	}


	/**
	 * Generate solution
	 */
	public void generateSolution() {
		
		if (Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER > 0 || Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION) {
			disableFirstSecondaryCriterion();
		}

		if (population.isEmpty()) {
			initializePopulation();
			assert!population.isEmpty() : "Could not create any test";
		}

		logger.debug("Starting evolution");
		int starvationCounter = 0;
		double bestFitness = Double.MAX_VALUE;
		double lastBestFitness = Double.MAX_VALUE;
		if (getFitnessFunction().isMaximizationFunction()) {
			bestFitness = 0.0;
			lastBestFitness = 0.0;
		}

		while (!isFinished()) {
			
			logger.info("Population size before: " + population.size());

			{
				double bestFitnessBeforeEvolution = getBestFitness();
				run();
				sortPopulation();
				double bestFitnessAfterEvolution = getBestFitness();

				if (getFitnessFunction().isMaximizationFunction())
					assert(bestFitnessAfterEvolution >= (bestFitnessBeforeEvolution
							- DELTA)) : "best fitness before evolve()/sortPopulation() was: " + bestFitnessBeforeEvolution
									+ ", now best fitness is " + bestFitnessAfterEvolution;
				else
					assert(bestFitnessAfterEvolution <= (bestFitnessBeforeEvolution
							+ DELTA)) : "best fitness before evolve()/sortPopulation() was: " + bestFitnessBeforeEvolution
									+ ", now best fitness is " + bestFitnessAfterEvolution;
			}

			{
				double bestFitnessBeforeLocalSearch = getBestFitness();
				applyLocalSearch();
				double bestFitnessAfterLocalSearch = getBestFitness();

				if (getFitnessFunction().isMaximizationFunction())
					assert(bestFitnessAfterLocalSearch >= (bestFitnessBeforeLocalSearch
							- DELTA)) : "best fitness before applyLocalSearch() was: " + bestFitnessBeforeLocalSearch
									+ ", now best fitness is " + bestFitnessAfterLocalSearch;
				else
					assert(bestFitnessAfterLocalSearch <= (bestFitnessBeforeLocalSearch
							+ DELTA)) : "best fitness before applyLocalSearch() was: " + bestFitnessBeforeLocalSearch
									+ ", now best fitness is " + bestFitnessAfterLocalSearch;
			}

			double newFitness = getBestFitness();

			if (getFitnessFunction().isMaximizationFunction())
				assert(newFitness >= (bestFitness - DELTA)) : "best fitness was: " + bestFitness
						+ ", now best fitness is " + newFitness;
			else
				assert(newFitness <= (bestFitness + DELTA)) : "best fitness was: " + bestFitness
						+ ", now best fitness is " + newFitness;
			bestFitness = newFitness;

			if (Double.compare(bestFitness, lastBestFitness) == 0) {
				starvationCounter++;
			} else {
				logger.info("reset starvationCounter after " + starvationCounter + " iterations");
				starvationCounter = 0;
				lastBestFitness = bestFitness;

			}

			updateSecondaryCriterion(starvationCounter);

			logger.info("Current iteration: " + currentIteration);
			this.notifyIteration();

			logger.info("Population size: " + population.size());
			logger.info("Best individual has fitness: " + population.get(0).getFitness());
			logger.info("Worst individual has fitness: " + population.get(population.size() - 1).getFitness());

		}
		// archive
		TimeController.execute(this::updateBestIndividualFromArchive, "update from archive", 5_000);

		notifySearchFinished();
	}
	
	/**
	 * Retrieve the fitness
	 * @return fitness of an individual
	 */
	private double getBestFitness() {
		T bestIndividual = getBestIndividual();
		for (FitnessFunction<T> ff : fitnessFunctions) {
			ff.getFitness(bestIndividual);
		}
		return bestIndividual.getFitness();
	}
	
	/**
	 * <p>
	 * setReplacementFunction
	 * </p>
	 * 
	 * @param replacement_function
	 *            a {@link org.evosuite.ga.ReplacementFunction} object.
	 */
	public void setReplacementFunction(ReplacementFunction replacement_function) {
		this.replacementFunction = replacement_function;
	}

	/**
	 * <p>
	 * getReplacementFunction
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.ReplacementFunction} object.
	 */
	public ReplacementFunction getReplacementFunction() {
		return replacementFunction;
	}

}

