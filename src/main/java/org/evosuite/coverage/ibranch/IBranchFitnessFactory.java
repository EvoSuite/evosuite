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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class IBranchFitnessFactory extends AbstractFitnessFactory<IBranchTestFitness> {

	private static Logger logger = LoggerFactory.getLogger(IBranchFitnessFactory.class);
	
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
			logger.info("Adding context branches for "+branchGoal.toString());
			for (CallContext context : callTree.getAllContexts(branchGoal.getClassName(),
			                                                   branchGoal.getMethod())) {
				goals.add(new IBranchTestFitness(branchGoal.getBranch(), context));
			}
		}
		return goals;
	}
}
