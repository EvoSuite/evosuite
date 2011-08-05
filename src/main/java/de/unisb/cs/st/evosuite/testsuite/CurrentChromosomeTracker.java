/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author Gordon Fraser
 * 
 */
@SuppressWarnings("unchecked")
//we can't know CType at instantiation type
public class CurrentChromosomeTracker<CType extends Chromosome> implements SearchListener {

	/** The current chromosome */
	private CType currentSuite = null;

	/** Singleton instance */
	private static CurrentChromosomeTracker<?> instance = null;

	/**
	 * Private constructor for singleton
	 */
	private CurrentChromosomeTracker() {

	}

	/**
	 * Singleton accessor
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static CurrentChromosomeTracker<?> getInstance() {
		if (instance == null)
			instance = new CurrentChromosomeTracker();

		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SearchListener#searchStarted(de.unisb.cs.st
	 * .evosuite.ga.FitnessFunction)
	 */
	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#iteration(java.util.List)
	 */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SearchListener#searchFinished(java.util.List)
	 */
	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SearchListener#fitnessEvaluation(de.unisb.
	 * cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SearchListener#mutation(de.unisb.cs.st.evosuite
	 * .ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		currentSuite = (CType) individual;
	}

	public CType getCurrentChromosome() {
		return currentSuite;
	}

	// TODO: This is very inefficient
	public void changed(TestChromosome changed) {
		if (Properties.CALL_PROBABILITY > 0) {
			TestSuiteChromosome suite = (TestSuiteChromosome) currentSuite;
			for (TestChromosome test : suite.tests) {
				if (test == changed || changed.getTestCase() == test.getTestCase())
					continue;
				for (StatementInterface s : test.getTestCase()) {
					if (s instanceof TestCallStatement) {
						TestCallStatement call = (TestCallStatement) s;
						if (call.getTest() != null
						        && call.getTest().equals(changed.getTestCase())) {
							if (!test.isChanged())
								test.setChanged(true);
							break;
						}
					}
				}
			}
		}
	}
}
