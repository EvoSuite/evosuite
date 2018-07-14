package org.evosuite.symbolic;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

public class DSEDefaultSuiteChromosomeFactory extends DSEDefaultChromosomeFactory<TestSuiteChromosome> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8032905938009367710L;

	private final TestSuiteChromosome singleton;

	public DSEDefaultSuiteChromosomeFactory(DSEDefaultTestChromosomeFactory f) {
		singleton = buildDefaultTestSuiteChromosome(f);
	}

	/**
	 * Creates the default test suite chromosome
	 * 
	 * @param f
	 * @return
	 */
	private static TestSuiteChromosome buildDefaultTestSuiteChromosome(DSEDefaultTestChromosomeFactory f) {
		TestSuiteChromosome defaultTestSuiteChromosome = new TestSuiteChromosome();
		for (int i = 0; i < f.numberOfDefaultChromosomes(); i++) {
			TestChromosome testChromosome = f.getDefaultChromosome(i);
			defaultTestSuiteChromosome.addTest(testChromosome);
		}
		return defaultTestSuiteChromosome;
	}

	@Override
	public TestSuiteChromosome getChromosome() {
		return singleton.clone();
	}

	@Override
	public int numberOfDefaultChromosomes() {
		return 1;
	}

	@Override
	public TestSuiteChromosome getDefaultChromosome(int i) throws IndexOutOfBoundsException {
		if (i != 0) {
			throw new IndexOutOfBoundsException();
		}
		return getChromosome();
	}

}
