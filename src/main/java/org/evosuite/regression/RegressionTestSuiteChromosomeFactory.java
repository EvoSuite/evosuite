/**
 * 
 */
package org.evosuite.regression;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testsuite.CurrentChromosomeTracker;
import org.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestSuiteChromosomeFactory implements
        ChromosomeFactory<RegressionTestSuiteChromosome> {

	private static final long serialVersionUID = -5460006842373221807L;

	/** Factory to manipulate and generate method sequences */
	private ChromosomeFactory<RegressionTestChromosome> testChromosomeFactory;

	/**
	 * <p>
	 * Constructor for TestSuiteChromosomeFactory.
	 * </p>
	 */
	public RegressionTestSuiteChromosomeFactory() {
		testChromosomeFactory = new RegressionTestChromosomeFactory();
	}

	/**
	 * <p>
	 * Constructor for TestSuiteChromosomeFactory.
	 * </p>
	 * 
	 * @param testFactory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public RegressionTestSuiteChromosomeFactory(
	        ChromosomeFactory<RegressionTestChromosome> testFactory) {
		testChromosomeFactory = testFactory;
	}

	/**
	 * <p>
	 * setTestFactory
	 * </p>
	 * 
	 * @param factory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public void setTestFactory(ChromosomeFactory<RegressionTestChromosome> factory) {
		testChromosomeFactory = factory;
	}

	/** {@inheritDoc} */
	@Override
	public RegressionTestSuiteChromosome getChromosome() {

		RegressionTestSuiteChromosome chromosome = new RegressionTestSuiteChromosome(
		        new RegressionTestChromosomeFactory());
		chromosome.getTestChromosomes().clear();
		CurrentChromosomeTracker<?> tracker = CurrentChromosomeTracker.getInstance();
		tracker.modification(chromosome);
		// ((AllMethodsChromosomeFactory)test_factory).clear();

		int numTests = Randomness.nextInt(Properties.MIN_INITIAL_TESTS,
		                                  Properties.MAX_INITIAL_TESTS + 1);

		for (int i = 0; i < numTests; i++) {
			RegressionTestChromosome test = testChromosomeFactory.getChromosome();
			chromosome.addTest(test);
			//chromosome.tests.add(test);
		}
		// logger.info("Covered methods: "+((AllMethodsChromosomeFactory)test_factory).covered.size());
		// logger.trace("Generated new test suite:"+chromosome);
		return chromosome;
	}

}
