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
package org.evosuite.testcase.localsearch;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.solver.SolverCache;
import org.evosuite.symbolic.solver.Solver;
import org.evosuite.symbolic.solver.SolverFactory;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSELocalSearch extends StatementLocalSearch {

	private static final Logger logger = LoggerFactory.getLogger(DSELocalSearch.class);


	public boolean doSearch(TestChromosome test, Set<Integer> statements,
			LocalSearchObjective<TestChromosome> objective) {
		logger.info("APPLYING DSE EEEEEEEEEEEEEEEEEEEEEEE");
		logger.info(test.getTestCase().toCode());
		logger.info("Starting symbolic execution");
		// Backup copy
		// test.getMutationHistory().clear();
		test.clone(); // I am not sure what is the purpose of this 

		DefaultTestCase clone_test_case = (DefaultTestCase) test.getTestCase().clone();
		List<BranchCondition> conditions = ConcolicExecution.executeConcolic(clone_test_case);
		logger.info("Done symbolic execution");
		for (BranchCondition c : conditions) {
			logger.info(" -> " + c.getLocalConstraint());
		}

		Set<VariableReference> targets = new HashSet<VariableReference>();
		for (Integer position : statements) {
			targets.add(test.getTestCase().getStatement(position).getReturnValue());
		}

		logger.info("Checking {} conditions", conditions.size());
		int num = 0;
		for (BranchCondition condition : conditions) {
			
			if (LocalSearchBudget.getInstance().isFinished()) {
				logger.debug("Local search budget used up: "
				        + Properties.LOCAL_SEARCH_BUDGET_TYPE);
				break;
			}
			logger.debug("Local search budget not yet used up");
			
			
			logger.info("Current condition: " + num + "/" + conditions.size() + ": " + condition.getLocalConstraint());
			num++;
			// Determine if this a branch condition depending on the target
			// statement
			Constraint<?> currentConstraint = condition.getLocalConstraint();

			if (!isRelevant(currentConstraint, targets)) {
				// if(!isRelevant(currentConstraint, test.getTestCase(),
				// statement)) {
				logger.info("Is not relevant for " + targets);
				continue;
			}
			logger.info("Is relevant for " + targets);

			List<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
			constraints.addAll(condition.getReachingConstraints());

			Constraint<?> targetConstraint = condition.getLocalConstraint().negate();
			constraints.add(targetConstraint);

			// Cone of influence reduction
			constraints = reduce(constraints);

			logger.info("Trying to solve: ");
			for (Constraint<?> c : constraints) {
				logger.info("  " + c);
			}

			DSEStats.getInstance().reportNewConstraints(constraints);

			// Get solution
			Solver solver = SolverFactory.getInstance().buildNewSolver();

			long startSolvingTime = System.currentTimeMillis();
			SolverCache solverCache = SolverCache.getInstance();
			SolverResult solverResult = solverCache.solve(solver, constraints);
			long estimatedSolvingTime = System.currentTimeMillis() - startSolvingTime;
			DSEStats.getInstance().reportNewSolvingTime(estimatedSolvingTime);

			if (solverResult == null) {
				logger.info("Found no result");

			} else if (solverResult.isUNSAT()) {
				logger.info("Found UNSAT result");
				DSEStats.getInstance().reportNewUNSAT();
			} else {
				logger.info("Found SAT result");
				DSEStats.getInstance().reportNewSAT();
				Map<String, Object> model = solverResult.getModel();
				TestCase oldTest = test.getTestCase();
				ExecutionResult oldResult = test.getLastExecutionResult().clone();
				TestCase newTest = updateTest(oldTest, model);
				logger.info("New test: " + newTest.toCode());
				test.setTestCase(newTest);
				// test.clearCachedMutationResults(); // TODO Mutation
				test.clearCachedResults();

				if (objective.hasImproved(test)) {
					DSEStats.getInstance().reportNewTestUseful();
					logger.info("Solution improves fitness, finishing DSE");
					return true;
				} else {
					DSEStats.getInstance().reportNewTestUnuseful();
					test.setTestCase(oldTest);
					// FIXXME: How can this be null?
					if (oldResult != null)
						test.setLastExecutionResult(oldResult);
					// TODO Mutation
				}
			}
		}
		return false;
	}

	@Override
	public boolean doSearch(TestChromosome test, int statement, LocalSearchObjective<TestChromosome> objective) {
		Set<Integer> statements = new HashSet<Integer>();
		statements.add(statement);
		return doSearch(test, statements, objective);
	}

	private boolean isRelevant(Constraint<?> constraint, Set<VariableReference> targets) {
		Set<Variable<?>> variables = constraint.getVariables();
		Set<String> targetNames = new HashSet<String>();
		for (VariableReference v : targets) {
			targetNames.add(v.getName());
		}
		for (Variable<?> var : variables) {
			if (targetNames.contains(var.getName()))
				return true;
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private TestCase updateTest(TestCase test, Map<String, Object> values) {

		TestCase newTest = test.clone();

		for (Object key : values.keySet()) {
			Object val = values.get(key);
			if (val != null) {
				logger.info("New value: " + key + ": " + val);
				if (val instanceof Long) {
					Long value = (Long) val;
					String name = ((String) key).replace("__SYM", "");
					// logger.warn("New long value for " + name + " is " +
					// value);
					PrimitiveStatement p = getStatement(newTest, name);
					if (p.getValue().getClass().equals(Character.class))
						p.setValue((char) value.intValue());
					else if (p.getValue().getClass().equals(Long.class))
						p.setValue(value);
					else if (p.getValue().getClass().equals(Integer.class))
						p.setValue(value.intValue());
					else if (p.getValue().getClass().equals(Short.class))
						p.setValue(value.shortValue());
					else if (p.getValue().getClass().equals(Boolean.class))
						p.setValue(value.intValue() > 0);
					else if (p.getValue().getClass().equals(Byte.class))
						p.setValue(value.byteValue() > 0);
					else
						logger.warn("New value is of an unsupported type: " + p.getValue().getClass() + val);
				} else if (val instanceof String) {
					String name = ((String) key).replace("__SYM", "");
					PrimitiveStatement p = getStatement(newTest, name);
					// logger.warn("New string value for " + name + " is " +
					// val);
					assert(p != null) : "Could not find variable " + name + " in test: " + newTest.toCode()
							+ " / Orig test: " + test.toCode() + ", seed: " + Randomness.getSeed();
					if (p.getValue().getClass().equals(Character.class))
						p.setValue((char) Integer.parseInt(val.toString()));
					else
						p.setValue(val.toString());
				} else if (val instanceof Double) {
					Double value = (Double) val;
					String name = ((String) key).replace("__SYM", "");
					PrimitiveStatement p = getStatement(newTest, name);
					// logger.warn("New double value for " + name + " is " +
					// value);
					assert(p != null) : "Could not find variable " + name + " in test: " + newTest.toCode()
							+ " / Orig test: " + test.toCode() + ", seed: " + Randomness.getSeed();

					if (p.getValue().getClass().equals(Double.class))
						p.setValue(value);
					else if (p.getValue().getClass().equals(Float.class))
						p.setValue(value.floatValue());
					else
						logger.warn("New value is of an unsupported type: " + val);
				} else {
					logger.debug("New value is of an unsupported type: " + val);
				}
			} else {
				logger.debug("New value is null");

			}
		}
		return newTest;

	}

	/**
	 * Apply cone of influence reduction to constraints with respect to the last
	 * constraint in the list
	 * 
	 * @param constraints
	 * @return
	 */
	private List<Constraint<?>> reduce(List<Constraint<?>> constraints) {

		Constraint<?> target = constraints.get(constraints.size() - 1);
		Set<Variable<?>> dependencies = getVariables(target);

		LinkedList<Constraint<?>> coi = new LinkedList<Constraint<?>>();
		if (dependencies.size() <= 0)
			return coi;

		coi.add(target);

		for (int i = constraints.size() - 2; i >= 0; i--) {
			Constraint<?> constraint = constraints.get(i);
			Set<Variable<?>> variables = getVariables(constraint);
			for (Variable<?> var : dependencies) {
				if (variables.contains(var)) {
					dependencies.addAll(variables);
					coi.addFirst(constraint);
					break;
				}
			}
		}
		return coi;
	}

	/**
	 * Get the statement that defines this variable
	 * 
	 * @param test
	 * @param name
	 * @return
	 */
	private PrimitiveStatement<?> getStatement(TestCase test, String name) {
		for (Statement statement : test) {

			if (statement instanceof PrimitiveStatement<?>) {
				if (statement.getReturnValue().getName().equals(name))
					return (PrimitiveStatement<?>) statement;
			}
		}
		return null;
	}

	/**
	 * Determine the set of variable referenced by this constraint
	 * 
	 * @param constraint
	 * @return
	 */
	private Set<Variable<?>> getVariables(Constraint<?> constraint) {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		getVariables(constraint.getLeftOperand(), variables);
		getVariables(constraint.getRightOperand(), variables);
		return variables;
	}

	/**
	 * Recursively determine constraints in expression
	 * 
	 * @param expr
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param variables
	 *            a {@link java.util.Set} object.
	 */
	public static void getVariables(Expression<?> expr, Set<Variable<?>> variables) {
		variables.addAll(expr.getVariables());
	}

}
