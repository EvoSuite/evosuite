package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * Historical concrete TestFitnessFactories only implement the getGoals() method of TestFitnessFactory.
 * Those old Factories can just extend these AstractFitnessFactory to support the new method getFitness()
 * @author Sebastian Steenbuck
 *
 */
public abstract class AbstractFitnessFactory implements TestFitnessFactory{

	@Override
	public double getFitness(TestSuiteChromosome suite){
		
		ExecutionTrace.enableTraceCalls();

		int coveredGoals = 0;
		for (TestFitnessFunction goal : getCoverageGoals()) {
			for (TestChromosome test : suite.getTestChromosomes()) {
				if (goal.isCovered(test)) {
					coveredGoals++;
					break;
				}
			}
		}

		ExecutionTrace.disableTraceCalls();

		return getCoverageGoals().size()-coveredGoals;
	}
}
