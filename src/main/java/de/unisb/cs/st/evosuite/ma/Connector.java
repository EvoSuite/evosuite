package de.unisb.cs.st.evosuite.ma;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * The <code>Connector</code> class is used to connect the manual editor to the
 * {@link GeneticAlgorithm} of EvoSuite. It computes all necessary informations.
 * Just call the static method {@link #externalCall} from EvoSuite.
 * <p>
 * Create an instance of the {@link #Editor} class if {@code iterCount} reach the
 * predefined ({@link Properties#MA_MAX_ITERATIONS}) value.
 * 
 * @see Properties#MA_ACTIVE
 * @see Properties#MA_BRANCHES_CALC
 * @see Properties#MA_TARGET_COVERAGE
 * @see Properties#MA_WIDE_GUI
 * @see Properties#MA_MIN_DELTA_COVERAGE
 * 
 * @author Yury Pavlov
 */
public class Connector {

	// Counter of iterations
	private static int iterCount = 0;

	// Coverage from the last iteration
	private static double oldCoverage = 0;

	/**
	 * Call this function in the {@link GeneticAlgorithm} after each population to check, when we must
	 * start the manual editor.
	 * 
	 * @param ga - {@link GeneticAlgorithm}
	 */
	public static void externalCall(GeneticAlgorithm ga) {
		double newCoverage = ((TestSuiteChromosome) ga.getBestIndividual()).getCoverage();
		double deltaCoverage = newCoverage - oldCoverage;
		/*
		 * Call manual edition when coverage is smaller then 100% and delta is
		 * too small and not change in few iterations
		 */
		if (deltaCoverage <= Properties.MA_MIN_DELTA_COVERAGE) {
			iterCount++;

			if (iterCount >= Properties.MA_MAX_ITERATIONS) {
				new Editor(ga);
				iterCount = 0;
			}

		} else {
			iterCount = 0;
		}

		if (Properties.MA_TARGET_COVERAGE <= newCoverage * 100) {
			new Editor(ga);
			iterCount = 0;
			// Deactivate
			Properties.MA_TARGET_COVERAGE = 101;
		}

		oldCoverage = newCoverage;
	}

}
