/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove all statements from a test case that do not contribute to the fitness
 * 
 * @author Gordon Fraser
 */
public class TestCaseMinimizer {

	private static final Logger logger = LoggerFactory.getLogger(TestCaseMinimizer.class);

	private final Set<TestFitnessFunction> fitnessFunctions = new HashSet<TestFitnessFunction>();

	/**
	 * Constructor
	 * 
	 * @param fitnessFunctions
	 *            a {@link java.util.Collection} object.
	 */
	public TestCaseMinimizer(Collection<TestFitnessFunction> fitnessFunctions) {
		this.fitnessFunctions.addAll(fitnessFunctions);
	}

	/**
	 * Constructor
	 * 
	 * @param fitnessFunction
	 *            Fitness function with which to measure whether a statement is
	 *            necessary
	 */
	public TestCaseMinimizer(TestFitnessFunction fitnessFunction) {
		this.fitnessFunctions.add(fitnessFunction);
	}

	/**
	 * Remove all unreferenced variables
	 * 
	 * @param t
	 *            The test case
	 * @return True if something was deleted
	 */
	public static boolean removeUnusedVariables(TestCase t) {
		List<Integer> to_delete = new ArrayList<Integer>();
		boolean has_deleted = false;

		int num = 0;
		for (Statement s : t) {
			VariableReference var = s.getReturnValue();
			if (!t.hasReferences(var)) {
				to_delete.add(num);
				has_deleted = true;
			}
			num++;
		}
		Collections.sort(to_delete, Collections.reverseOrder());
		for (Integer position : to_delete) {
			t.remove(position);
		}

		return has_deleted;
	}

	private static boolean isWorse(FitnessFunction<TestChromosome> fitness,
	        TestChromosome oldChromosome, TestChromosome newChromosome) {
		if (fitness.isMaximizationFunction()) {
			if (oldChromosome.getFitness(fitness) > newChromosome.getFitness(fitness))
				return true;
		} else {
			if (newChromosome.getFitness(fitness) > oldChromosome.getFitness(fitness))
				return true;
		}

		for (SecondaryObjective objective : TestChromosome.getSecondaryObjectives()) {
			if (objective.compareChromosomes(oldChromosome, newChromosome) < 0)
				return true;
		}

		return false;
	}

	/**
	 * Calculate the fitness values for all fitness functions in a map.
	 * Returns null if a timeout is reached
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @return a {@link java.util.Map} object.
	 */
	private Map<TestFitnessFunction, Double> getFitnessValues(TestChromosome test) {
		Map<TestFitnessFunction, Double> fitnessMap = new HashMap<TestFitnessFunction, Double>();
		for (TestFitnessFunction fitness : fitnessFunctions) {
			if (isTimeoutReached()) {
				logger.debug("Timeout while computing fitness values");
				return null;
			}
			
			double fit = fitness.getFitness(test);
			fitnessMap.put(fitness, fit);
		}
		return fitnessMap;
	}

	
    private boolean isTimeoutReached() {
        return !TimeController.getInstance().isThereStillTimeInThisPhase();
    }
    
	/**
	 * Central minimization function. Loop and try to remove until all
	 * statements have been checked.
	 * 
	 * @param c
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 */
	public void minimize(TestChromosome c) {
		if (!Properties.MINIMIZE) {
			return;
		}
		logger.info("Minimizing test case");
		//logger.info(c.test.toCode());

		/** Factory method that handles statement deletion */
		TestFactory testFactory = TestFactory.getInstance();

		Map<TestFitnessFunction, Double> fitness = getFitnessValues(c);
		if (fitness==null|| isTimeoutReached()) {
			return;
		}

		logger.debug("Start fitness values: " + fitness);
		assert ConstraintVerifier.verifyTest(c);
		
		if (isTimeoutReached()) {
			logger.debug("Timeout reached after verifying test");
			return;
		}
		
		boolean changed = true;

		while (changed) {
			changed = false;

			for (int i = c.test.size() - 1; i >= 0; i--) {
				if (isTimeoutReached()) {
					logger.debug("Timeout reached before minimizing statement {}", c.test.getStatement(i).getCode());
					return;
				}
				
				logger.debug("Deleting statement {}", c.test.getStatement(i).getCode());
				TestChromosome copy = (TestChromosome) c.clone();
				boolean modified = false;
				try {
					modified = testFactory.deleteStatementGracefully(c.test, i);
				} catch (ConstructionFailedException e) {
					modified = false;
				}
				
				if(!modified){
					c.setChanged(false);
					c.test = copy.test;
					logger.debug("Deleting failed");
					continue;
				}

				c.setChanged(true);

				Map<TestFitnessFunction, Double> newFitness = getFitnessValues(c);
				if (newFitness==null) {
					logger.debug("Keeping original version due to timeout");
					restoreTestCase(c, copy);
					return;
				}
				//double new_fitness = fitnessFunction.getFitness(c);

				boolean isWorse = false;
				for (TestFitnessFunction fitnessFunction : fitnessFunctions) {
					if (isTimeoutReached()) {
						logger.debug("Keeping original version due to timeout");
						restoreTestCase(c, copy);
						return;
					}
					
					if (isWorse(fitnessFunction, copy, c)) {
						isWorse = true;
						break;
					}
					
				}

				if (!isWorse) {
					logger.debug("Keeping shorter version");
					fitness = newFitness;
					changed = true;
					break;
				} else {
					logger.debug("Keeping original version");
					restoreTestCase(c, copy);
				}

			}
		}

		assert ConstraintVerifier.verifyTest(c);

		if (logger.isDebugEnabled()) {
			logger.debug("Minimized test case: ");
			logger.debug(c.test.toCode());
		}

	}

	private static void restoreTestCase(TestChromosome c, TestChromosome copy) {
		c.test = copy.test;
		c.copyCachedResults(copy);
		//c.setFitness(copy.getFitness());
		c.setFitnessValues(copy.getFitnessValues());
		c.setPreviousFitnessValues(copy.getPreviousFitnessValues());
		c.setChanged(false);
	}

}
