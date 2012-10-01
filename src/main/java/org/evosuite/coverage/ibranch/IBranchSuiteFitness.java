/**
 * 
 */
package org.evosuite.coverage.ibranch;

import java.util.List;
import java.util.Map;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class IBranchSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -4745892521350308986L;

	private final List<IBranchTestFitness> branchGoals;

	public IBranchSuiteFitness() {
		IBranchFitnessFactory factory = new IBranchFitnessFactory();
		branchGoals = factory.getCoverageGoals();
	}

	private void determineMinimumDistance(Map<IBranchTestFitness, Double> distances,
	        List<ExecutionResult> results) {

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		double fitness = 0.0;

		List<ExecutionResult> results = runTestSuite(suite);

		// Determine minimum branch distance for each branch in each context
		for (ExecutionResult result : results) {

		}

		return fitness;
	}

}
