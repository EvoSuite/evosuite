/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import org.apache.log4j.Logger;


import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.Randomness;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author Gordon Fraser
 *
 */
public class OUMTestChromosomeFactory implements ChromosomeFactory {

	protected static Logger logger = Logger.getLogger(OUMTestChromosomeFactory.class);
	
	/** Attempts before giving up construction */
	protected int max_attempts = Properties.getPropertyOrDefault("max_attempts", 1000);

	/** Factory to manipulate and generate method sequences */
	private OUMTestFactory test_factory = OUMTestFactory.getInstance();
	
	public TestCase getRandomTestCase(int size) {
		TestCase test = new DefaultTestCase();
		int num = 0;
		
		// Choose a random length in 0 - size
		Randomness randomness = Randomness.getInstance();
		int length = randomness.nextInt(size);
		logger.debug("Generating randomized test case of length " + length);
		
		// Then add random stuff
		while(test.size() < length && num < max_attempts) {
			test_factory.insertRandomStatement(test);
			num++;
		}
		if(logger.isDebugEnabled())
			logger.debug("Randomized test case:" + test.toCode());

		return test;
	}
	
	@Override
	public Chromosome getChromosome() {
		TestChromosome c = new TestChromosome();
		c.test = getRandomTestCase(Properties.CHROMOSOME_LENGTH);
		return c;
	}

}
