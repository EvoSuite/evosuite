package de.unisb.cs.st.evosuite.ui.genetics;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.testsuite.CurrentChromosomeTracker;

public class UITestSuiteChromosomeFactory implements ChromosomeFactory<UITestSuiteChromosome> {
	private static final long serialVersionUID = 1L;
	private ChromosomeFactory<UITestChromosome> testFactory;

	public UITestSuiteChromosomeFactory(ChromosomeFactory<UITestChromosome> testFactory) {
		this.testFactory = testFactory;
	}

	@Override
	public UITestSuiteChromosome getChromosome() {
		UITestSuiteChromosome chromosome = new UITestSuiteChromosome(this.testFactory);
		CurrentChromosomeTracker<?> tracker = CurrentChromosomeTracker.getInstance();
		tracker.modification(chromosome);

		// TODO: Change to random number
		for (int i = 0; i < Properties.NUM_TESTS; i++) {
			chromosome.addTest(testFactory.getChromosome());
		}

		return chromosome;
	}
}
