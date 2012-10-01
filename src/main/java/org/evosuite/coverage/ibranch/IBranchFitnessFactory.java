/**
 * 
 */
package org.evosuite.coverage.ibranch;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.setup.CallContext;
import org.evosuite.setup.CallTree;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class IBranchFitnessFactory extends AbstractFitnessFactory {

	/* (non-Javadoc)
	 * @see org.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {

		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

		// retrieve set of branches
		BranchCoverageFactory branchFactory = new BranchCoverageFactory();
		List<TestFitnessFunction> branchGoals = branchFactory.getCoverageGoals();

		CallTree callTree = DependencyAnalysis.getCallTree();

		// try to find all occurrences of this branch in the call tree
		for (TestFitnessFunction fitnessFunction : branchGoals) {
			BranchCoverageTestFitness branchGoal = (BranchCoverageTestFitness) fitnessFunction;
			for (CallContext context : callTree.getAllContexts(branchGoal.getClassName(),
			                                                   branchGoal.getMethod())) {
				goals.add(new IBranchTestFitness(branchGoal.getBranch(), context));
			}
		}
		return goals;
	}
}
