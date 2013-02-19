package org.evosuite.testcase;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.LocalSearchObjective;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.search.CachedConstraintSolver;
import org.evosuite.symbolic.search.ConstraintSolverTimeoutException;
import org.evosuite.testsuite.TestCaseExpander;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSELocalSearch extends LocalSearch {

	private static final Logger logger = LoggerFactory
			.getLogger(DSELocalSearch.class);

	public boolean doSearch(TestChromosome test, Set<Integer> statements,
			LocalSearchObjective<TestChromosome> objective) {
		logger.info("APPLYING DSE EEEEEEEEEEEEEEEEEEEEEEE");
		logger.info(test.getTestCase().toCode());
		logger.info("Starting symbolic execution");
		// Backup copy
		test.getMutationHistory().clear();
		TestChromosome clone = (TestChromosome) test.clone();

		// List<BranchCondition> conditions =
		// ConcolicExecution.getSymbolicPath(test);
		List<BranchCondition> conditions = ConcolicExecution
				.executeConcolic((DefaultTestCase) test.getTestCase().clone());
		logger.info("Done symbolic execution");
		for (BranchCondition c : conditions) {
			logger.info(" -> " + c.getLocalConstraint());
		}

		Set<VariableReference> targets = new HashSet<VariableReference>();
		for (Integer position : statements) {
			targets.add(test.getTestCase().getStatement(position)
					.getReturnValue());
		}

		logger.info("Checking {} conditions", conditions.size());
		int num = 0;
		for (BranchCondition condition : conditions) {
			logger.info("Current condition: " + num + "/" + conditions.size()
					+ ": " + condition.getLocalConstraint());
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

			Constraint<?> targetConstraint = condition.getLocalConstraint()
					.negate();
			constraints.add(targetConstraint);

			// Cone of influence reduction
			constraints = reduce(constraints);

			logger.info("Trying to solve: ");
			for (Constraint<?> c : constraints) {
				logger.info("  " + c);
			}

			DSEStats.reportNewConstraints(constraints);

			// Get solution
			CachedConstraintSolver skr = new CachedConstraintSolver();

			long startSolvingTime = System.currentTimeMillis();
			Map<String, Object> values;
			try {
				values = skr.solve(constraints);
			} catch (ConstraintSolverTimeoutException e) {
				values = null;
			}
			long estimatedSolvingTime = System.currentTimeMillis()
					- startSolvingTime;
			DSEStats.reportNewSolvingTime(estimatedSolvingTime);

			if (values != null && !values.isEmpty()) {
				logger.info("Found solution");
				DSEStats.reportNewSAT();
				TestCase oldTest = test.getTestCase();
				TestCase newTest = updateTest(oldTest, values);
				logger.info("New test: " + newTest.toCode());
				test.setTestCase(newTest);
				// test.clearCachedMutationResults(); // TODO Mutation
				test.clearCachedResults();

				if (objective.hasImproved(test)) {
					DSEStats.reportNewTestUseful();
					logger.info("Solution improves fitness, finishing DSE");
					return true;
				} else {
					DSEStats.reportNewTestUnuseful();
					if (Properties.DSE_KEEP_ALL_TESTS) {
						logger.info("Solution does not improve fitness, keeping solution");
						objective.retainPartialSolution((TestChromosome) test
								.clone());
					}

					test.setTestCase(oldTest);
					// FIXXME: How can this be null?
					if (clone.getLastExecutionResult() != null)
						test.setLastExecutionResult(clone
								.getLastExecutionResult());
					// TODO Mutation
				}
			} else {
				logger.info("Found no solution");
				DSEStats.reportNewUNSAT();
			}
		}

		return false;
	}

	@Override
	public boolean doSearch(TestChromosome test, int statement,
			LocalSearchObjective<TestChromosome> objective) {
		Set<Integer> statements = new HashSet<Integer>();
		statements.add(statement);
		return doSearch(test, statements, objective);
	}

	private boolean isRelevant(Constraint<?> constraint,
			Set<VariableReference> targets) {
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

	private boolean isRelevant(Constraint<?> constraint, TestCase test,
			int statement) {
		Set<Variable<?>> variables = constraint.getVariables();
		String target = test.getStatement(statement).getReturnValue().getName();
		for (Variable<?> var : variables) {
			if (var.getName().equals(target))
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
						logger.warn("New value is of an unsupported type: "
								+ p.getValue().getClass() + val);
				} else if (val instanceof String) {
					String name = ((String) key).replace("__SYM", "");
					PrimitiveStatement p = getStatement(newTest, name);
					// logger.warn("New string value for " + name + " is " +
					// val);
					assert (p != null) : "Could not find variable " + name
							+ " in test: " + newTest.toCode()
							+ " / Orig test: " + test.toCode() + ", seed: "
							+ Randomness.getSeed();
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
					assert (p != null) : "Could not find variable " + name
							+ " in test: " + newTest.toCode()
							+ " / Orig test: " + test.toCode() + ", seed: "
							+ Randomness.getSeed();

					if (p.getValue().getClass().equals(Double.class))
						p.setValue(value);
					else if (p.getValue().getClass().equals(Float.class))
						p.setValue(value.floatValue());
					else
						logger.warn("New value is of an unsupported type: "
								+ val);
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
		for (StatementInterface statement : test) {

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
	public static void getVariables(Expression<?> expr,
			Set<Variable<?>> variables) {
		variables.addAll(expr.getVariables());
	}

	private TestCase expandTestCase(TestCase test) {
		TestCaseExpander expander = new TestCaseExpander();
		return expander.expandTestCase(test);
	}
}
