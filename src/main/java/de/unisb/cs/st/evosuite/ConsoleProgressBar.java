/**
 * 
 */
package de.unisb.cs.st.evosuite;

import java.io.Serializable;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class ConsoleProgressBar implements SearchListener, Serializable {

	private static final long serialVersionUID = 7303558940966638158L;

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

	private long max = 1;

	private int currentCoverage = 0;

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
		long current = stoppingCondition.getCurrentValue();
		currentCoverage = (int) Math.floor(((TestSuiteChromosome) algorithm.getBestIndividual()).getCoverage() * 100);
		printProgressBar((int) (100 * current / max), currentCoverage);
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
		long current = stoppingCondition.getCurrentValue();
		printProgressBar((int) (100 * current / max), currentCoverage);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SearchListener#modification(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

}
