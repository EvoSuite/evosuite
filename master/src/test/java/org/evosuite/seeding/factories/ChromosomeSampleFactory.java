package org.evosuite.seeding.factories;

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;

public class ChromosomeSampleFactory implements ChromosomeFactory<TestSuiteChromosome> {
	public static final TestSuiteChromosome CHROMOSOME;
	private static final TestSampleFactory FACTORY;
	static {
		FACTORY = new TestSampleFactory();
		CHROMOSOME = new TestSuiteChromosome();
		for (int i = 0; i < 10; i++){
			CHROMOSOME.addTest(FACTORY.getChromosome());
		}
	}
	@Override
	public TestSuiteChromosome getChromosome() {
		// TODO Auto-generated method stub
		return CHROMOSOME.clone();
	}
	
	
}
