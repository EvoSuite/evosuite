/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.path;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimePathSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 8301900778876171653L;

	List<TestFitnessFunction> goals;

	public PrimePathSuiteFitness() {
		PrimePathCoverageFactory factory = new PrimePathCoverageFactory();
		goals = factory.getCoverageGoals();
		ExecutionTrace.enableTraceCalls();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		TestSuiteChromosome suite = (TestSuiteChromosome) individual;
		List<ExecutionResult> results = runTestSuite(suite);
		List<TestFitnessFunction> coveredGoals = new ArrayList<TestFitnessFunction>();
		double fitness = 0.0;

		for (TestFitnessFunction goal : goals) {
			double goalFitness = Double.MAX_VALUE;
			for (ExecutionResult result : results) {
				TestChromosome tc = new TestChromosome();
				tc.setTestCase(result.test);
				double resultFitness = goal.getFitness(tc, result);
				if (resultFitness < goalFitness)
					goalFitness = resultFitness;
				if (goalFitness == 0.0) {
					result.test.addCoveredGoal(goal);
					coveredGoals.add(goal);
					break;
				}
			}
			fitness += goalFitness;
		}
		suite.setCoverage(coveredGoals.size() / (double) goals.size());
		updateIndividual(individual, fitness);
		return fitness;
	}
}
