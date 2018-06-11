package org.evosuite.coverage.epa;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.testsuite.AbstractFitnessFactory;

public class EPAExceptionCoverageFactory extends AbstractFitnessFactory<EPAExceptionCoverageTestFitness> {

	private final EPA epa;

	private final LinkedList<EPAExceptionCoverageTestFitness> goals;

	public EPAExceptionCoverageFactory(String targetClassName, EPA epaAutomata) {
		this.epa = epaAutomata;
		this.goals = buildExceptionCoverageGoals();
	}

	@Override
	public List<EPAExceptionCoverageTestFitness> getCoverageGoals() {
		return goals;
	}

	private LinkedList<EPAExceptionCoverageTestFitness> buildExceptionCoverageGoals() {
		LinkedList<EPAExceptionCoverageTestFitness> goals = new LinkedList<EPAExceptionCoverageTestFitness>();
		Set<EPAState> states = new HashSet<EPAState>(this.epa.getStates());
		states.add(EPAState.INVALID_OBJECT_STATE);

		for (EPAState fromState : states) {
			for (String actionId : this.epa.getActions()) {
				for (EPAState toState : states) {
					EPAExceptionCoverageGoal goal = new EPAExceptionCoverageGoal(Properties.TARGET_CLASS, this.epa,
							fromState, actionId, toState);
					EPAExceptionCoverageTestFitness testFitness = new EPAExceptionCoverageTestFitness(goal);
					goals.add(testFitness);
				}
			}
		}
		return goals;
	}

}
