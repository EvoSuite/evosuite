/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage.concurrency;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * 
 * 
 */
public class ConcurrencyCoverageTestFitness extends TestFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Target branch */
	private final ConcurrencyCoverageGoal goal;

	/**
	 * Constructor - fitness is specific to a branch
	 */
	public ConcurrencyCoverageTestFitness(ConcurrencyCoverageGoal goal) {
		this.goal = goal;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConcurrencyCoverageTestFitness other = (ConcurrencyCoverageTestFitness) obj;
		if (goal == null) {
			if (other.goal != null) {
				return false;
			}
		} else if (!goal.equals(other.goal)) {
			return false;
		}
		return true;
	}

	/**
	 * Calculate approach level + branch distance
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		ConcurrencyDistance distance = goal.getDistance(result, result.getTrace().concurrencyTracer);
		double fitness = 0.0;

		fitness = distance.approachLevel + normalize(distance.branchDistance) + normalize(distance.scheduleDistance);

		logger.debug("Approach level: " + distance.approachLevel + " / branch distance: " + distance.branchDistance
				+ ", fitness = " + fitness);

		updateIndividual(individual, fitness);
		return fitness;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return goal.toString();
	}

	/**
	 * Store information
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}

}
