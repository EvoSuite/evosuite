package org.evosuite.coverage.epa;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.testcase.execution.ExecutionResult;
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

		for (EPAState fromState : states)
			for (String actionId : this.epa.getActions())
				for (EPAState toState : states)
					for (String actionId_2 : this.epa.getActions())
						for (EPAState toState_2 : states)
						{
							EPAAdjacentEdgesCoverageGoal goal = new EPAAdjacentEdgesCoverageGoal(Properties.TARGET_CLASS, this.epa,
									fromState, actionId, toState, actionId_2, toState_2);
							EPAAdjacentEdgesCoverageTestFitness testFitness = new EPAAdjacentEdgesCoverageTestFitness(goal);
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
		return 0;
	}

}
