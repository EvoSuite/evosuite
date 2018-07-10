package org.evosuite.coverage.epa;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testsuite.TestSuiteChromosome;

public class EPAAdjacentEdgesCoverageFactory implements TestFitnessFactory<EPAAdjacentEdgesCoverageTestFitness> {

	private final EPA epa;
	private final LinkedList<EPAAdjacentEdgesCoverageTestFitness> goals;

	public EPAAdjacentEdgesCoverageFactory(EPA epaAutomata) {
		this.epa = epaAutomata;
		this.goals = buildCoverageGoals();
	}

	private LinkedList<EPAAdjacentEdgesCoverageTestFitness> buildCoverageGoals() {
		LinkedList<EPAAdjacentEdgesCoverageTestFitness> goals = new LinkedList<EPAAdjacentEdgesCoverageTestFitness>();
		Set<EPAState> states = new HashSet<EPAState>(this.epa.getStates());

		/*
		 * fromState -> firstActionId -> middleState -> secondActionId -> toState
		 */
		for (EPAState fromState : states)
			for (String firstActionId : this.epa.getActions())
				for (EPAState middleState : states)
					for (String secondActionId : this.epa.getActions())
						for (EPAState toState : states) {
							EPAAdjacentEdgesCoverageGoal goal = new EPAAdjacentEdgesCoverageGoal(
									Properties.TARGET_CLASS, this.epa, fromState, firstActionId, middleState,
									secondActionId, toState);
							EPAAdjacentEdgesCoverageTestFitness testFitness = new EPAAdjacentEdgesCoverageTestFitness(
									goal);
							goals.add(testFitness);
						}
		return goals;
	}

	@Override
	public List<EPAAdjacentEdgesCoverageTestFitness> getCoverageGoals() {
		return goals;
	}

	@Override
	public double getFitness(TestSuiteChromosome suite) {

		ExecutionTracer.enableTraceCalls();

		int coveredGoals = 0;
		for (EPAAdjacentEdgesCoverageTestFitness goal : getCoverageGoals()) {
			for (TestChromosome test : suite.getTestChromosomes()) {
				if (goal.isCovered(test)) {
					coveredGoals++;
					break;
				}
			}
		}

		ExecutionTracer.disableTraceCalls();

		return getCoverageGoals().size() - coveredGoals;

	}

}
