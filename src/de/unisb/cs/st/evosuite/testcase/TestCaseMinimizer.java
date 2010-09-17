/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Javalanche is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.ga.ConstructionFailedException;

/**
 * Remove all statements from a test case that do not contribute to the fitness
 * 
 * @author Gordon Fraser
 *
 */

public class TestCaseMinimizer {

	Logger logger = Logger.getLogger(TestCaseMinimizer.class);
	
	TestFitnessFunction fitness_function;
	
	double fitness = 0.0;
	
	boolean enabled = Properties.getPropertyOrDefault("minimize", true);
	
	/**
	 * Constructor
	 * 
	 * @param fitness_function
	 *   Fitness function with which to measure whether a statement is necessary
	 */
	public TestCaseMinimizer(TestFitnessFunction fitness_function) {
		this.fitness_function = fitness_function;
	}
	
	/**
	 * Remove all unreferenced variables
	 * @param t
	 *   The test case
	 * @return
	 *   True if something was deleted
	 */
	public boolean removeUnusedVariables(TestCase t) {
		List<Integer> to_delete = new ArrayList<Integer>();
		boolean has_deleted = false;
		
		int num = 0;
		for(Statement s : t.statements) {
			VariableReference var = s.getReturnValue();
			if(!t.hasReferences(var)) {
				to_delete.add(num);
				has_deleted = true;			
			}
			num++;
		}
		Collections.sort(to_delete, Collections.reverseOrder());
		for(Integer position : to_delete) {
			t.remove(position);
		}
		
		return has_deleted;
	}

	/**
	 * Remove all unreferenced primitive variables
	 * @param t
	 *   The test case
	 * @return
	 *   True if something was removed
	 */
	private boolean removeUnusedPrimitives(TestCase t) {
		List<Integer> to_delete = new ArrayList<Integer>();
		boolean has_deleted = false;
		
		int num = 0;
		for(Statement s : t.statements) {
			if(s instanceof PrimitiveStatement<?>) {
				VariableReference var = s.getReturnValue();
				if(!t.hasReferences(var)) {
					to_delete.add(num);
					has_deleted = true;			
				}
			}
			num++;
		}
		Collections.sort(to_delete, Collections.reverseOrder());
		for(Integer position : to_delete) {
			t.remove(position);
		}
		
		return has_deleted;
	}

	/**
	 * Attempt removal of a statement.
	 * If the statement can be removed without reducing fitness, then the change is kept.
	 * 
	 * @param c
	 *   Chromosome representing the test case
	 * @param position
	 *   Position of the statement to be removed
	 * @return
	 *   True if statement was deleted
	 */
	private boolean removeStatement(TestChromosome c, int position) {
		logger.info("Trying to remove "+position);
		TestChromosome copy = (TestChromosome) c.clone();
		copy.test.remove(position);
		removeUnusedPrimitives(copy.test);
		//logger.info("Potential test case is:");
		//logger.info(copy.test.toCode());
		double new_fitness = fitness_function.getFitness(copy);
		//logger.info("This change affects the fitness: "+fitness+" -> "+new_fitness);
		if(new_fitness - fitness >= -0.03) {
			logger.info("Keeping reduced version: "+fitness+"/"+new_fitness);
			c.test = copy.test;
			//logger.info("Test case after removal:");
			//logger.info(c.test.toCode());
			fitness = new_fitness;
			removeUnusedPrimitives(c.test);
			return true;
		}
		logger.info("Keeping original version: "+fitness+"/"+new_fitness);
		return false;
	}
	
	/**
	 * Central minimization function. Loop and try to remove until all statements have been checked.
	 * @param c
	 */
	public void minimize(TestChromosome c) {
		if(!enabled) {
			removeUnusedPrimitives(c.test);
			return;
		}
//		logger.info("Minimizing test case for mutation "+fitness_function.current_mutation.getId());
		logger.info("Minimizing test case");
		//logger.info(c.test.toCode());
			
		Logger logger1 = Logger.getLogger(fitness_function.getClass());
		Level old_level1 = logger.getLevel();
		//logger1.setLevel(Level.OFF);
		Logger logger2 = Logger.getLogger(TestCase.class);
		Level old_level2 = logger.getLevel();
		logger2.setLevel(Level.OFF);
		
		
		Logger logger3 = Logger.getLogger(StringTraceExecutionObserver.class);
		Level old_level3 = logger.getLevel();
		logger3.setLevel(Level.OFF);
		
		/** Factory method that handles statement deletion */
		TestFactory test_factory = TestFactory.getInstance();

		removeUnusedPrimitives(c.test);
		fitness = fitness_function.getFitness(c);
		boolean changed = true;
		while(changed) {
			int position = 0;
			changed = false;
			/*
			for(Statement i : c.test.statements) {
				if(!c.test.hasReferences(i.getReturnValue())) {
					logger.info("Statement is not referenced: "+i.getCode());

					changed = removeStatement(c, position);
					*/
					/*
					double fitness_without = fitnessWithout(c, position);
					if(fitness_without >= fitness) {
						logger.info("Removing unused statement "+i.retval.statement);
						logger.info("Test case before removal:");
						logger.info(c.test.toCode());
						c.test.remove(position);
						logger.info("Test case after removal:");
						logger.info(c.test.toCode());
						removeUnusedPrimitives(c.test);
						logger.info("Test case after removing primitives:");
						logger.info(c.test.toCode());
						changed = true;
						fitness = fitness_without;
					}
					*/
			/*
				}
				if(changed)
					break;
				
				position++;
			}
			*/
			for(int i = c.test.size() - 1; i>=0; i--) {
				logger.debug("Deleting statement "+c.test.getStatement(i).getCode());
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
				//logger.info("This change affects the fitness: "+fitness+" -> "+new_fitness);
				if(new_fitness <= fitness) {
					fitness = new_fitness;
					changed = true;
					break;
				} else {
					c.test = copy.test;
					c.last_result = copy.last_result;
					c.setChanged(false);
				}
			}
			
		}
		logger1.setLevel(old_level1);
		logger2.setLevel(old_level2);
		logger3.setLevel(old_level3);
	}
	
}
