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
/**
 * 
 */
package org.evosuite.coverage.mutation;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * <p>
 * WeakMutationTestFitness class.
 * </p>
 * 
 * @author fraser
 */
public class WeakMutationTestFitness extends MutationTestFitness {

	private static final long serialVersionUID = 7468742584904580204L;

	/**
	 * <p>
	 * Constructor for WeakMutationTestFitness.
	 * </p>
	 * 
	 * @param mutation
	 *            a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public WeakMutationTestFitness(Mutation mutation) {
		super(mutation);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.coverage.mutation.MutationTestFitness#getFitness(org.evosuite.testcase.TestChromosome, org.evosuite.testcase.ExecutionResult)
	 */
	/** {@inheritDoc} */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;

		double executionDistance = diameter;

		// Get control flow distance
		if (!result.getTrace().wasMutationTouched(mutation.getId()))
			executionDistance = getExecutionDistance(result);
		else
			executionDistance = 0.0;

		double infectionDistance = 1.0;

		// If executed, but not with reflection
		if (executionDistance <= 0 && !result.calledReflection()) {
			if(executionDistance < 0) {
				logger.warn("Execution distance less than 0! "+mutation);
				assert(false) : "Invalid execution distance on mutation "+mutation;
				executionDistance = 0.0;
			}
			// Add infection distance
			assert (result.getTrace() != null);
			// assert (result.getTrace().mutantDistances != null);
			assert (result.getTrace().wasMutationTouched(mutation.getId()));
			assert (result.getTrace().getMutationDistance(mutation.getId()) >= 0) : "Infection distance less than 0: " + mutation;
			infectionDistance = normalize(result.getTrace().getMutationDistance(mutation.getId()));
			logger.debug("Infection distance for mutation = " + infectionDistance);
		}

		fitness = infectionDistance + executionDistance;
		logger.debug("Individual fitness: " + " + " + infectionDistance + " + "
		        + executionDistance + " = " + fitness);

		updateIndividual(this, individual, fitness);
		if (fitness == 0.0) {
			individual.getTestCase().addCoveredGoal(this);
		}
		return fitness;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Weak " + mutation.toString();
	}

}
