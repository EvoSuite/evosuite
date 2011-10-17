/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * @param <T>
 * 
 */
public class TournamentChromosomeFactory<T extends Chromosome> implements
        ChromosomeFactory<T> {

	private static final long serialVersionUID = -2493386206236363431L;

	private static Logger logger = LoggerFactory.getLogger(TournamentChromosomeFactory.class);

	private final FitnessFunction fitnessFunction;

	private final ChromosomeFactory<T> factory;

	private final int tournamentSize = 10;

	public TournamentChromosomeFactory(FitnessFunction fitness,
	        ChromosomeFactory<T> factory) {
		this.fitnessFunction = fitness;
		this.factory = factory;
	}

	/**
	 * This factory produces <i>tournamentSize</i> individuals, and returns the
	 * best one
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T getChromosome() {
		Chromosome bestIndividual = null;
		logger.debug("Starting random generation");
		for (int i = 0; i < tournamentSize; i++) {
			Chromosome candidate = factory.getChromosome();
			fitnessFunction.getFitness(candidate);
			if (bestIndividual == null) {
				bestIndividual = candidate;
			} else if (candidate.compareTo(bestIndividual) <= 0) {
				logger.debug("Old individual has fitness " + bestIndividual.getFitness()
				        + ", replacing with fitness " + candidate.getFitness());
				bestIndividual = candidate;
			}
		}
		logger.debug("Resulting fitness: " + bestIndividual.getFitness());

		assert (bestIndividual != null);

		return (T) bestIndividual;
	}
}
