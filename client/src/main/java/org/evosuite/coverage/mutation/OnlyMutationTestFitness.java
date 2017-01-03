/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * 
 * @author gordon
 *
 */
public class OnlyMutationTestFitness extends MutationTestFitness {

	private static final long serialVersionUID = -6724941216935595963L;

	public OnlyMutationTestFitness(Mutation m) {
		super(m);
	}
	
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;

		// Get control flow distance
		if (!result.getTrace().wasMutationTouched(mutation.getId()) || result.calledReflection()) {
			fitness = 1.0;
		} else {
			fitness = normalize(result.getTrace().getMutationDistance(mutation.getId()));
			logger.debug("Infection distance for mutation = " + fitness);
		}

		updateIndividual(this, individual, fitness);
		if (fitness == 0.0) {
			individual.getTestCase().addCoveredGoal(this);
		}
		return fitness;	
	}


	@Override
	public String getTargetMethod() {
		return "Weak " + mutation.toString();
	}

}
