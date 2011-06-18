package de.unisb.cs.st.evosuite.ma;

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * The {@code Connector} Class is used to connect to the main algorithm of
 * EvoSuite and extract all needed information. This is possible by calling static
 * method externalCall.
 * 
 * @author Yury Pavlov
 */
public class Connector {
	/*
	 * If in the next MAX_ITERATION iterations GA delta Fitness <
	 * MIN_DELTA_FITNESS then we must create test for this branch in manual mode
	 */
	private static final double MIN_DELTA_COVERAGE = 0.001;
	private static final int MAX_ITERATION = 500;

	private static int iterCount = 0;
	private static double oldCoverageVal = Double.MAX_VALUE;

	/**
	 * Call this function in GA after each population to check, when we need to
	 * start manual mode
	 * 
	 * @param sa
	 *            - SearchAlgorithm instance to work with it
	 */
	public static void externalCall(GeneticAlgorithm ga) {
		double newCoverageVal = ((TestSuiteChromosome) ga.getBestIndividual()).getCoverage();
		double deltaCoverage = oldCoverageVal - newCoverageVal;
		
		Debug.printDebConn("Iteration: " + iterCount + "\t\tCoverage: " + Double.toString(newCoverageVal));

		/*
		 * Call manual edition when coverage is smaller then 100% and delta is
		 * too small and not change in few iterations
		 */
		if (newCoverageVal > 0)
			if (deltaCoverage < MIN_DELTA_COVERAGE) {
				iterCount++;

				if (iterCount > MAX_ITERATION) {
					Editor editor = new Editor(ga);
					iterCount = 0;
				}

			} else {
				iterCount = 0;
			}

		oldCoverageVal = newCoverageVal;
	}

}
