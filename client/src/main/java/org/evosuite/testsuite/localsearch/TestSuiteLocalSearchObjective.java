/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.testsuite.localsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * TestSuiteLocalSearchObjective class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestSuiteLocalSearchObjective implements LocalSearchObjective<TestChromosome> {

	private static final Logger logger = LoggerFactory.getLogger(TestSuiteLocalSearchObjective.class);

	private final List<TestSuiteFitnessFunction> fitnessFunctions = new ArrayList<>();

	private final TestSuiteChromosome suite;
	
	private final int testIndex;

	// TODO: This assumes we are not doing NSGA-II
	private boolean isMaximization = false;
	
	private double lastFitnessSum = 0.0;
	
	private Map<FitnessFunction<?>, Double> lastFitness = new HashMap<>();

	private Map<FitnessFunction<?>, Double> lastCoverage = new HashMap<>();

	/**
	 * <p>
	 * Constructor for TestSuiteLocalSearchObjective.
	 * </p>
	 * 
	 * @param fitness
	 *            a {@link org.evosuite.testsuite.TestSuiteFitnessFunction}
	 *            object.
	 * @param suite
	 *            a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 * @param index
	 *            a int.
	 */
	public TestSuiteLocalSearchObjective(List<TestSuiteFitnessFunction> fitness,
	        TestSuiteChromosome suite, int index) {
		this.fitnessFunctions.addAll(fitness);
		this.suite = suite;
		this.testIndex = index;
		for(TestSuiteFitnessFunction ff : fitness) {
			if(ff.isMaximizationFunction())
				isMaximization = true;
			else
				isMaximization = false;
			break;
		}
		updateLastFitness();
		updateLastCoverage();
	}
	
	public static TestSuiteLocalSearchObjective getTestSuiteLocalSearchObjective(List<FitnessFunction<? extends Chromosome>> fitness,
	        TestSuiteChromosome suite, int index) {
		List<TestSuiteFitnessFunction> ffs = new ArrayList<>();
		for(FitnessFunction<? extends Chromosome> ff : fitness) {
			TestSuiteFitnessFunction tff = (TestSuiteFitnessFunction)ff;
			ffs.add(tff);
		}
		return new TestSuiteLocalSearchObjective(ffs, suite, index);
	}
	
	private void updateLastFitness() {
		lastFitnessSum = 0.0;
		for(TestSuiteFitnessFunction fitness : fitnessFunctions) {
			double newFitness = fitness.getFitness(suite);
			lastFitnessSum += newFitness;
			lastFitness.put(fitness, newFitness);
		}
	}

	private void updateLastCoverage() {
		for(TestSuiteFitnessFunction fitness : fitnessFunctions) {
			lastCoverage.put(fitness, suite.getCoverage(fitness));
		}
	}

	/**
	 * Returns a new TestSuiteLocalSearchObjective. This fresh objective has a 
	 * fresh test suite with only two duplicates of the passed test
	 * 
	 * @param test the test used to create the fresh TestSuiteChromosome
	 * @return
	 */
	public TestSuiteLocalSearchObjective getCopyForTest(TestChromosome test) {
		TestSuiteChromosome s = new TestSuiteChromosome();
		s.addTest(test);
		s.addTest((TestChromosome) test.clone());
		return new TestSuiteLocalSearchObjective(fitnessFunctions, s, 0);
	}

	@Override
	public boolean isDone() {
		
		for(TestSuiteFitnessFunction fitness : fitnessFunctions) {
			if(fitness.isMaximizationFunction() || fitness.getFitness(suite) != 0.0)
				return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#hasImproved(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasImproved(TestChromosome individual) {
		return hasChanged(individual) < 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#hasNotWorsened(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasNotWorsened(TestChromosome individual) {
		return hasChanged(individual) < 1;
	}

	private boolean isFitnessBetter(double newFitness, double oldFitness) {
		if(isMaximization) {
			return newFitness > oldFitness;
		} else {
			return newFitness < oldFitness;
		}
	}
	
	private boolean isFitnessWorse(double newFitness, double oldFitness) {
		if(isMaximization) {
			return newFitness < oldFitness;
		} else {
			return newFitness > oldFitness;
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#hasChanged(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int hasChanged(TestChromosome individual) {
		individual.setChanged(true);
		suite.setTestChromosome(testIndex, individual);
		LocalSearchBudget.getInstance().countFitnessEvaluation();
		for(TestSuiteFitnessFunction fitnessFunction : fitnessFunctions)
			fitnessFunction.getFitness(suite);
		double newFitness = suite.getFitness();
		
		if (isFitnessBetter(newFitness, lastFitnessSum)) {
			logger.info("Local search improved fitness from " + lastFitnessSum + " to "
			        + newFitness);
			updateLastFitness();
			updateLastCoverage();
			return -1;
		} else if (isFitnessWorse(newFitness, lastFitnessSum)) {
			logger.info("Local search worsened fitness from " + lastFitnessSum + " to "
			        + newFitness);
			suite.setFitnessValues(lastFitness);
			suite.setCoverageValues(lastCoverage);
			return 1;
		} else {
			logger.info("Local search did not change fitness of " + lastFitnessSum);

			updateLastCoverage();
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#getFitnessFunction()
	 */
	/** {@inheritDoc} */
	@Override
	public List<FitnessFunction<? extends Chromosome>> getFitnessFunctions() {
		throw new NotImplementedException("This should not be called");

		// return (List<FitnessFunction<? extends Chromosome>>) fitnessFunctions;
	}


	@Override
	public void addFitnessFunction(FitnessFunction<? extends Chromosome> fitness) {
		throw new NotImplementedException("This should not be called");		
	}

	@Override
	public boolean isMaximizationObjective() {
		return isMaximization;
	}

	/**
	 * This method returns the stored suite fitness value. 
	 * @return
	 */
	@Deprecated
	public double getSuiteFitness() {
		return suite.getFitness();
	}
	
	/**
	 * This method updates the fitness values using the list of fitness functions.
	 */
	@Deprecated
	public void updateSuiteFitness() {
		for(TestSuiteFitnessFunction fitnessFunction : fitnessFunctions) {
			fitnessFunction.getFitness(suite);
		}
	}
}
