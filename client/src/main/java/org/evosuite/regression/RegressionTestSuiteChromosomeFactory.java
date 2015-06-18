/**
 * 
 */
package org.evosuite.regression;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testsuite.CurrentChromosomeTracker;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.factories.TestSuiteChromosomeFactory;
import org.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestSuiteChromosomeFactory extends TestSuiteChromosomeFactory {

	private static final long serialVersionUID = -5460006842373221807L;

	/** Factory to manipulate and generate method sequences */

	/** {@inheritDoc} */
	@Override
	public TestSuiteChromosome getChromosome() {

		RegressionTestSuiteChromosome chromosome = new RegressionTestSuiteChromosome(testChromosomeFactory);
		
		chromosome.clearTests();
		CurrentChromosomeTracker<?> tracker = CurrentChromosomeTracker.getInstance();
		tracker.modification(chromosome);
		// ((AllMethodsChromosomeFactory)test_factory).clear();

		int numTests = Randomness.nextInt(Properties.MIN_INITIAL_TESTS,
		                                  Properties.MAX_INITIAL_TESTS + 1);

		for (int i = 0; i < numTests; i++) {
			TestChromosome test = testChromosomeFactory.getChromosome();
			chromosome.addTest(test);
			//chromosome.tests.add(test);
		}
		// logger.info("Covered methods: "+((AllMethodsChromosomeFactory)test_factory).covered.size());
		// logger.trace("Generated new test suite:"+chromosome);
		return chromosome;
	}

}
