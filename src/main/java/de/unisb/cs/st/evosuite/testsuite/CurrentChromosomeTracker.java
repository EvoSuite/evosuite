/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
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

	private final static double CALL_PROBABILITY = Properties.getDoubleValue("call_probability");

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
	public void searchStarted(FitnessFunction objective) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#iteration(java.util.List)
	 */
	@Override
	public void iteration(List<Chromosome> population) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SearchListener#searchFinished(java.util.List)
	 */
	@Override
	public void searchFinished(List<Chromosome> population) {
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
		if (CALL_PROBABILITY > 0) {
			TestSuiteChromosome suite = (TestSuiteChromosome) currentSuite;
			for (TestChromosome test : suite.tests) {
				if (test == changed || changed.test == test.test)
					continue;
				for (StatementInterface s : test.test.getStatements()) {
					if (s instanceof TestCallStatement) {
						TestCallStatement call = (TestCallStatement) s;
						if (call.getTest() != null && call.getTest().equals(changed.test)) {
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
