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
/**
 * 
 */
package org.evosuite.testcase;

import java.util.List;

import org.evosuite.ga.LocalSearchObjective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Local search on the parameters of a function call
 * 
 * 1. null/non-null 2. Other assignable values in test case 3. Type hierarchy
 * 
 * 
 * @author Gordon Fraser
 * 
 */
public class ParameterLocalSearch implements LocalSearch {

	private static Logger logger = LoggerFactory.getLogger(ParameterLocalSearch.class);

	private ExecutionResult oldResult;

	private boolean oldChanged;

	private void backup(ExecutableChromosome test, StatementInterface p) {
		oldResult = test.getLastExecutionResult();
		oldChanged = test.isChanged();
	}

	private void restore(ExecutableChromosome test, StatementInterface p) {
		test.setLastExecutionResult(oldResult);
		test.setChanged(oldChanged);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.LocalSearch#doSearch(de.unisb.cs.st.evosuite.testcase.TestChromosome, int, de.unisb.cs.st.evosuite.ga.LocalSearchObjective)
	 */
	@Override
	public void doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective) {
		StatementInterface stmt = test.getTestCase().getStatement(statement);
		backup(test, stmt);
		if (stmt instanceof MethodStatement) {
			doSearch(test, (MethodStatement) stmt, objective);
		} else if (stmt instanceof ConstructorStatement) {
			doSearch(test, (ConstructorStatement) stmt, objective);
		} else if (stmt instanceof FieldStatement) {
			doSearch(test, (FieldStatement) stmt, objective);
		}
	}

	/**
	 * Go through parameters of method call and apply local search
	 * 
	 * @param test
	 * @param statement
	 * @param objective
	 */
	private void doSearch(TestChromosome test, MethodStatement statement,
	        LocalSearchObjective objective) {
		logger.info("Original test: " + test.getTestCase().toCode());

		if (!statement.isStatic()) {
			logger.info("Replacing callee");
			VariableReference callee = statement.getCallee();
			List<VariableReference> objects = test.getTestCase().getObjects(callee.getType(),
			                                                                statement.getPosition());
			objects.remove(callee);
			boolean done = false;

			for (VariableReference replacement : objects) {
				statement.setCallee(replacement);
				if (objective.hasImproved(test)) {
					done = true;
					backup(test, statement);
					break;
				} else {
					logger.info("Undoing change");
					restore(test, statement);
				}
			}
			if (!done)
				statement.setCallee(callee);
		}

		int numParameter = 0;
		for (VariableReference parameter : statement.getParameterReferences()) {
			logger.info("Replacing parameter " + numParameter);

			// First try null
			statement.replaceParameterReference(new NullReference(test.getTestCase(),
			        parameter.getType()), numParameter);
			logger.info("Resulting test: " + test.getTestCase().toCode());

			// Else try all other values available in the test
			if (!objective.hasImproved(test)) {
				logger.info("Undoing change");
				restore(test, statement);

				statement.replaceParameterReference(parameter, numParameter);
				boolean done = false;

				List<VariableReference> objects = test.getTestCase().getObjects(parameter.getType(),
				                                                                statement.getPosition());
				objects.remove(parameter);
				for (VariableReference replacement : objects) {
					statement.replaceParameterReference(replacement, numParameter);
					logger.info("Resulting test: " + test.getTestCase().toCode());
					if (objective.hasImproved(test)) {
						backup(test, statement);
						done = true;
						break;
					} else {
						logger.info("Undoing change");
						restore(test, statement);
					}
				}
				if (!done)
					statement.replaceParameterReference(parameter, numParameter);

			}
			numParameter++;
		}

	}

	/**
	 * Go through parameters of constructor call and apply local search
	 * 
	 * @param test
	 * @param statement
	 * @param objective
	 */
	private void doSearch(TestChromosome test, ConstructorStatement statement,
	        LocalSearchObjective objective) {
		int numParameter = 0;
		for (VariableReference parameter : statement.getParameterReferences()) {

			// First try null
			statement.replaceParameterReference(new NullReference(test.getTestCase(),
			        parameter.getType()), numParameter);

			// Else try all other values available in the test
			if (!objective.hasImproved(test)) {
				statement.replaceParameterReference(parameter, numParameter);
				boolean done = false;

				List<VariableReference> objects = test.getTestCase().getObjects(parameter.getType(),
				                                                                statement.getPosition());
				objects.remove(parameter);
				for (VariableReference replacement : objects) {
					statement.replaceParameterReference(replacement, numParameter);
					if (objective.hasImproved(test)) {
						done = true;
						break;
					}
				}
				if (!done)
					statement.replaceParameterReference(parameter, numParameter);

			}
			numParameter++;
		}
	}

	/**
	 * Try to replace source of field with all possible choices
	 * 
	 * @param test
	 * @param statement
	 * @param objective
	 */
	private void doSearch(TestChromosome test, FieldStatement statement,
	        LocalSearchObjective objective) {
		if (!statement.isStatic()) {
			VariableReference source = statement.getSource();
			List<VariableReference> objects = test.getTestCase().getObjects(source.getType(),
			                                                                statement.getPosition());
			objects.remove(source);
			boolean done = false;

			for (VariableReference replacement : objects) {
				statement.setSource(replacement);
				if (objective.hasImproved(test)) {
					done = true;
					break;
				}
			}
			if (!done)
				statement.setSource(source);
		}
	}
}
