package de.unisb.cs.st.evosuite.testcase;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.ChromosomeFactory;
import de.unisb.cs.st.ga.GAProperties;

public class FixedLengthTestChromosomeFactory implements ChromosomeFactory {

	protected static Logger logger = Logger.getLogger(FixedLengthTestChromosomeFactory.class);
	
	/** Attempts before giving up construction */
	protected int max_attempts     = Properties.getPropertyOrDefault("max_attempts", 1000);

	/** Factory to manipulate and generate method sequences */
	private TestFactory test_factory = TestFactory.getInstance();


	/**
	 * Constructor
	 * @param m: Target mutation
	 */
	public FixedLengthTestChromosomeFactory() {
	}
	
	/**
	 * Create a random individual
	 * @param size
	 */
	private TestCase getRandomTestCase(int size) {
		TestCase test = new TestCase();
		int num = 0;
		
		num = 0;
		// Then add random stuff
		while(test.size() < size && num < max_attempts) {
			test_factory.insertRandomStatement(test);
			num++;
		}
		logger.debug("Randomized test case:" + test.toCode());

		return test;
	}
	
	/**
	 * Generate a random chromosome
	 */
	public Chromosome getChromosome() {
		TestChromosome c = new TestChromosome();
		c.test = getRandomTestCase(GAProperties.chromosome_length);
		return c;
	}

}
