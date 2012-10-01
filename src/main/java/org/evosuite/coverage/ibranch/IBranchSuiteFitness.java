/**
 * 
 */
package org.evosuite.coverage.ibranch;

import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class IBranchSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -4745892521350308986L;

	private final List<TestFitnessFunction> branchGoals;

	public IBranchSuiteFitness() {
		IBranchFitnessFactory factory = new IBranchFitnessFactory();
		branchGoals = factory.getCoverageGoals();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		double fitness = 0.0;

		AbstractTestSuiteChromosome<ExecutableChromosome> suite = (AbstractTestSuiteChromosome<ExecutableChromosome>) individual;
		List<ExecutionResult> results = runTestSuite(suite);

		return fitness;
	}

}
