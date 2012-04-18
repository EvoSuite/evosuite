/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class FixedSizeTestSuiteChromosomeFactory implements
        ChromosomeFactory<TestSuiteChromosome> {

	private static final long serialVersionUID = 6269582177138945987L;

	/** Factory to manipulate and generate method sequences */
	private final ChromosomeFactory<TestChromosome> testChromosomeFactory;

	private final int size;

	public FixedSizeTestSuiteChromosomeFactory(int size) {
		testChromosomeFactory = new RandomLengthTestFactory();
		this.size = size;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.ChromosomeFactory#getChromosome()
	 */
	@Override
	public TestSuiteChromosome getChromosome() {
		TestSuiteChromosome chromosome = new TestSuiteChromosome(
		        new RandomLengthTestFactory());
		chromosome.tests.clear();
		CurrentChromosomeTracker<?> tracker = CurrentChromosomeTracker.getInstance();
		tracker.modification(chromosome);

		for (int i = 0; i < size; i++) {
			TestChromosome test = testChromosomeFactory.getChromosome();
			chromosome.tests.add(test);
		}
		return chromosome;
	}

}
