/**
 * 
 */
package org.evosuite.coverage.ibranch;

import org.evosuite.coverage.branch.Branch;
import org.evosuite.setup.CallContext;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class IBranchTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = -1399396770125054561L;

	private final Branch branch;

	private final CallContext context;

	public IBranchTestFitness(Branch branch, CallContext context) {
		this.branch = branch;
		this.context = context;
	}

	public Branch getBranch() {
		return branch;
	}

	public CallContext getContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getFitness(org.evosuite.testcase.TestChromosome, org.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		// TODO Auto-generated method stub
		return 0;
	}

}
