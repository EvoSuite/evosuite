/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testsuite;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.testcase.DefaultTestFactory;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteMinimizer {

	/** Logger */
	private final static Logger logger = Logger.getLogger(TestSuiteMinimizer.class);

	/** Factory method that handles statement deletion */
	private final DefaultTestFactory test_factory = DefaultTestFactory.getInstance();

	private final TestFitnessFactory testFitnessFactory;

	public TestSuiteMinimizer(TestFitnessFactory factory) {
		this.testFitnessFactory = factory;
	}

	/**
	 * Minimize test suite with respect to the isCovered Method of the goals defined by the supplied TestFitnessFactory
	 * 
	 * 
	 * @param suite
	 * @param fitness_function
	 */
	public void minimize(TestSuiteChromosome suite) {

		CurrentChromosomeTracker.getInstance().modification(suite);
		Properties.RECYCLE_CHROMOSOMES = false; // TODO: FIXXME!

		// Remove previous results as they do not contain method calls
		// in the case of whole suite generation
		for (ExecutableChromosome test : suite.getTestChromosomes()) {
			test.setChanged(true);
			test.setLastExecutionResult(null);
		}

		boolean size = false;
		String strategy = Properties.SECONDARY_OBJECTIVE;
		if (strategy.contains(":"))
			strategy = strategy.substring(0, strategy.indexOf(':'));
		if (strategy.equals("size"))
			size = true;

		Logger logger1 = Logger.getLogger(TestFitnessFunction.class);
		Level old_level1 = logger.getLevel();
		logger1.setLevel(Level.OFF);
		Logger logger2 = Logger.getLogger(TestSuiteFitnessFunction.class);
		Level old_level2 = logger.getLevel();
		logger2.setLevel(Level.OFF);
		Logger logger3 = Logger.getLogger(DefaultTestFactory.class);
		Level old_level3 = logger.getLevel();
		logger3.setLevel(Level.OFF);

		if (strategy.equals("size")) {
			// If we want to remove tests, start with shortest
			Collections.sort(suite.tests, new Comparator<TestChromosome>() {
				@Override
				public int compare(TestChromosome chromosome1, TestChromosome chromosome2) {
					return chromosome1.size() - chromosome2.size();
				}
			});
		} else if (strategy.equals("maxlength")) {
			// If we want to remove the longest test, start with longest
			Collections.sort(suite.tests, new Comparator<TestChromosome>() {
				@Override
				public int compare(TestChromosome chromosome1, TestChromosome chromosome2) {
					return chromosome2.size() - chromosome1.size();
				}
			});
		}

		// double fitness = fitness_function.getFitness(suite);
		// double coverage = suite.coverage;
		double fitness = 0;

		//if (branch)
		//	fitness = getNumCoveredBranches(suite);
		//else
		//logger.fatal("type:::: " + testFitnessFactory.getClass());
		fitness = testFitnessFactory.getFitness(suite);

		boolean changed = true;
		while (changed) {
			changed = false;
			
			removeEmptyTestCases(suite);

			for (TestChromosome testChromosome : suite.tests) {
				for (int i = testChromosome.size() - 1; i >= 0; i--) {
					logger.debug("Current size: " + suite.size() + "/"
					        + suite.totalLengthOfTestCases());
					logger.debug("Deleting statement "
					        + testChromosome.getTestCase().getStatement(i).getCode() + " from test");
					TestChromosome orgiginalTestChromosome = (TestChromosome) testChromosome.clone();

					try {
						test_factory.deleteStatementGracefully(testChromosome.getTestCase(), i);
						testChromosome.setChanged(true);
					} catch (ConstructionFailedException e) {
						testChromosome.setChanged(false);
						testChromosome.setTestCase(orgiginalTestChromosome.getTestCase());
						logger.debug("Deleting failed");
						continue;
					}
					// logger.debug("Trying: ");
					// logger.debug(test.test.toCode());

					double modifiedVerFitness = 0;
					//if (branch)
					//	new_fitness = getNumCoveredBranches(suite);
					//else
					modifiedVerFitness = testFitnessFactory.getFitness(suite);

					if (Double.compare(modifiedVerFitness, fitness)<=0) {
						fitness = modifiedVerFitness;
						changed = true;
						// 
						// 
						//
						/**
						 * This means, that we try to delete statements equally from each test case (If size is 'false'.) 
						 * The hope is that the median length of the test cases is shorter, as opposed to the average length.
						 */
						if (!size)
							break;
					} else {
						// Restore previous state
						logger.debug("Can't remove statement "
						        + orgiginalTestChromosome.getTestCase().getStatement(i).getCode());
						logger.debug("Restoring fitness from " + modifiedVerFitness + " to "
						        + fitness);
						testChromosome.setTestCase(orgiginalTestChromosome.getTestCase());
						testChromosome.setLastExecutionResult(orgiginalTestChromosome.getLastExecutionResult());
						testChromosome.setChanged(false);
						// suite.setFitness(fitness); // Redo new fitness value
						// determined by fitness function
					}
				}
			}
		}
		// suite.coverage = coverage;
		removeEmptyTestCases(suite);

		//assert (checkFitness(suite) == fitness);

		logger1.setLevel(old_level1);
		logger2.setLevel(old_level2);
		logger3.setLevel(old_level3);
	}
	
	private void removeEmptyTestCases(TestSuiteChromosome suite){
		Iterator<TestChromosome> it = suite.tests.iterator();
		while (it.hasNext()) {
			ExecutableChromosome test = it.next();
			if (test.size() == 0) {
				logger.debug("Removing empty test case");
				it.remove();
			}
		}
	}

}
