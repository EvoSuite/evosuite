/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author fraser
 * 
 */
public class MutationFactory extends AbstractFitnessFactory {

	private static Logger logger = LoggerFactory.getLogger(MutationFactory.class);

	private boolean strong = true;

	private List<TestFitnessFunction> goals = null;

	public MutationFactory() {
	}

	public MutationFactory(boolean strongMutation) {
		this.strong = strongMutation;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		if (goals != null)
			return goals;

		goals = new ArrayList<TestFitnessFunction>();

		// String targetMethod = Properties.TARGET_METHOD;

		// TODO: What about methods without mutants?
		/*
		// Branchless methods
		String class_name = Properties.TARGET_CLASS;
		for (String method : BranchPool.getBranchlessMethods()) {
			if (targetMethod.equals("") || method.endsWith(targetMethod))
				goals.add(new BranchCoverageTestFitness(new BranchCoverageGoal(
				        class_name, method.substring(method.lastIndexOf(".") + 1))));
		}
		*/
		for (Mutation m : MutationPool.getMutants()) {
			if (MutationTimeoutStoppingCondition.isDisabled(m))
				continue;
			if (strong)
				goals.add(new StrongMutationTestFitness(m));
			else
				goals.add(new WeakMutationTestFitness(m));
		}
		return goals;
	}
}
