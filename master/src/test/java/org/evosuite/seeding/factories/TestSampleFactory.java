package org.evosuite.seeding.factories;

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testcase.TestChromosome;

public class TestSampleFactory implements ChromosomeFactory<TestChromosome> {

	public static final TestChromosome CHROMOSOME;
	
	static {
		CHROMOSOME = new TestChromosome();
	}
	
	@Override
	public TestChromosome getChromosome() {
		// TODO Auto-generated method stub
		return CHROMOSOME;
	}

}
