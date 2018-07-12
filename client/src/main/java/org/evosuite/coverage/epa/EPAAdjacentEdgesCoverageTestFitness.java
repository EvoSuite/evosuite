package org.evosuite.coverage.epa;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class EPAAdjacentEdgesCoverageTestFitness extends TestFitnessFunction{

	private static final long serialVersionUID = -7176491037398694311L;
	
	private final EPAAdjacentEdgesCoverageGoal goal;

	public EPAAdjacentEdgesCoverageTestFitness(EPAAdjacentEdgesCoverageGoal goal) {
		this.goal = goal;
	}
	
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = goal.getDistance(result);
		updateIndividual(this, individual, fitness);

		return fitness;
	}

	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof EPAAdjacentEdgesCoverageTestFitness) {
			EPAAdjacentEdgesCoverageTestFitness otherEPAAdjacentEdgesFitness = (EPAAdjacentEdgesCoverageTestFitness) other;
			return goal.compareTo(otherEPAAdjacentEdgesFitness.goal);
		}
		return compareClassName(other);
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
		} else if (obj instanceof EPAAdjacentEdgesCoverageTestFitness) {
			EPAAdjacentEdgesCoverageTestFitness other = (EPAAdjacentEdgesCoverageTestFitness) obj;
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
