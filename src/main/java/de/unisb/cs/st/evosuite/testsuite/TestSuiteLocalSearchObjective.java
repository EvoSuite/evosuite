/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.LocalSearchBudget;
import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteLocalSearchObjective implements LocalSearchObjective {

	private static Logger logger = LoggerFactory.getLogger(TestSuiteLocalSearchObjective.class);

	private final TestSuiteFitnessFunction fitness;

	private final TestSuiteChromosome suite;

	private final int testIndex;

	private double lastFitness;

	private double lastCoverage;

	public TestSuiteLocalSearchObjective(TestSuiteFitnessFunction fitness,
	        TestSuiteChromosome suite, int index) {
		this.fitness = fitness;
		this.suite = suite;
		this.testIndex = index;
		this.lastFitness = suite.getFitness();
		this.lastCoverage = suite.getCoverage();

		/*
		for (TestChromosome test : suite.getTestChromosomes()) {
			test.setChanged(true);
			test.setLastExecutionResult(null);
		}

		double fit = fitness.getFitness(suite);
		assert (fit == this.lastFitness);
		*/
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasImproved(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public boolean hasImproved(Chromosome individual) {
		individual.setChanged(true);
		suite.setTestChromosome(testIndex, (TestChromosome) individual);
		LocalSearchBudget.evaluation();
		double newFitness = fitness.getFitness(suite);
		if (newFitness < lastFitness) { // TODO: Maximize
			logger.info("Local search improved fitness from " + lastFitness + " to "
			        + newFitness);
			lastFitness = newFitness;
			lastCoverage = suite.getCoverage();
			suite.setFitness(lastFitness);
			return true;
		} else {
			suite.setFitness(lastFitness);
			suite.setCoverage(lastCoverage);
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
