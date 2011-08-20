/**
 * 
 */
package de.unisb.cs.st.evosuite;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author fraser
 * 
 */
public class ConsoleProgressBar implements SearchListener {

	public static void printProgressBar(int percent, int coverage) {
		StringBuilder bar = new StringBuilder("[Progress:");

		/*
		for (int i = 0; i < 50; i++) {
			if (i < (percent / 2)) {
				bar.append("=");
			} else if (i == (percent / 2)) {
				bar.append(">");
			} else {
				bar.append(" ");
			}
		}
		bar.append("]   " + percent + "%  [Coverage: " + coverage + "%]");
		System.out.print("\r" + bar.toString());
		*/

		for (int i = 0; i < 30; i++) {
			if (i < (int) (percent * 0.30)) {
				bar.append("=");
			} else if (i == (int) (percent * 0.30)) {
				bar.append(">");
			} else {
				bar.append(" ");
			}
		}

		bar.append(Math.min(100, percent) + "%] [Cov:");

		for (int i = 0; i < 35; i++) {
			if (i < (int) (coverage * 0.35)) {
				bar.append("=");
			} else if (i == (int) (coverage * 0.35)) {
				bar.append(">");
			} else {
				bar.append(" ");
			}
		}

		bar.append(coverage + "%]");

		System.out.print("\r" + bar.toString());

	}

	private StoppingCondition stoppingCondition = null;

	private int max = 1;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchStarted(de.unisb.cs.st.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		stoppingCondition = TestSuiteGenerator.getStoppingCondition();
		max = stoppingCondition.getLimit();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#iteration(de.unisb.cs.st.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		int current = stoppingCondition.getCurrentValue();
		printProgressBar(100 * current / max,
		                 (int) Math.round(((TestSuiteChromosome) algorithm.getBestIndividual()).getCoverage() * 100));
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#searchFinished(de.unisb.cs.st.evosuite.ga.GeneticAlgorithm)
	 */
	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		System.out.println("");

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#fitnessEvaluation(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#modification(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

}
