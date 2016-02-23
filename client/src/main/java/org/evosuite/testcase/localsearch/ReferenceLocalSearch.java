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

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 *
 */
public class ReferenceLocalSearch extends StatementLocalSearch {

	private static final Logger logger = LoggerFactory.getLogger(ReferenceLocalSearch.class);

	private int positionDelta = 0;

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.LocalSearch#getPositionDelta()
	 */
	@Override
	public int getPositionDelta() {
		return positionDelta;
	}


	private enum Mutations {
		REPLACE, PARAMETER, CALL
	};

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.LocalSearch#doSearch(org.evosuite.testcase.TestChromosome, int, org.evosuite.ga.LocalSearchObjective)
	 */
	@Override
	public boolean doSearch(TestChromosome test, int statement,
	        LocalSearchObjective<TestChromosome> objective) {

		boolean hasImproved = false;
		int currentProbe = 0;

		backup(test);
		int oldLength = test.size();

		while (currentProbe < Properties.LOCAL_SEARCH_PROBES
		        && !LocalSearchBudget.getInstance().isFinished()) {
			logger.info("Current probe on statement " + statement + ": " + currentProbe);

			List<Mutations> mutations = new ArrayList<Mutations>();
			mutations.add(Mutations.REPLACE);
			Statement st = test.getTestCase().getStatement(statement);
			if (!st.getReturnValue().isPrimitive() && !(st instanceof NullStatement)) {
				mutations.add(Mutations.CALL);
			}
			if (st.getNumParameters() > 0) {
				mutations.add(Mutations.PARAMETER);
			} else {
				mutations.remove(Mutations.PARAMETER);
			}

			int delta = 0;

			Mutations m = Randomness.choice(mutations);
			//logger.debug("Test before mutation: {}", test.getTestCase().toCode());

			switch (m) {
			case REPLACE:
				replace(test, statement);
				if (test.size() > oldLength)
					delta = test.size() - oldLength;
				break;
			case PARAMETER:
				changeParameters(test, statement);
				break;
			case CALL:
				addCall(test, statement);
				break;
			}

			if (test.isChanged()) {
				logger.info("Is changed");
				// logger.info("Test after mutation: " + test.getTestCase().toCode());
				if (objective.hasImproved(test)) {
					logger.info("Fitness has improved, keeping");

					currentProbe = 0;
					hasImproved = true;
					backup(test);
					statement += delta;
					positionDelta += delta;
					oldLength = test.size();
				} else {
					logger.info("Fitness has not improved, reverting");

					currentProbe++;
					restore(test);
				}
			} else {
				logger.info("Is not changed");
				currentProbe++;
			}

		}

		return hasImproved;
	}

	/**
	 * Add a method call on the return value of the object at position statement
	 *
	 * @param test
	 * @param statement
	 * @param objective
	 */
	private boolean addCall(TestChromosome test, int statement) {

		logger.debug("Adding call");

		TestFactory factory = TestFactory.getInstance();
		Statement theStatement = test.getTestCase().getStatement(statement);
		VariableReference var = theStatement.getReturnValue();

		int oldLength = test.size();
		factory.insertRandomCallOnObjectAt(test.getTestCase(), var, statement + 1);
		test.setChanged(test.size() != oldLength);

		return false;
	}

	/**
	 * Replace the call with a completely different call
	 *
	 * @param test
	 * @param statement
	 * @param objective
	 * @return
	 */
	private boolean replace(TestChromosome test, int statement) {

		logger.debug("Replacing call");

		TestFactory factory = TestFactory.getInstance();
		Statement theStatement = test.getTestCase().getStatement(statement);
		VariableReference var = theStatement.getReturnValue();
		int oldLength = test.size();
		try {
			VariableReference replacement = null;
			if (Randomness.nextDouble() < Properties.NULL_PROBABILITY) {
				NullStatement nullStatement = new NullStatement(test.getTestCase(),
				        var.getType());
				replacement = test.getTestCase().addStatement(nullStatement, statement);
			} else if(!var.isPrimitive()) {
				// Test cluster does not keep track of generators for primitives
				replacement = factory.createObject(test.getTestCase(), var.getType(),
				                                   statement, 0, null);
			}
			if(replacement != null) {
				int oldStatement = statement + (test.size() - oldLength);
				for (int i = oldStatement + 1; i < test.size(); i++) {
					test.getTestCase().getStatement(i).replace(var, replacement);
				}
				factory.deleteStatement(test.getTestCase(), oldStatement);
				test.setChanged(true);
			}

		} catch (ConstructionFailedException e) {
			if (test.size() < oldLength) {
				restore(test);
			}
			test.setChanged(test.size() != oldLength);
		}

		return false;
	}

	/**
	 * Switch parameter/callee variables with other available objects
	 *
	 * @param test
	 * @param statement
	 * @param objective
	 * @return
	 */
	private boolean changeParameters(TestChromosome test, int statement) {
		logger.debug("Changing parameters");
		Statement stmt = test.getTestCase().getStatement(statement);
		if (stmt instanceof MethodStatement) {
			return replaceMethodParameter(test, (MethodStatement) stmt);
		} else if (stmt instanceof ConstructorStatement) {
			return replaceConstructorParameter(test, (ConstructorStatement) stmt);
		} else if (stmt instanceof FieldStatement) {
			return replaceFieldSource(test, (FieldStatement) stmt);
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
	private boolean replaceMethodParameter(TestChromosome test, MethodStatement statement) {

		List<VariableReference> parameters = statement.getParameterReferences();
		if (parameters.isEmpty())
			return false;

		int max = parameters.size();
		if (!statement.isStatic()) {
			max++;
		}
		int numParameter = Randomness.nextInt(max);
		if (numParameter == parameters.size()) {
			// replace callee
			VariableReference callee = statement.getCallee();
			List<VariableReference> objects = test.getTestCase().getObjects(callee.getType(),
			                                                                statement.getPosition());
			objects.remove(callee);
			if (objects.isEmpty())
				return false;

			VariableReference replacement = Randomness.choice(objects);
			statement.setCallee(replacement);
			test.setChanged(true);

		} else {
			VariableReference parameter = parameters.get(numParameter);
			List<VariableReference> objects = test.getTestCase().getObjects(parameter.getType(),
			                                                                statement.getPosition());
			objects.remove(parameter);
			objects.remove(statement.getReturnValue());
			NullStatement nullStatement = new NullStatement(test.getTestCase(),
			        parameter.getType());
			if (!parameter.isPrimitive())
				objects.add(nullStatement.getReturnValue());

			if (objects.isEmpty())
				return false;

			VariableReference replacement = Randomness.choice(objects);
			if (replacement == nullStatement.getReturnValue()) {
				test.getTestCase().addStatement(nullStatement, statement.getPosition());
			}
			statement.replaceParameterReference(replacement, numParameter);
			test.setChanged(true);

		}

		return false;
	}

	/**
	 * Go through parameters of constructor call and apply local search
	 *
	 * @param test
	 * @param statement
	 * @param objective
	 */
	private boolean replaceConstructorParameter(TestChromosome test,
	        ConstructorStatement statement) {

		List<VariableReference> parameters = statement.getParameterReferences();
		if (parameters.isEmpty())
			return false;

		int numParameter = Randomness.nextInt(parameters.size());
		VariableReference parameter = parameters.get(numParameter);

		List<VariableReference> objects = test.getTestCase().getObjects(parameter.getType(),
		                                                                statement.getPosition());
		objects.remove(parameter);
		objects.remove(statement.getReturnValue());

		NullStatement nullStatement = new NullStatement(test.getTestCase(),
		        parameter.getType());
		if (!parameter.isPrimitive())
			objects.add(nullStatement.getReturnValue());

		if (objects.isEmpty())
			return false;

		VariableReference replacement = Randomness.choice(objects);
		if (replacement == nullStatement.getReturnValue()) {
			test.getTestCase().addStatement(nullStatement, statement.getPosition());
		}

		statement.replaceParameterReference(replacement, numParameter);
		test.setChanged(true);

		return false;
	}

	/**
	 * Try to replace source of field with all possible choices
	 *
	 * @param test
	 * @param statement
	 * @param objective
	 */
	private boolean replaceFieldSource(TestChromosome test, FieldStatement statement) {
		if (!statement.isStatic()) {
			VariableReference source = statement.getSource();
			List<VariableReference> objects = test.getTestCase().getObjects(source.getType(),
			                                                                statement.getPosition());
			objects.remove(source);

			if (!objects.isEmpty()) {
				statement.setSource(Randomness.choice(objects));
				test.setChanged(true);
			}
		}

		return false;
	}

}
