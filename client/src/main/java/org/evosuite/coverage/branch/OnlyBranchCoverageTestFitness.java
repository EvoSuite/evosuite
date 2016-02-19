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
package org.evosuite.coverage.branch;

import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * Fitness function for a single test on a single branch
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class OnlyBranchCoverageTestFitness extends TestFitnessFunction {


	private static final long serialVersionUID = -7540212369784578236L;

	/** Target branch */
	private final BranchCoverageGoal goal;

	/**
	 * Constructor - fitness is specific to a branch
	 * 
	 * @param goal
	 *            a {@link org.evosuite.coverage.branch.BranchCoverageGoal}
	 *            object.
	 */
	public OnlyBranchCoverageTestFitness(BranchCoverageGoal goal) throws IllegalArgumentException{
		if(goal == null){
			throw new IllegalArgumentException("goal cannot be null");
		}
		this.goal = goal;
	}

	/**
	 * <p>
	 * getBranch
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.branch.Branch} object.
	 */
	public Branch getBranch() {
		return goal.getBranch();
	}

	public boolean getValue() {
		return goal.getValue();
	}

	public BranchCoverageGoal getBranchGoal() {
		return goal;
	}

	/**
	 * <p>
	 * getClassName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return goal.getClassName();
	}

	/**
	 * <p>
	 * getMethod
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getMethod() {
		return goal.getMethodName();
	}

	/**
	 * <p>
	 * getBranchExpressionValue
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean getBranchExpressionValue() {
		return goal.getValue();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Calculate approach level + branch distance
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		ControlFlowDistance distance = goal.getDistance(result);

		double fitness = distance.getResultingBranchFitness();

		// If there is an undeclared exception it is a failing test
		//if (result.hasUndeclaredException())
		//	fitness += 1;

		logger.debug("Approach level: " + distance.getApproachLevel()
		        + " / branch distance: " + distance.getBranchDistance() + ", fitness = "
		        + fitness);

		updateIndividual(this, individual, fitness);
		return fitness;
	}

	//	@Override
	//	public int getDifficulty() {
	//		if (goal == null)
	//			return 1;
	//		else
	//			return goal.getDifficulty();
	//	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return goal.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OnlyBranchCoverageTestFitness other = (OnlyBranchCoverageTestFitness) obj;
		if (goal == null) {
			if (other.goal != null)
				return false;
		} else if (!goal.equals(other.goal))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof OnlyBranchCoverageTestFitness) {
			OnlyBranchCoverageTestFitness otherOnlyBranchFitness = (OnlyBranchCoverageTestFitness) other;
			return goal.compareTo(otherOnlyBranchFitness.goal);
		}
		return compareClassName(other);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
	 */
	@Override
	public String getTargetClass() {
		return getClassName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		return getMethod();
	}

}
