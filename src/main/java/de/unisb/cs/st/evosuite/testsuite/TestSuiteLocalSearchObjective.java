/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteLocalSearchObjective implements LocalSearchObjective {

	private final TestSuiteFitnessFunction fitness;

	private final TestSuiteChromosome suite;

	private final int testIndex;

	private double lastFitness;

	public TestSuiteLocalSearchObjective(TestSuiteFitnessFunction fitness,
	        TestSuiteChromosome suite, int index) {
		this.fitness = fitness;
		this.suite = suite;
		this.testIndex = index;
		this.lastFitness = suite.getFitness();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasImproved(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public boolean hasImproved(Chromosome individual) {
		individual.setChanged(true);
		suite.setTestChromosome(testIndex, (TestChromosome) individual);
		double newFitness = fitness.getFitness(suite);
		if (newFitness < lastFitness) { // TODO: Maximize
			lastFitness = newFitness;
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#getFitnessFunction()
	 */
	@Override
	public FitnessFunction getFitnessFunction() {
		return fitness;
	}

}
