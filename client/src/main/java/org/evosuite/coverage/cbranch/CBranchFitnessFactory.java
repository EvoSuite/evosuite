/**
 * 
 */
package org.evosuite.coverage.cbranch;

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
 * @author Gordon Fraser, mattia
 * 
 */
public class CBranchFitnessFactory extends AbstractFitnessFactory<CBranchTestFitness> {

	private static Logger logger = LoggerFactory.getLogger(CBranchFitnessFactory.class);

	/* (non-Javadoc)
	 * @see org.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<CBranchTestFitness> getCoverageGoals() {
		//TODO this creates duplicate goals. Momentary fixed using a Set, but it should be optimised
		Set<CBranchTestFitness> goals = new HashSet<CBranchTestFitness>();

		// retrieve set of branches
		BranchCoverageFactory branchFactory = new BranchCoverageFactory();
		List<BranchCoverageTestFitness> branchGoals = branchFactory.getCoverageGoals();
		CallGraph callGraph = DependencyAnalysis.getCallGraph();

		// try to find all occurrences of this branch in the call tree
		for (BranchCoverageTestFitness branchGoal : branchGoals) {
			logger.info("Adding context branches for " + branchGoal.toString());
			for (CallContext context : callGraph.getMethodEntryPoint(branchGoal.getClassName(),
				                          branchGoal.getMethod())) {

				goals.add(new CBranchTestFitness(branchGoal.getBranchGoal(), context));				
			}
		} 
		
		logger.info("Created " + goals.size() + " goals");
		return new ArrayList<CBranchTestFitness>(goals);
	}
}

