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
import org.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class IBranchFitnessFactory extends AbstractFitnessFactory<IBranchTestFitness> {

	/* (non-Javadoc)
	 * @see org.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<IBranchTestFitness> getCoverageGoals() {

		List<IBranchTestFitness> goals = new ArrayList<IBranchTestFitness>();

		// retrieve set of branches
		BranchCoverageFactory branchFactory = new BranchCoverageFactory();
		List<BranchCoverageTestFitness> branchGoals = branchFactory.getCoverageGoals();

		CallTree callTree = DependencyAnalysis.getCallTree();

		// try to find all occurrences of this branch in the call tree
		for (BranchCoverageTestFitness branchGoal : branchGoals) {
			for (CallContext context : callTree.getAllContexts(branchGoal.getClassName(),
			                                                   branchGoal.getMethod())) {
				goals.add(new IBranchTestFitness(branchGoal.getBranch(), context));
			}
		}
		return goals;
	}
}
