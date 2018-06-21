package org.evosuite.coverage.epa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.testcase.TestFitnessFunction;
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
