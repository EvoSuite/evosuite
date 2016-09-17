package org.evosuite.coverage.epa;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class EPATransitionCoverageTestFitness extends TestFitnessFunction {
	
	private final EPATransitionCoverageGoal goal;

	public EPATransitionCoverageTestFitness(EPATransitionCoverageGoal goal) {
		this.goal = goal;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -8090928123309618388L;

	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		
		double fitness = goal.getDistance(result);

		// If there is an undeclared exception it is a failing test
		//if (result.hasUndeclaredException())
		//	fitness += 1;

		updateIndividual(this, individual, fitness);

		return fitness;
	}

	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof EPATransitionCoverageTestFitness) {
			EPATransitionCoverageTestFitness otherEPATransitionFitness = (EPATransitionCoverageTestFitness) other;
			return goal.compareTo(otherEPATransitionFitness.goal);
		}
		return 0;
	}

	@Override
	public String getTargetClass() {
		return goal.getClassName();
	}

	@Override
	public String getTargetMethod() {
		return goal.getMethodName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EPATransitionCoverageTestFitness other = (EPATransitionCoverageTestFitness) obj;
		if (goal == null) {
			if (other.goal != null)
				return false;
		} else if (!goal.equals(other.goal))
			return false;
		return true;
	}
	
	public String toString() {
		return goal.toString();
	}
	
	public String getGoalName() {
		final EPATransition t = goal.getEPATransition();
		return t.getTransitionName();
	}
	
	public boolean isGoalError() {
		return goal.isError();
	}
}
