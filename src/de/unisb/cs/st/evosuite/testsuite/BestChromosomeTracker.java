/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.util.List;

import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.FitnessFunction;
import de.unisb.cs.st.ga.SearchListener;


/**
 * @author Gordon Fraser
 *
 */
public class BestChromosomeTracker implements SearchListener {

	/** The actual value of the best chromosome */
	private TestSuiteChromosome best = null;
	
	/** Singleton instance */
	private static BestChromosomeTracker instance = null;
	
	/**
	 * Private constructor because this is a singleton
	 */
	private BestChromosomeTracker() {
		
	}
	
	/**
	 * Singleton accessor
	 * @return
	 */
	public static BestChromosomeTracker getInstance() {
		if(instance == null)
			instance = new BestChromosomeTracker();
		
		return instance;
	}
	
	/**
	 * The current best individual
	 * @return
	 */
	public TestSuiteChromosome getBest() {
		return best;
	}

	/** 
	 * Simply remember the current best chromosome
	 */
	public void iteration(List<Chromosome> population) {
		this.best = (TestSuiteChromosome) population.get(0);
	}

	public void searchFinished(List<Chromosome> best) { }

	public void searchStarted(FitnessFunction objective) { }

	public void fitnessEvaluation(Chromosome result) { }

}
