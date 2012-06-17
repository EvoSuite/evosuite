/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.testcase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.SecondaryObjective;

/**
 * Remove all statements from a test case that do not contribute to the fitness
 * 
 * @author Gordon Fraser
 * 
 */

public class TestCaseMinimizer {

	private static Logger logger = LoggerFactory.getLogger(TestCaseMinimizer.class);

	private final TestFitnessFunction fitnessFunction;

	private double fitness = 0.0;


	/**
	 * Constructor
	 * 
	 * @param fitnessFunction
	 *            Fitness function with which to measure whether a statement is
	 *            necessary
	 */
	public TestCaseMinimizer(TestFitnessFunction fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}

	/**
	 * Remove all unreferenced variables
	 * 
	 * @param t
	 *            The test case
	 * @return True if something was deleted
	 */
	public boolean removeUnusedVariables(TestCase t) {
		List<Integer> to_delete = new ArrayList<Integer>();
		boolean has_deleted = false;

		int num = 0;
		for (StatementInterface s : t) {
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

	private static boolean isWorse(FitnessFunction fitness, TestChromosome oldChromosome,
	        TestChromosome newChromosome) {
		if (fitness.isMaximizationFunction()) {
			if (oldChromosome.getFitness() > newChromosome.getFitness())
				return true;
		} else {
			if (newChromosome.getFitness() > oldChromosome.getFitness())
				return true;
		}

		for (SecondaryObjective objective : TestChromosome.getSecondaryObjectives()) {
			if (objective.compareChromosomes(oldChromosome, newChromosome) < 0)
				return true;
		}

		return false;
	}

	/**
	 * Central minimization function. Loop and try to remove until all
	 * statements have been checked.
	 * 
	 * @param c
	 */
	public void minimize(TestChromosome c) {
		if (!Properties.MINIMIZE) {
			return;
		}
		logger.info("Minimizing test case");
		//logger.info(c.test.toCode());

		/** Factory method that handles statement deletion */
		AbstractTestFactory testFactory = DefaultTestFactory.getInstance();

		fitness = fitnessFunction.getFitness(c);
		logger.debug("Start fitness value: " + fitness);
		boolean changed = true;
		while (changed) {
			changed = false;

			for (int i = c.test.size() - 1; i >= 0; i--) {
				logger.debug("Deleting statement {}", c.test.getStatement(i).getCode());
				TestChromosome copy = (TestChromosome) c.clone();
				try {
					c.setChanged(true);
					testFactory.deleteStatementGracefully(c.test, i);
				} catch (ConstructionFailedException e) {
					c.setChanged(false);
					c.test = copy.test;
					logger.debug("Deleting failed");
					continue;
				}

				double new_fitness = fitnessFunction.getFitness(c);

				if (!isWorse(fitnessFunction, copy, c)) {
					logger.debug("Keeping shorter version");
					fitness = new_fitness;
					changed = true;
					break;
				} else {
					logger.debug("Keeping original version");
					c.test = copy.test;
					c.copyCachedResults(copy);
					c.setFitness(copy.getFitness());
					c.setChanged(false);
				}
			}

		}

		if (logger.isDebugEnabled()) {
			logger.debug("Minimized test case: ");
			logger.debug(c.test.toCode());
		}

	}

}
