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

package de.unisb.cs.st.evosuite.testcase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.OUM.OUMTestFactory;
import de.unisb.cs.st.evosuite.assertion.StringTraceExecutionObserver;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;

/**
 * Remove all statements from a test case that do not contribute to the fitness
 * 
 * @author Gordon Fraser
 * 
 */

public class TestCaseMinimizer {

	private static Logger logger = Logger.getLogger(TestCaseMinimizer.class);

	private final TestFitnessFunction fitness_function;

	private double fitness = 0.0;

	private final boolean enabled = Properties.getPropertyOrDefault("minimize",
	                                                                true);

	/**
	 * Constructor
	 * 
	 * @param fitness_function
	 *            Fitness function with which to measure whether a statement is
	 *            necessary
	 */
	public TestCaseMinimizer(TestFitnessFunction fitness_function) {
		this.fitness_function = fitness_function;
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

	/**
	 * Central minimization function. Loop and try to remove until all
	 * statements have been checked.
	 * 
	 * @param c
	 */
	public void minimize(TestChromosome c) {
		if (!enabled) {
			return;
		}
		logger.info("Minimizing test case");
		//logger.info(c.test.toCode());

		Logger logger1 = Logger.getLogger(fitness_function.getClass());
		Level old_level1 = logger.getLevel();
		//logger1.setLevel(Level.OFF);
		Logger logger2 = Logger.getLogger(DefaultTestCase.class);
		Level old_level2 = logger.getLevel();
		logger2.setLevel(Level.OFF);

		Logger logger3 = Logger.getLogger(StringTraceExecutionObserver.class);
		Level old_level3 = logger.getLevel();
		logger3.setLevel(Level.OFF);

		/** Factory method that handles statement deletion */

		AbstractTestFactory test_factory = null;
		String factory_name = Properties.getPropertyOrDefault("test_factory",
		                                                      "Random");
		if (factory_name.equals("OUM"))
			test_factory = OUMTestFactory.getInstance();
		else
			test_factory = DefaultTestFactory.getInstance();

		fitness = fitness_function.getFitness(c);
		logger.debug("Start fitness value: " + fitness);
		boolean changed = true;
		while (changed) {
			changed = false;

			for (int i = c.test.size() - 1; i >= 0; i--) {
				logger.debug("Deleting statement "
				        + c.test.getStatement(i).getCode());
				TestChromosome copy = (TestChromosome) c.clone();
				try {
					c.setChanged(true);
					test_factory.deleteStatementGracefully(c.test, i);
				} catch (ConstructionFailedException e) {
					c.setChanged(true);
					c.test = copy.test;
					logger.debug("Deleting failed");
					continue;
				}

				double new_fitness = fitness_function.getFitness(c);

				logger.debug("New fitness is " + new_fitness);
				if (c.compareTo(copy) <= 0) {
					logger.debug("Keeping shorter version");
					fitness = new_fitness;
					changed = true;
					break;
				} else {
					logger.debug("Keeping original version");
					c.test = copy.test;
					c.last_result = copy.last_result;
					c.setFitness(copy.getFitness());
					c.setChanged(false);
				}
			}

		}
		logger1.setLevel(old_level1);
		logger2.setLevel(old_level2);
		logger3.setLevel(old_level3);
		if (logger.isDebugEnabled()) {
			logger.debug("Minimized test case: ");
			logger.debug(c.test.toCode());
		}

	}

}
