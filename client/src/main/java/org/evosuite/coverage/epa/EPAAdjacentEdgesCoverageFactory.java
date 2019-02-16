package org.evosuite.coverage.epa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.testsuite.AbstractFitnessFactory;

public class EPAAdjacentEdgesCoverageFactory extends AbstractFitnessFactory<EPAAdjacentEdgesCoverageTestFitness> {

	public static long UPPER_BOUND_OF_GOALS;

	public EPAAdjacentEdgesCoverageFactory(EPA epaAutomata) {
		int numOfStates = epaAutomata.getStates().size();
		int numOfActions = epaAutomata.getActions().size();
		int maxNumOfEdges = numOfStates * numOfActions * numOfStates;
		int maxNumOfDepartingEdges = numOfActions * numOfStates;
		UPPER_BOUND_OF_GOALS = (maxNumOfEdges * maxNumOfDepartingEdges) * 2;
	}

	private static Map<String, EPAAdjacentEdgesCoverageTestFitness> goals = new LinkedHashMap<>();

	public static Map<String, EPAAdjacentEdgesCoverageTestFitness> getGoals() {
		return goals;
	}

	@Override
	public List<EPAAdjacentEdgesCoverageTestFitness> getCoverageGoals() {
		return new ArrayList<EPAAdjacentEdgesCoverageTestFitness>(goals.values());
	}

}
