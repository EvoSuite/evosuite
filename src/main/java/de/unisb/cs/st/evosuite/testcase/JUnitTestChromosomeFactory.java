/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.junit.ComplexJUnitTestReader;
import de.unisb.cs.st.evosuite.junit.JUnitTestReader;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class JUnitTestChromosomeFactory implements ChromosomeFactory<TestChromosome> {

	private static final long serialVersionUID = 2760642997019090252L;

	private final TestCase userTest;

	private final ChromosomeFactory<TestChromosome> defaultFactory;

	/**
	 * Attempt to read the test case
	 * 
	 * @param className
	 */
	public JUnitTestChromosomeFactory(String className,
	        ChromosomeFactory<TestChromosome> defaultFactory) {
		this.defaultFactory = defaultFactory;
		JUnitTestReader reader = new ComplexJUnitTestReader(null,
		        new String[] { Properties.SOURCEPATH[0] }); // TODO
		userTest = null; //reader.readJUnitTestCase(SimpleTestExample01.class.getName() + "#test");
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.ChromosomeFactory#getChromosome()
	 */
	@Override
	public TestChromosome getChromosome() {
		double P_delta = 0.1d;
		double P_clone = 0.1d;
		int MAX_CHANGES = 10;

		if (Randomness.nextDouble() < P_clone) {
			return defaultFactory.getChromosome();
		}

		// Cloning
		TestChromosome chromosome = new TestChromosome();
		chromosome.setTestCase(userTest.clone());

		// Delta
		if (Randomness.nextDouble() < P_delta) {
			// TODO: Use decreasing probability like during insertion?
			int numChanges = Randomness.nextInt(1, MAX_CHANGES);
			for (int i = 0; i < numChanges; i++) {
				chromosome.mutate();
			}
		}

		return chromosome;
	}

}
