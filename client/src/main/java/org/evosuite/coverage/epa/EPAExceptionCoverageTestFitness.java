package org.evosuite.coverage.epa;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class EPAExceptionCoverageTestFitness extends TestFitnessFunction {

	private final EPAExceptionCoverageGoal goal;

	/**
	 * 
	 */
	private static final long serialVersionUID = 131334423071788377L;

	public EPAExceptionCoverageTestFitness(EPAExceptionCoverageGoal goal) {
		this.goal = goal;
	}

	/**
	 * Returns 0.0 if there is at least one exceptional transition (before an
	 * invalid state is reached) with the same origin state, action and destination
	 * state
	 * 
	 * @param individual
	 * @param result
	 * @return
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = goal.getDistance(result);

		updateIndividual(this, individual, fitness);

		return fitness;

	}

	@Override
	public int compareTo(TestFitnessFunction other) {
		// TODO Auto-generated method stub
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

	public String getGoalName() {
		return goal.getGoalName();
	}

	@Override
	public int hashCode() {
		return this.goal.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof EPAExceptionCoverageTestFitness) {
			EPAExceptionCoverageTestFitness other = (EPAExceptionCoverageTestFitness) obj;
			return this.goal.equals(other.goal);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return goal.toString();
	}
	
	

}
