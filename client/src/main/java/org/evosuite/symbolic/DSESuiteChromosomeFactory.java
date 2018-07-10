package org.evosuite.symbolic;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

public class DSESuiteChromosomeFactory extends DSEChromosomeFactory<TestSuiteChromosome> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8032905938009367710L;

	private final TestSuiteChromosome singleton;

	public DSESuiteChromosomeFactory(DSETestChromosomeFactory f) {
		singleton = buildDefaultTestSuiteChromosome(f);
	}

	/**
	 * Creates the default test suite chromosome
	 * 
	 * @param f
	 * @return
	 */
	private static TestSuiteChromosome buildDefaultTestSuiteChromosome(DSETestChromosomeFactory f) {
		TestSuiteChromosome defaultTestSuiteChromosome = new TestSuiteChromosome();
		for (int i = 0; i < f.numberOfChromosomes(); i++) {
			TestChromosome testChromosome = f.getChromosome(i);
			defaultTestSuiteChromosome.addTest(testChromosome);
		}
		return defaultTestSuiteChromosome;
	}

	@Override
	public TestSuiteChromosome getChromosome() {
		return singleton.clone();
	}

	@Override
	public int numberOfChromosomes() {
		return 1;
	}

	@Override
	public TestSuiteChromosome getChromosome(int i) throws IndexOutOfBoundsException {
		if (i != 0) {
			throw new IndexOutOfBoundsException();
		}
		return getChromosome();
	}

}
