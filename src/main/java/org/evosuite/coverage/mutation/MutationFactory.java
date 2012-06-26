/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.coverage.mutation;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	 * @see org.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		return getCoverageGoals(null);
	}

	public List<TestFitnessFunction> getCoverageGoals(String targetMethod) {
		if (goals != null)
			return goals;

		goals = new ArrayList<TestFitnessFunction>();

		for (Mutation m : MutationPool.getMutants()) {
			if (targetMethod != null && !m.getMethodName().endsWith(targetMethod))
				continue;

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
