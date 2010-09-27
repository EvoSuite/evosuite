/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.OUM.OUMTestChromosomeFactory;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.ChromosomeFactory;

/**
 * @author Gordon Fraser
 *
 */
public class TestSuiteChromosomeFactory implements ChromosomeFactory {

	Logger logger = Logger.getLogger(TestSuiteChromosomeFactory.class);
	
	/** Desired number of tests*/
	protected int num_tests   = Properties.getPropertyOrDefault("num_tests", 2);
	
	/** Factory to manipulate and generate method sequences */
	private ChromosomeFactory test_factory;
	
	public TestSuiteChromosomeFactory() {
		String factory_name = Properties.getPropertyOrDefault("test_factory", "Random");
		if(factory_name.equals("OUM"))
			test_factory = new OUMTestChromosomeFactory();
		else
			test_factory = new RandomLengthTestFactory();

//		test_factory = new RandomLengthTestFactory();
//		test_factory = new AllMethodsChromosomeFactory();
//		test_factory = new OUMTestChromosomeFactory();
	}
	
	public TestSuiteChromosomeFactory(ChromosomeFactory test_factory) {
		this.test_factory = test_factory;
	}
	
	public void setTestFactory(ChromosomeFactory factory) {
		test_factory = factory;
	}
	
	public void setNumberOfTests(int num) {
		num_tests = num;
	}
	
	public Chromosome getChromosome() {

		TestSuiteChromosome chromosome = new TestSuiteChromosome();
		chromosome.tests.clear();
		//((AllMethodsChromosomeFactory)test_factory).clear();
		
		// TODO: Change to random number
		for(int i = 0; i < num_tests; i++) {
			TestChromosome test = (TestChromosome) test_factory.getChromosome();
			chromosome.tests.add(test);
		}
//		logger.info("Covered methods: "+((AllMethodsChromosomeFactory)test_factory).covered.size());
		//logger.trace("Generated new test suite:"+chromosome);
		return chromosome;
	}

}
