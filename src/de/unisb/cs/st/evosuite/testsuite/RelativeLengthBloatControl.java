/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import org.apache.log4j.Logger;

import de.unisb.cs.st.ga.BloatControlFunction;
import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.FitnessFunction;
import de.unisb.cs.st.ga.GAProperties;
import de.unisb.cs.st.ga.SearchListener;

/**
 * @author Gordon Fraser
 *
 */
public class RelativeLengthBloatControl implements BloatControlFunction, SearchListener {

	Logger logger = Logger.getLogger(BloatControlFunction.class);
	
	/**
	 * Longest individual in current generation
	 */
	protected int current_max = 0;
	
	protected double best_fitness = Double.MAX_VALUE; // FIXXME: Assuming minimizing fitness!
	
	/** Factor for bloat control */
	protected int bloat_factor = Integer.parseInt(GAProperties.getPropertyOrDefault("GA.bloat_factor", "2"));

	/**
	 * Reject individuals that are larger than twice the length of the current best individual
	 */
	public boolean isTooLong(Chromosome chromosome) {
		
		// Always accept if fitness is better
		if(chromosome.getFitness() < best_fitness)
			return false;
		
		//logger.debug("Current - max: "+((TestSuiteChromosome)chromosome).length()+" - "+current_max);
		if(current_max > 0) {
		//	if(((TestSuiteChromosome)chromosome).length() > bloat_factor * current_max)
		//		logger.debug("Bloat control: "+((TestSuiteChromosome)chromosome).length() +" > "+ bloat_factor * current_max);

			return ((TestSuiteChromosome)chromosome).length() > bloat_factor * current_max;
		}
		else
			return false; // Don't know max length so can't reject!
				
	}

	/**
	 * Set current max length to max of best chromosome
	 */
	public void iteration(Chromosome best) {
		current_max = ((TestSuiteChromosome)best).length();
		best_fitness = best.getFitness();
	}

	public void searchFinished(Chromosome result) {
	}

	public void searchStarted(FitnessFunction objective) {
	}

	public void fitnessEvaluation(Chromosome result) {	
	}
}
