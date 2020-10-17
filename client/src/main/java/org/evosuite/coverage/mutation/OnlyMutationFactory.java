/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.mutation;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.TestGenerationContext;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;

public class OnlyMutationFactory extends MutationFactory {

	@Override
	public List<MutationTestFitness> getCoverageGoals() {
		if (this.goals != null) {
			return this.goals;
		}

		this.goals = new ArrayList<>();

		for (Mutation m : MutationPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getMutants()) {
			//if (MutationTimeoutStoppingCondition.isDisabled(m))
			//	continue;
			this.goals.add(new OnlyMutationTestFitness(m));
		}
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Mutants, this.goals.size());

		return this.goals;
	}

}
