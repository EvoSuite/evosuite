package org.evosuite.coverage.epa;

import org.evosuite.Properties;

/**
 * This fitness function counts the degree of normal transitions that do not
 * belong to the EPA automata. It is a minimization function (less is better).
 * The value 0.0 means all transitions were covered.
 *
 * @author galeotti
 */
public class EPAErrorCoverageSuiteFitness extends EPASuiteFitness {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7223594459434843145L;

	public EPAErrorCoverageSuiteFitness(String epaXMLFilename) {
		super(epaXMLFilename);
	}

	@Override
	protected EPAFitnessFactory getGoalFactory(EPA epa) {
		return new EPAErrorCoverageFactory(Properties.TARGET_CLASS, epa);
	}
}
