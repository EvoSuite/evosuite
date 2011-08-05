/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author fraser
 * 
 */
public class MutationFactory extends AbstractFitnessFactory {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

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
			goals.add(new MutationTestFitness(m));
		}
		return goals;
	}
}
