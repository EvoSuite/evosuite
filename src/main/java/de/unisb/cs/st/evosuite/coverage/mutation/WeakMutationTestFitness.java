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
package de.unisb.cs.st.evosuite.coverage.mutation;

import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author fraser
 * 
 */
public class WeakMutationTestFitness extends MutationTestFitness {

	private static final long serialVersionUID = 7468742584904580204L;

	/**
	 * @param mutation
	 */
	public WeakMutationTestFitness(Mutation mutation) {
		super(mutation);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.mutation.MutationTestFitness#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;

		double executionDistance = diameter;

		// Get control flow distance
		if (!result.getTrace().touchedMutants.contains(mutation.getId()))
			executionDistance = getExecutionDistance(result);
		else
			executionDistance = 0.0;

		double infectionDistance = 1.0;

		// If executed...
		if (executionDistance <= 0) {
			// Add infection distance
			assert (result.getTrace() != null);
			assert (result.getTrace().mutantDistances != null);
			assert (mutation != null);
			assert (result.getTrace().touchedMutants.contains(mutation.getId()));
			infectionDistance = normalize(result.getTrace().mutantDistances.get(mutation.getId()));
			logger.debug("Infection distance for mutation = " + infectionDistance);
		}

		fitness = infectionDistance + executionDistance;
		logger.debug("Individual fitness: " + " + " + infectionDistance + " + "
		        + executionDistance + " = " + fitness);

		updateIndividual(individual, fitness);
		if (fitness == 0.0) {
			individual.getTestCase().addCoveredGoal(this);
		}
		return fitness;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Weak " + mutation.toString();
	}
}
