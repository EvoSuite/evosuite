/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class MutationSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -8320078404661057113L;

	protected final BranchCoverageSuiteFitness branchFitness;

	protected final List<TestFitnessFunction> mutationGoals;

	public static int mostCoveredGoals = 0;

	public MutationSuiteFitness() {
		MutationFactory factory = new MutationFactory(
		        Properties.CRITERION == Criterion.WEAKMUTATION ? false : true);
		mutationGoals = factory.getCoverageGoals();
		logger.info("Mutation goals: " + mutationGoals.size());
		branchFitness = new BranchCoverageSuiteFitness();
	}

	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	public ExecutionResult runTest(TestCase test, Mutation mutant) {

		return MutationTestFitness.runTest(test, mutant);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public abstract double getFitness(Chromosome individual);
}
