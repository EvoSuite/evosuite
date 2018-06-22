package org.evosuite.coverage.epa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.testsuite.AbstractFitnessFactory;

public class EPAMiningCoverageFactory extends AbstractFitnessFactory<EPAMiningCoverageTestFitness> {

    private static Map<String, EPAMiningCoverageTestFitness> goals = new LinkedHashMap<>();

    public static Map<String, EPAMiningCoverageTestFitness> getGoals() {
        return goals;
    }

    /** {@inheritDoc} */
	@Override
	public List<EPAMiningCoverageTestFitness> getCoverageGoals() {
		return new ArrayList<EPAMiningCoverageTestFitness>(goals.values());
	}
}
