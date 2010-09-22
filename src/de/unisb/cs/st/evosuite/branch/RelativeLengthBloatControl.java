/**
 * 
 */
package de.unisb.cs.st.evosuite.branch;

import java.util.List;

import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.ga.BloatControlFunction;
import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.FitnessFunction;
import de.unisb.cs.st.ga.GAProperties;
import de.unisb.cs.st.ga.SearchListener;


/**
 * @author fraser
 *
 */
public class RelativeLengthBloatControl implements BloatControlFunction,
		SearchListener {

	protected int current_max = 0;
	
	protected double best_fitness = Double.MAX_VALUE; // FIXXME: Assuming minimizing fitness!
	
	/** Factor for bloat control */
	protected int bloat_factor = GAProperties.getPropertyOrDefault("bloat_factor", 2);
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.javalanche.ga.BloatControlFunction#isTooLong(de.unisb.cs.st.javalanche.ga.Chromosome)
	 */
	public boolean isTooLong(Chromosome chromosome) {
		// Always accept if fitness is better
		if(chromosome.getFitness() < best_fitness)
			return false;
		
		//logger.debug("Current - max: "+((TestSuiteChromosome)chromosome).length()+" - "+current_max);
		if(current_max > 0) {
		//	if(((TestSuiteChromosome)chromosome).length() > bloat_factor * current_max)
		//		logger.debug("Bloat control: "+((TestSuiteChromosome)chromosome).length() +" > "+ bloat_factor * current_max);

			return ((TestChromosome)chromosome).size() > bloat_factor * current_max;
		}
		else
			return false; // Don't know max length so can't reject!
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.javalanche.ga.SearchListener#fitnessEvaluation(de.unisb.cs.st.javalanche.ga.Chromosome)
	 */
	public void fitnessEvaluation(Chromosome result) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.javalanche.ga.SearchListener#iteration(de.unisb.cs.st.javalanche.ga.Chromosome)
	 */
	public void iteration(List<Chromosome> population) {
		current_max = ((TestChromosome)population.get(0)).size();
		best_fitness = population.get(0).getFitness();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.javalanche.ga.SearchListener#searchFinished(de.unisb.cs.st.javalanche.ga.Chromosome)
	 */
	public void searchFinished(List<Chromosome> best) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.javalanche.ga.SearchListener#searchStarted(de.unisb.cs.st.javalanche.ga.FitnessFunction)
	 */
	public void searchStarted(FitnessFunction objective) {
		// TODO Auto-generated method stub

	}

}
