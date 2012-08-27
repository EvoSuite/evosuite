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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.ga.DSEBudget;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.expr.BinaryExpression;
import org.evosuite.symbolic.expr.Cast;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.StringBuilderExpression;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringMultipleComparison;
import org.evosuite.symbolic.expr.UnaryExpression;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.search.Seeker;
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

	private static final Logger logger = LoggerFactory.getLogger(TestSuiteDSE.class);

	private final Set<Integer> uncoveredBranches = new HashSet<Integer>();

	private final Set<Integer> uncoverableBranches = new HashSet<Integer>();

	/** Constant <code>nrConstraints=0</code> */
	public static int nrConstraints = 0;
	/** Constant <code>nrSolvedConstraints=0</code> */
	public static int nrSolvedConstraints = 0;
	private int nrCurrConstraints = 0;

	private final Set<Branch> branches = new HashSet<Branch>();

	private final Map<String, Set<Integer>> jpfBranchMap = new HashMap<String, Set<Integer>>();

	/** Constant <code>success=0</code> */
	public static int success = 0;
	/** Constant <code>failed=0</code> */
	public static int failed = 0;

	private final TestSuiteFitnessFunction fitness;

	private final Map<String, Integer> variablePositionMap = new HashMap<String, Integer>();

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
	 * <p>
	 * applyDSE
	 * </p>
	 * 
	 * @param individual
	 *            a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 */
	public void applyDSE(TestSuiteChromosome individual) {
		ConcolicExecution concolicExecution = new ConcolicExecution();

		Map<String, Map<Integer, Map<Comparator, Set<BranchCondition>>>> solvedConstraints = new HashMap<String, Map<Integer, Map<Comparator, Set<BranchCondition>>>>();
		Map<BranchCondition, TestCase> expandedTests = new HashMap<BranchCondition, TestCase>();

		List<TestChromosome> tests = new ArrayList<TestChromosome>(
		        individual.getTestChromosomes());
		Randomness.shuffle(tests);
		for (TestChromosome test : tests) {
			if (DSEBudget.isHalfRemaining()) {
				logger.info("Half the DSE Budget used up, continuing with constraint solving");
				break;
			}
			if (DSEBudget.isFinished()) {
				logger.info("DSE Budget used up");
				break;
			}
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				test.setLastExecutionResult(runTest(test.getTestCase()));
				test.setChanged(false);
			}
			if (test.getLastExecutionResult().hasTimeout()
			        || test.getLastExecutionResult().hasTestException()) {
				logger.info("Skipping test with timeout or exception");
				continue;
			}

			TestCase expandedTest = expandTestCase(test.getTestCase());
			TestChromosome expandedChromosome = new TestChromosome();
			expandedChromosome.setTestCase(expandedTest);
			List<BranchCondition> branches;
			branches = concolicExecution.getSymbolicPath(expandedChromosome);

			for (BranchCondition branch : branches) {
				String index = branch.getFullName() + branch.getInstructionIndex();
				if (!solvedConstraints.containsKey(index))
					solvedConstraints.put(index,
					                      new HashMap<Integer, Map<Comparator, Set<BranchCondition>>>());
				int localConstraint = 0;
				Constraint<?> c = branch.getLocalConstraint();
				//				for (Constraint<?> c : branch.getLocalConstraints()) {
				if (!solvedConstraints.get(index).containsKey(localConstraint))
					solvedConstraints.get(index).put(localConstraint,
					                                 new HashMap<Comparator, Set<BranchCondition>>());
				if (!solvedConstraints.get(index).get(localConstraint).containsKey(c.getComparator()))
					solvedConstraints.get(index).get(localConstraint).put(c.getComparator(),
					                                                      new HashSet<BranchCondition>());
				solvedConstraints.get(index).get(localConstraint).get(c.getComparator()).add(branch);
				expandedTests.put(branch, expandedTest);
				localConstraint++;
				//				}
			}
		}

		double originalFitness = individual.getFitness();
		TestSuiteChromosome clone = individual.clone();

		// DSEBudget.DSEStarted();

		List<Map<Integer, Map<Comparator, Set<BranchCondition>>>> cs = new ArrayList<Map<Integer, Map<Comparator, Set<BranchCondition>>>>();
		cs.addAll(solvedConstraints.values());
		Randomness.shuffle(cs);
		boolean attempted = false;
		for (Map<Integer, Map<Comparator, Set<BranchCondition>>> branchConstraints : cs) {
			if (DSEBudget.isFinished()) {
				logger.info("DSE Budget used up");
				break;
			}
			for (Integer localConstraint : branchConstraints.keySet()) {
				// for (Map<Comparator, Set<BranchCondition>>
				// comparatorConstraints : branchConstraints.values()) {
				Map<Comparator, Set<BranchCondition>> comparatorConstraints = branchConstraints.get(localConstraint);
				logger.info("Current constraint: " + localConstraint);
				for (Comparator c : comparatorConstraints.keySet()) {
					logger.info("Comparator " + c);
					for (BranchCondition bc : comparatorConstraints.get(c)) {
						logger.info("Branch details: " + bc.getFullName() + ":"
						        + bc.getInstructionIndex());
					}
				}
				for (Comparator c : comparatorConstraints.keySet()) {
					Comparator cInverse = c.not();
					if (!comparatorConstraints.containsKey(cInverse)) {
						attempted = true;
						logger.info("Found branch only covered one way - "
						        + comparatorConstraints.get(c).size()
						        + " candidate constraints");
						BranchCondition branch = Randomness.choice(comparatorConstraints.get(c));
						TestCase newTest = negateCondition(branch.getReachingConstraints(),
						                                   branch.getLocalConstraint(),
						                                   expandedTests.get(branch));
						if (newTest != null) {
							logger.info("Found new test!");
							clone.addTest(newTest);
							if (fitness.getFitness(clone) < originalFitness) {
								logger.info("New test improves fitness to "
								        + clone.getFitness());
								individual.addTest(newTest);
								originalFitness = clone.getFitness();
							} else {
								logger.info("New test does not improve fitness");
							}
							success++;
						} else {
							failed++;
							logger.info("Failed to find new test.");
						}

					} else {
						logger.info("Branch is covered both ways: " + c);
						BranchCondition branch = Randomness.choice(comparatorConstraints.get(c));
						logger.info("Branch details: " + branch.getFullName() + ":"
						        + branch.getInstructionIndex());
					}
				}
			}
		}
		if (!attempted) {
			logger.info("Found no candidate branches!");
		}

		DSEBudget.evaluation();
	}

	private void addBranch(Branch b) {
		String key = b.getClassName() + "." + b.getMethodName();
		if (!jpfBranchMap.containsKey(key)) {
			jpfBranchMap.put(key, new HashSet<Integer>());
		}
		jpfBranchMap.get(key).add(b.getInstruction().getJPFId());
	}

	/**
	 * Determine which of the branches are covered by the suite and which are
	 * not
	 * 
	 * @param suite
	 */
	private void determineCoveredBranches(TestSuiteChromosome suite) {
		Set<Integer> coveredTrue = new HashSet<Integer>();
		Set<Integer> coveredFalse = new HashSet<Integer>();

		for (TestChromosome test : suite.getTestChromosomes()) {
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				test.setLastExecutionResult(runTest(test.getTestCase()));
				test.setChanged(false);
				/*
				 * for (Integer branchId :
				 * test.getLastExecutionResult().getTrace
				 * ().covered_predicates.keySet()) { logger.debug("Distances " +
				 * branchId + ": " +
				 * test.getLastExecutionResult().getTrace().true_distances
				 * .get(branchId) + "/" +
				 * test.getLastExecutionResult().getTrace(
				 * ).false_distances.get(branchId));
				 * 
				 * }
				 */
			}

			for (Integer branchId : test.getLastExecutionResult().getTrace().getCoveredPredicates()) {
				if (test.getLastExecutionResult().getTrace().getTrueDistance(branchId) == 0.0)
					coveredTrue.add(branchId);
				if (test.getLastExecutionResult().getTrace().getFalseDistance(branchId) == 0.0)
					coveredFalse.add(branchId);
				logger.debug("Distances "
				        + branchId
				        + ": "
				        + test.getLastExecutionResult().getTrace().getTrueDistance(branchId)
				        + "/"
				        + test.getLastExecutionResult().getTrace().getFalseDistance(branchId));

			}
		}

		for (Integer branchId : coveredTrue) {
			if (!coveredFalse.contains(branchId)) {
				Branch b = BranchPool.getBranch(branchId);
				logger.info("Covered only true: " + b);
				branches.add(b);
				addBranch(b);
				uncoveredBranches.add(branchId);
			}
		}
		for (Integer branchId : coveredFalse) {
			if (!coveredTrue.contains(branchId)) {
				Branch b = BranchPool.getBranch(branchId);
				logger.info("Covered only false: " + b);
				branches.add(b);
				addBranch(b);
				uncoveredBranches.add(branchId);
			}
		}
		logger.info("Found " + uncoveredBranches.size() + " candidate branches");
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
	@SuppressWarnings("unchecked")
	private TestCase negateCondition(Set<Constraint<?>> reachingConstraints,
	        Constraint<?> localConstraint, TestCase test) {
		List<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
		constraints.addAll(reachingConstraints);

		Constraint<Long> targetConstraint = new IntegerConstraint(
		        localConstraint.getLeftOperand(), localConstraint.getComparator().not(),
		        localConstraint.getRightOperand());
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
			logger.debug("Calculating cone of influence for " + size + " constraints");
			constraints = reduce(constraints);
			logger.info("Reduced constraints from " + size + " to " + constraints.size());
		}

		nrCurrConstraints = constraints.size();
		nrConstraints += nrCurrConstraints;
		/*
		 * counter = 0; for (Constraint cnstr : constraints) {
		 * logger.debug("Cnstr " + (counter++) + " : " + cnstr + " dist: " +
		 * DistanceEstimator.getDistance(constraints)); }
		 */
		logger.info("Applying local search");
		Seeker skr = new Seeker();
		Map<String, Object> values = skr.getModel(constraints);

		// TODO Let's hope you get to delete this at some point ;P
		// CVC3Solver solver = new CVC3Solver();
		// Map<String, Object> values = solver.getModel(constraints);

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
						        + " in test: " + newTest.toCode() + " / Orig test: "
						        + test.toCode() + ", seed: " + Randomness.getSeed();
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
						        + " in test: " + newTest.toCode() + " / Orig test: "
						        + test.toCode() + ", seed: " + Randomness.getSeed();

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
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

	/**
	 * Clear the information about the last DSE run
	 */
	private void clearBranches() {
		branches.clear();
		jpfBranchMap.clear();
		uncoveredBranches.clear();
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
	public static void getVariables(Expression<?> expr, Set<Variable<?>> variables) {
		if (expr instanceof Variable<?>) {
			variables.add((Variable<?>) expr);
		} else if (expr instanceof StringMultipleComparison) {
			StringMultipleComparison smc = (StringMultipleComparison) expr;
			getVariables(smc.getLeftOperand(), variables);
			getVariables(smc.getRightOperand(), variables);
			ArrayList<Expression<?>> ar_l_ex = smc.getOther();
			Iterator<Expression<?>> itr = ar_l_ex.iterator();
			while (itr.hasNext()) {
				Expression<?> element = itr.next();
				getVariables(element, variables);
			}
		} else if (expr instanceof StringBuilderExpression) {
			StringBuilderExpression sB = (StringBuilderExpression) expr;
			getVariables(sB.getExpr(), variables);
		} else if (expr instanceof StringComparison) {
			StringComparison sc = (StringComparison) expr;
			getVariables(sc.getLeftOperand(), variables);
			getVariables(sc.getRightOperand(), variables);
		} else if (expr instanceof BinaryExpression<?>) {
			BinaryExpression<?> bin = (BinaryExpression<?>) expr;
			getVariables(bin.getLeftOperand(), variables);
			getVariables(bin.getRightOperand(), variables);
		} else if (expr instanceof UnaryExpression<?>) {
			UnaryExpression<?> un = (UnaryExpression<?>) expr;
			getVariables(un.getOperand(), variables);
		} else if (expr instanceof Cast<?>) {
			Cast<?> cst = (Cast<?>) expr;
			getVariables(cst.getConcreteObject(), variables);
		} else if (expr instanceof Constraint<?>) {
			// ignore
		}
	}

	private TestCase expandTestCase(TestCase test) {
		TestCaseExpander expander = new TestCaseExpander();
		return expander.expandTestCase(test);
	}

}
