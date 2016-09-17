package org.evosuite.coverage.epa;

import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;

import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This fitness function counts the degree of covered transitions. It is a
 * minimization function (less is better). The value 0.0 means all transitions
 * were covered.
 *
 * @author galeotti
 */
public class EPAErrorSuiteFitness extends EPASuiteFitness {

	public EPAErrorSuiteFitness(String epaXMLFilename) {
		super(epaXMLFilename);
	}

	@Override
	protected EPAFitnessFactory getGoalFactory(EPA epa) {
		return new EPAErrorFactory(Properties.TARGET_CLASS, epa);
	}
}
