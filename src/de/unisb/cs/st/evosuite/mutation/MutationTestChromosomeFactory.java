package de.unisb.cs.st.evosuite.mutation;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFactory;
import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.ChromosomeFactory;
import de.unisb.cs.st.ga.GAProperties;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

public class MutationTestChromosomeFactory implements ChromosomeFactory {

	protected static Logger logger = Logger.getLogger(MutationTestChromosomeFactory.class);
	
	/** Mutation we are trying to kill */
	protected Mutation target;
	
	/** Attempts before giving up construction */
	protected int max_attempts     = Integer.parseInt(System.getProperty("GA.max_attempts"));

	/** Factory to manipulate and generate method sequences */
	private TestFactory test_factory = TestFactory.getInstance();

	/**
	 * Constructor
	 * @param m: Target mutation
	 */
	public MutationTestChromosomeFactory(Mutation m) {
		target = m;
	}

	/**
	 * Constructor
	 * @param m: Target mutation
	 */
	public MutationTestChromosomeFactory() {
		target = null;
	}
	
	/**
	 * Create a random individual
	 * @param size
	 */
	private TestCase getRandomTestCase(int size) {
		TestCase test = new TestCase();
		int num = 0;
		
//		List<AccessibleObject> calls = test_factory.getMutantMethods();
				
		// Always start with a constructor
		/*
		while(test.size() == 0 && num < max_attempts) {
			test_factory.addRandomConstructor(test);
			num++;
		}
		*/
		
		// Now add a mutant call
		/*
		int last_size = test.size();
		while(test.size() == last_size) {
			int num_call = randomness.nextInt(calls.size());
			AccessibleObject call = calls.get(num_call);
			test_factory.addCall(test, call);
		}
		*/

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
