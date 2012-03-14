/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class RandomSearch extends GeneticAlgorithm {

	private static Logger logger = LoggerFactory.getLogger(RandomSearch.class);

	/**
	 * @param factory
	 */
	public RandomSearch(ChromosomeFactory<? extends Chromosome> factory) {
		super(factory);
	}

	private static final long serialVersionUID = -7685015421245920459L;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.GeneticAlgorithm#evolve()
	 */
	@Override
	protected void evolve() {
		Chromosome newChromosome = chromosomeFactory.getChromosome();
		fitnessFunction.getFitness(newChromosome);
		notifyEvaluation(newChromosome);
		if (newChromosome.compareTo(getBestIndividual()) <= 0) {
			logger.info("New fitness: " + newChromosome.getFitness());
			population.set(0, newChromosome);
		}
		currentIteration++;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.GeneticAlgorithm#initializePopulation()
	 */
	@Override
	public void initializePopulation() {
		generateRandomPopulation(1);
		calculateFitness();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.GeneticAlgorithm#generateSolution()
	 */
	@Override
	public void generateSolution() {
		notifySearchStarted();
		if (population.isEmpty())
			initializePopulation();

		currentIteration = 0;
		while (!isFinished()) {
			evolve();
			this.notifyIteration();
		}
		notifySearchFinished();
	}

}
