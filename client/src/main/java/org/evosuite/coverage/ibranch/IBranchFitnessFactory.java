/**
 * 
 */
package org.evosuite.coverage.ibranch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.setup.CallContext;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.callgraph.CallGraph;
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
		//TODO this creates duplicate goals. Momentary fixed using a Set, but it should be optimised
		Set<IBranchTestFitness> goals = new HashSet<IBranchTestFitness>();

		// retrieve set of branches
		BranchCoverageFactory branchFactory = new BranchCoverageFactory();
		List<BranchCoverageTestFitness> branchGoals = branchFactory.getCoverageGoalsForAllKnownClasses();

		CallGraph callGraph = DependencyAnalysis.getCallGraph();

		// try to find all occurrences of this branch in the call tree
		for (BranchCoverageTestFitness branchGoal : branchGoals) {
			logger.info("Adding context branches for " + branchGoal.toString());
			for (CallContext context : callGraph.getAllContexts(branchGoal.getClassName(),
			                                                   branchGoal.getMethod())) {
				if(context.isEmpty()) continue;
				goals.add(new IBranchTestFitness(branchGoal.getBranchGoal(), context));				
			}
		}

		logger.info("Created " + goals.size() + " goals");
		return new ArrayList<IBranchTestFitness>(goals);
	}
}
