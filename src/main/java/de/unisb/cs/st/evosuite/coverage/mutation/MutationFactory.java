/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
