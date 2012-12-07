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
package org.evosuite.testsuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.DSEBudget;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.search.ConstraintSolver;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * TestSuiteDSE class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestSuiteDSE {

	private static final Logger logger = LoggerFactory
			.getLogger(TestSuiteDSE.class);

	/** Constant <code>nrConstraints=0</code> */
	public static int nrConstraints = 0;

	/** Constant <code>nrSolvedConstraints=0</code> */
	public static int nrSolvedConstraints = 0;
	private int nrCurrConstraints = 0;

	/** Constant <code>success=0</code> */
	public static int success = 0;
	/** Constant <code>failed=0</code> */
	public static int failed = 0;

	private final TestSuiteFitnessFunction fitness;

	private final Map<TestChromosome, List<BranchCondition>> branchConditions = new HashMap<TestChromosome, List<BranchCondition>>();

	private final List<TestBranchPair> unsolvedBranchConditions = new ArrayList<TestBranchPair>();

	private final Set<BranchCondition> unsolvableBranchConditions = new HashSet<BranchCondition>();

	private final Map<String, Integer> solutionAttempts = new HashMap<String, Integer>();

	private class TestBranchPair {
		TestChromosome test;
		BranchCondition branch;

		TestBranchPair(TestChromosome test, BranchCondition branch) {
			this.test = test;
			this.branch = branch;
		}

	}

	/**
	 * <p>
	 * Constructor for TestSuiteDSE.
	 * </p>
	 * 
	 * @param fitness
	 *            a {@link org.evosuite.testsuite.TestSuiteFitnessFunction}
	 *            object.
	 */
	public TestSuiteDSE(TestSuiteFitnessFunction fitness) {
		this.fitness = fitness;
	}

	/**
	 * Before applying DSE we expand test cases, such that each primitive value
	 * is used at only exactly one position as a parameter
	 * 
	 * @param individual
	 * @return
	 */
	private TestSuiteChromosome expandTestSuite(TestSuiteChromosome individual) {
		TestSuiteChromosome newTestSuite = new TestSuiteChromosome();
		for (TestChromosome test : individual.getTestChromosomes()) {

			// First make sure we are up to date with the execution
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				test.setLastExecutionResult(TestCaseExecutor.runTest(test
						.getTestCase()));
				test.setChanged(false);
			}

			// We skip tests that have problems
			if (test.getLastExecutionResult().hasTimeout()
					|| test.getLastExecutionResult().hasTestException()) {
				logger.info("Skipping test with timeout or exception");
				continue;
			}

			TestCase newTest = test.getTestCase().clone();
			// TODO: We could cut away the call that leads to an exception?
			/*
			 * if (!test.getLastExecutionResult().noThrownExceptions()) { while
			 * (newTest.size() - 1 >=
			 * test.getLastExecutionResult().getFirstPositionOfThrownException
			 * ()) { newTest.remove(newTest.size() - 1); } }
			 */
			TestCase expandedTest = expandTestCase(newTest);
			newTestSuite.addTest(expandedTest);
		}
		return newTestSuite;
	}

	/**
	 * Iterate over path constraints to identify those which map to branches
	 * that are only covered one way
	 */
	private void calculateUncoveredBranches() {
		unsolvedBranchConditions.clear();
		Map<String, Map<Comparator, Set<TestBranchPair>>> solvedConstraints = new HashMap<String, Map<Comparator, Set<TestBranchPair>>>();

		for (TestChromosome test : branchConditions.keySet()) {
			for (BranchCondition branch : branchConditions.get(test)) {

				if (unsolvableBranchConditions.contains(branch))
					continue;

				String index = getBranchIndex(branch);
				if (!solvedConstraints.containsKey(index))
					solvedConstraints.put(index,
							new HashMap<Comparator, Set<TestBranchPair>>());

				Constraint<?> c = branch.getLocalConstraint();
				if (!solvedConstraints.get(index)
						.containsKey(c.getComparator()))
					solvedConstraints.get(index).put(c.getComparator(),
							new HashSet<TestBranchPair>());
				solvedConstraints.get(index).get(c.getComparator())
						.add(new TestBranchPair(test, branch));
			}
		}

		for (String index : solvedConstraints.keySet()) {
			if (solvedConstraints.get(index).size() == 1) {
				Set<TestBranchPair> branches = solvedConstraints.get(index)
						.values().iterator().next();
				unsolvedBranchConditions.addAll(branches);
			}
		}
		logger.info("Update set of unsolved branch conditions to "
				+ unsolvedBranchConditions.size());

		Randomness.shuffle(unsolvedBranchConditions);
	}

	/**
	 * Calculate and store path constraints for an individual
	 * 
	 * @param test
	 */
	private void updatePathConstraints(TestChromosome test) {
		List<BranchCondition> branches = ConcolicExecution
				.getSymbolicPath(test);
		branchConditions.put(test, branches);
	}

	/**
	 * Create path constraints for all tests in a test suite
	 * 
	 * @param testSuite
	 */
	private void createPathConstraints(TestSuiteChromosome testSuite) {
		for (TestChromosome test : testSuite.getTestChromosomes()) {
			updatePathConstraints(test);
		}
		calculateUncoveredBranches();
	}

	private String getBranchIndex(BranchCondition branch) {
		return branch.getFullName() + branch.getInstructionIndex();
	}

	/**
	 * Get a new candidate for negation
	 * 
	 * @return
	 */
	private TestBranchPair getNextBranchCondition() {
		TestBranchPair pair = unsolvedBranchConditions.remove(0);

		String index = getBranchIndex(pair.branch);
		if (!unsolvedBranchConditions.isEmpty()) {
			while (solutionAttempts.containsKey(index)
					&& solutionAttempts.get(index) >= Properties.CONSTRAINT_SOLUTION_ATTEMPTS
					&& !unsolvedBranchConditions.isEmpty()) {
				logger.info("Reached maximum number of attempts for branch "
						+ index);
				pair = unsolvedBranchConditions.remove(0);
				index = getBranchIndex(pair.branch);
			}
		}

		if (!solutionAttempts.containsKey(index))
			solutionAttempts.put(index, 1);
		else
			solutionAttempts.put(index, solutionAttempts.get(index) + 1);

		return pair;
	}

	/**
	 * Check if there are further candidates for negation
	 * 
	 * @return
	 */
	private boolean hasNextBranchCondition() {
		return !unsolvedBranchConditions.isEmpty();
	}

	/**
	 * Attempt to negate individual branches until budget is used up, or there
	 * are no further branches to negate
	 * 
	 * @param individual
	 */
	public void applyDSE(TestSuiteChromosome individual) {
		TestSuiteChromosome expandedTests = expandTestSuite(individual);
		createPathConstraints(expandedTests);
		fitness.getFitness(expandedTests);

		double originalFitness = individual.getFitness();

		while (hasNextBranchCondition() && !DSEBudget.isFinished()) {
			logger.info("DSE time remaining: " + DSEBudget.getTimeRemaining());
			logger.info("Branches remaining: "
					+ unsolvedBranchConditions.size());
			/*
			 * for (TestBranchPair b : unsolvedBranchConditions) {
			 * logger.info(b.branch.getFullName() + " : " +
			 * b.branch.getInstructionIndex() + ", " +
			 * b.branch.getReachingConstraints().size()); }
			 */
			TestBranchPair next = getNextBranchCondition();
			BranchCondition branch = next.branch;
			// logger.info("Chosen branch condition: " + branch);
			// logger.info(branch.getReachingConstraints().toString());

			TestCase newTest = negateCondition(branch.getReachingConstraints(),
					branch.getLocalConstraint(), next.test.getTestCase());

			if (newTest != null) {
				logger.info("Found new test: " + newTest.toCode());
				// TestChromosome newTestChromosome =
				// expandedTests.addTest(newTest);
				TestChromosome newTestChromosome = new TestChromosome();
				newTestChromosome.setTestCase(newTest);
				expandedTests.addTest(newTestChromosome);
				// updatePathConstraints(newTestChromosome);
				// calculateUncoveredBranches();

				if (fitness.getFitness(expandedTests) < originalFitness) {
					logger.info("New test improves fitness to {}",
							expandedTests.getFitness());
					// expandedTests.addTest(newTestChromosome); // no need to
					// clone so we
					// can keep
					// executionresult
					updatePathConstraints(newTestChromosome);
					calculateUncoveredBranches();
					individual.addTest(newTest);
					originalFitness = expandedTests.getFitness();
					// TODO: Cancel on fitness 0 - would need to know if
					// ZeroFitness is a stopping condition
				} else {
					logger.info("New test does not improve fitness");
					expandedTests.deleteTest(newTest);
				}
				success++;
			} else {
				unsolvableBranchConditions.add(branch);
				failed++;
				logger.info("Failed to find new test.");
			}
		}
		fitness.getFitness(individual);
		DSEBudget.evaluation();

	}

	/**
	 * Generate new constraint and ask solver for solution
	 * 
	 * @param condition
	 * @param test
	 * @return
	 */
	// @SuppressWarnings("rawtypes")
	// @SuppressWarnings("rawtypes")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private TestCase negateCondition(Set<Constraint<?>> reachingConstraints,
			Constraint<?> localConstraint, TestCase test) {
		List<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
		constraints.addAll(reachingConstraints);

		Constraint<?> targetConstraint =localConstraint.negate();
		constraints.add(targetConstraint);
		if (!targetConstraint.isSolveable()) {
			logger.info("Found unsolvable constraint: " + targetConstraint);
			// TODO: This is usually the case when the same variable is used for
			// several parameters of a method
			// Could we treat this as a special case?
			return null;
		}

		int size = constraints.size();
		/*
		 * int counter = 0; for (Constraint cnstr : constraints) {
		 * logger.debug("Cnstr " + (counter++) + " : " + cnstr + " dist: " +
		 * DistanceEstimator.getDistance(constraints)); }
		 */
		if (size > 0) {
			logger.debug("Calculating cone of influence for " + size
					+ " constraints");
			constraints = reduce(constraints);
			logger.info("Reduced constraints from " + size + " to "
					+ constraints.size());
			// for (Constraint<?> c : constraints) {
			// logger.info(c.toString());
			// }
		}

		nrCurrConstraints = constraints.size();
		nrConstraints += nrCurrConstraints;

		logger.info("Applying local search");
		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> values = skr.getModel(constraints);

		if (values != null && !values.isEmpty()) {
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
						logger.debug("New value is of an unsupported type: "
								+ val);
					}
				} else {
					logger.debug("New value is null");

				}
			}
			return newTest;
		} else {
			logger.info("Found no solution");
			return null;
		}

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
	 * Concrete execution
	 * 
	 * @param test
	 * @return
	 */
	private ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			result = TestCaseExecutor.getInstance().execute(test);
		} catch (Exception e) {
			logger.error("",e);
			throw new Error(e);
		}

		return result;
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
