package de.unisb.cs.st.evosuite.ma;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * The {@code Connector} Class is used to connect to the main algorithm of
 * EvoSuite and extract all needed information. This is possible by calling
 * static method externalCall.
 * 
 * @author Yury Pavlov
 */
public class Connector {
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
		System.out.println("Delta: " + deltaCoverage);
		/*
		 * Call manual edition when coverage is smaller then 100% and delta
		 * is too small and not change in few iterations
		 */
		if (newCoverageVal > 0)
			if (deltaCoverage < Properties.MIN_DELTA_COVERAGE) {
				iterCount++;

				if (iterCount > Properties.MAX_ITERATION) {
					Editor editor = new Editor(ga);
					iterCount = 0;
				}

			} else {
				iterCount = 0;
			}

		oldCoverageVal = newCoverageVal;
	}
}
