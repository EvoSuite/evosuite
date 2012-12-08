package org.exsyst.genetics;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testsuite.CurrentChromosomeTracker;
import org.evosuite.testsuite.TestSuiteFitnessFunction;


public class UITestSuiteChromosomeFactory implements ChromosomeFactory<UITestSuiteChromosome> {
	private static final long serialVersionUID = 1L;
	private ChromosomeFactory<UITestChromosome> testFactory;
	private TestSuiteFitnessFunction fitnessFunction;

	public UITestSuiteChromosomeFactory(ChromosomeFactory<UITestChromosome> testFactory, TestSuiteFitnessFunction fitnessFunction) {
		this.testFactory = testFactory;
		this.fitnessFunction = fitnessFunction;
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
		
		// Make sure that chromosomes created by this factory come with pre-computed fitness values
		// (Chromosomes coming out of here without fitness caused trouble when the budget was used
		// up right after creating the initial population... since the genetic algorithm removes all
		// chromosomes which have no associated fitness function after a timeout, the resulting
		// population would then be empty. This fixes that.)
		this.fitnessFunction.getFitness(chromosome);
		// Also Chromosome#setFitness doesn't reset changed to false anymore... weird... do it here.
		chromosome.setChanged(false);
		
		return chromosome;
	}
}
