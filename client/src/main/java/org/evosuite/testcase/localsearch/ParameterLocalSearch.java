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
package org.evosuite.testcase.localsearch;

import java.util.List;

import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local search on the parameters of a function call
 * 
 * 1. null/non-null 2. Other assignable values in test case 3. Type hierarchy
 * 
 * @author Gordon Fraser
 */
public class ParameterLocalSearch extends StatementLocalSearch {

	private static final Logger logger = LoggerFactory.getLogger(ParameterLocalSearch.class);

	private ExecutionResult oldResult;

	private boolean oldChanged;

	private void backup(ExecutableChromosome test, Statement p) {
		oldResult = test.getLastExecutionResult();
		oldChanged = test.isChanged();
	}

	private void restore(ExecutableChromosome test, Statement p) {
		test.setLastExecutionResult(oldResult);
		test.setChanged(oldChanged);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.LocalSearch#doSearch(org.evosuite.testcase.TestChromosome, int, org.evosuite.ga.LocalSearchObjective)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean doSearch(TestChromosome test, int statement,
	        LocalSearchObjective<TestChromosome> objective) {
		Statement stmt = test.getTestCase().getStatement(statement);
		backup(test, stmt);
		if (stmt instanceof MethodStatement) {
			return doSearch(test, (MethodStatement) stmt, objective);
		} else if (stmt instanceof ConstructorStatement) {
			return doSearch(test, (ConstructorStatement) stmt, objective);
		} else if (stmt instanceof FieldStatement) {
			return doSearch(test, (FieldStatement) stmt, objective);
		} else {
			return false;
		}
	}

	/**
	 * Go through parameters of method call and apply local search
	 * 
	 * @param test
	 * @param statement
	 * @param objective
	 */
	private boolean doSearch(TestChromosome test, MethodStatement statement,
	        LocalSearchObjective<TestChromosome> objective) {
		logger.info("Original test: " + test.getTestCase().toCode());

		boolean hasImproved = false;

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
					hasImproved = true;
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
						hasImproved = true;
						done = true;
						break;
					} else {
						logger.info("Undoing change");
						restore(test, statement);
					}
				}
				if (!done)
					statement.replaceParameterReference(parameter, numParameter);

			} else {
				hasImproved = true;
			}
			numParameter++;
		}

		return hasImproved;

	}

	/**
	 * Go through parameters of constructor call and apply local search
	 * 
	 * @param test
	 * @param statement
	 * @param objective
	 */
	private boolean doSearch(TestChromosome test, ConstructorStatement statement,
	        LocalSearchObjective<TestChromosome> objective) {
		int numParameter = 0;
		boolean hasImproved = false;

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
						hasImproved = true;
						break;
					}
				}
				if (!done)
					statement.replaceParameterReference(parameter, numParameter);

			} else {
				hasImproved = true;
			}
			numParameter++;
		}

		return hasImproved;
	}

	/**
	 * Try to replace source of field with all possible choices
	 * 
	 * @param test
	 * @param statement
	 * @param objective
	 */
	private boolean doSearch(TestChromosome test, FieldStatement statement,
	        LocalSearchObjective<TestChromosome> objective) {
		if (!statement.isStatic()) {
			VariableReference source = statement.getSource();
			List<VariableReference> objects = test.getTestCase().getObjects(source.getType(),
			                                                                statement.getPosition());
			objects.remove(source);

			for (VariableReference replacement : objects) {
				statement.setSource(replacement);
				if (objective.hasImproved(test)) {
					return true;
				}
			}
			statement.setSource(source);
		}

		return false;
	}
}
