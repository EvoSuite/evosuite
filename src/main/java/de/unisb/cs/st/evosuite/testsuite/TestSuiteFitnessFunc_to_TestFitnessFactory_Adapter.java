package de.unisb.cs.st.evosuite.testsuite;

import java.util.List;

import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * This adapters allows the use of a TestSuiteFitnessFunction as a TestFitnessFactory for the purpose of TestSuite minimization.
 * @author Sebastian Steenbuck
 *
 */
public class TestSuiteFitnessFunc_to_TestFitnessFactory_Adapter implements TestFitnessFactory{

	private final TestSuiteFitnessFunction testSuiteFitness;
	public TestSuiteFitnessFunc_to_TestFitnessFactory_Adapter(TestSuiteFitnessFunction testSuiteFitness){
		this.testSuiteFitness=testSuiteFitness;
	}
	
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getFitness(TestSuiteChromosome suite) {
		return testSuiteFitness.getFitness(suite);
	}

}
