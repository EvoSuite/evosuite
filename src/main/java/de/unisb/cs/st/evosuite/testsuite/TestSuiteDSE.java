/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.symbolic.BranchCondition;
import de.unisb.cs.st.evosuite.symbolic.ConcolicExecution;
import de.unisb.cs.st.evosuite.symbolic.expr.BinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Cast;
import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstraint;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringMultipleComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.UnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Variable;
import de.unisb.cs.st.evosuite.symbolic.search.Seeker;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.StaticTestCluster;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.VariableReference;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class TestSuiteDSE {

	private static Logger logger = LoggerFactory.getLogger(TestSuiteDSE.class);

	private final Set<Integer> uncoveredBranches = new HashSet<Integer>();

	private final Set<Integer> uncoverableBranches = new HashSet<Integer>();

	public static int nrConstraints = 0;
	public static int nrSolvedConstraints = 0;
	private int nrCurrConstraints = 0;

	private final Set<Branch> branches = new HashSet<Branch>();

	private final Map<String, Set<Integer>> jpfBranchMap = new HashMap<String, Set<Integer>>();

	public static int success = 0;
	public static int failed = 0;

	private final TestSuiteFitnessFunction fitness;

	private static long startTime;

	private static long endTime;

	public TestSuiteDSE(TestSuiteFitnessFunction fitness) {
		this.fitness = fitness;
	}

	public static void setStart() {
		startTime = System.currentTimeMillis();
		endTime = startTime + Properties.DSE_SEARCH_TIMEOUT;
	}

	public static boolean isFinished() {
		return System.currentTimeMillis() >= endTime;
	}

	public void applyDSE(TestSuiteChromosome individual) {
		ConcolicExecution concolicExecution = new ConcolicExecution();
		setStart();

		Map<String, Map<Integer, Map<Comparator, Set<BranchCondition>>>> solvedConstraints = new HashMap<String, Map<Integer, Map<Comparator, Set<BranchCondition>>>>();
		Map<BranchCondition, TestCase> expandedTests = new HashMap<BranchCondition, TestCase>();

		List<TestChromosome> tests = new ArrayList<TestChromosome>(
		        individual.getTestChromosomes());
		Randomness.shuffle(tests);
		for (TestChromosome test : tests) {
			if (isFinished())
				break;
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				test.setLastExecutionResult(runTest(test.getTestCase()));
				test.setChanged(false);
			}
			if (test.getLastExecutionResult().hasTimeout()) {
				logger.info("Skipping test with timeout");
				continue;
			}

			TestCase expandedTest = expandTestCase(test.getTestCase());
			TestChromosome expandedChromosome = new TestChromosome();
			expandedChromosome.setTestCase(expandedTest);
			List<BranchCondition> branches = concolicExecution.getSymbolicPath(expandedChromosome);
			for (BranchCondition branch : branches) {
				String index = branch.ins.getMethodInfo().getFullName()
				        + branch.ins.getInstructionIndex();
				if (!solvedConstraints.containsKey(index))
					solvedConstraints.put(index,
					                      new HashMap<Integer, Map<Comparator, Set<BranchCondition>>>());
				int localConstraint = 0;
				for (Constraint c : branch.localConstraints) {
					if (!solvedConstraints.get(index).containsKey(localConstraint))
						solvedConstraints.get(index).put(localConstraint,
						                                 new HashMap<Comparator, Set<BranchCondition>>());
					if (!solvedConstraints.get(index).get(localConstraint).containsKey(c.getComparator()))
						solvedConstraints.get(index).get(localConstraint).put(c.getComparator(),
						                                                      new HashSet<BranchCondition>());
					solvedConstraints.get(index).get(localConstraint).get(c.getComparator()).add(branch);
					expandedTests.put(branch, expandedTest);
					localConstraint++;
				}
			}
		}

		double originalFitness = individual.getFitness();
		TestSuiteChromosome clone = individual.clone();

		setStart();

		List<Map<Integer, Map<Comparator, Set<BranchCondition>>>> cs = new ArrayList<Map<Integer, Map<Comparator, Set<BranchCondition>>>>();
		cs.addAll(solvedConstraints.values());
		Randomness.shuffle(cs);
		for (Map<Integer, Map<Comparator, Set<BranchCondition>>> branchConstraints : cs) {
			if (isFinished())
				break;
			for (Integer localConstraint : branchConstraints.keySet()) {
				//			for (Map<Comparator, Set<BranchCondition>> comparatorConstraints : branchConstraints.values()) {
				Map<Comparator, Set<BranchCondition>> comparatorConstraints = branchConstraints.get(localConstraint);
				for (Comparator c : comparatorConstraints.keySet()) {
					Comparator cInverse = c.not();
					if (!comparatorConstraints.containsKey(cInverse)) {
						logger.info("Found branch only covered one way - "
						        + comparatorConstraints.get(c).size()
						        + " candidate constraints");
						BranchCondition branch = Randomness.choice(comparatorConstraints.get(c));
						TestCase newTest = negateCondition(branch,
						                                   expandedTests.get(branch),
						                                   localConstraint);
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
					}
				}
			}
		}
	}

	/**
	 * For each uncovered branch try to add a new test
	 * 
	 * @param individual
	 */
	public void applyOldDSE(TestSuiteChromosome individual) {
		long dseEndTime = System.currentTimeMillis() + Properties.DSE_SEARCH_TIMEOUT;
		clearBranches();
		determineCoveredBranches(individual);

		ConcolicExecution concolicExecution = new ConcolicExecution();

		logger.info("Applying DSE to suite of size " + individual.size());
		logger.info("Starting with " + uncoveredBranches.size() + " candidate branches");

		List<TestChromosome> testsToHandle = new LinkedList<TestChromosome>();

		testsToHandle.addAll(individual.getTestChromosomes());
		Collections.shuffle(testsToHandle);

		while (!testsToHandle.isEmpty() && System.currentTimeMillis() < dseEndTime) {
			//pop the first element
			TestChromosome test = testsToHandle.get(0);
			testsToHandle.remove(0);

			if (test.getLastExecutionResult().hasTimeout()) {
				logger.info("Skipping test with timeout");
				continue;
			}

			// Only apply DSE if if makes any sense
			if (hasUncoveredBranches(test)) {
				logger.info("Found uncovered branches in test, applying DSE");

				// TODO: Mapping back to original is missing
				TestCase expandedTest = expandTestCase(test.getTestCase());
				TestChromosome expandedChromosome = new TestChromosome();
				expandedChromosome.setTestCase(expandedTest);
				//test.setTestCase(expandedTest);
				//test.clearCachedResults();

				logger.info("DSE start");
				// Apply DSE to gather constraints
				List<BranchCondition> branches = concolicExecution.getSymbolicPath(expandedChromosome);

				logger.info("DSE finished - found " + branches.size() + " branches");

				// For each uncovered branch
				for (BranchCondition branch : branches) {
					String className = branch.ins.getMethodInfo().getClassName();

					// TODO: Need to match prefixes?
					if (!className.equals(Properties.TARGET_CLASS)
					        && !className.startsWith(Properties.TARGET_CLASS + "$")) {
						logger.debug("Branch is not in target class: " + className);
						continue;
					}

					//logger.debug("Current branch: " + branch);
					if (isUncovered(branch) && coverable(branch)) {
						logger.info("Trying to cover branch "
						        + branch.ins.getInstructionIndex() + ": " + branch);

						// Try to solve negated constraint
						TestCase newTest = negateCondition(branch, expandedTest);

						// If successful, add resulting test to test suite
						if (newTest != null) {
							TestChromosome newChromosome = new TestChromosome();
							newChromosome.setTestCase(newTest);
							updateTestSuite(individual, newChromosome);

							//testsToHandle.add(newChromosome);
							//Collections.shuffle(testsToHandle);

							//newTests.add(newTest);
							//setCovered(branch);
							//assert (uncoveredBranches.size() < oldCovered);
							if (uncoveredBranches.isEmpty()) {
								nrSolvedConstraints += nrCurrConstraints;
								nrCurrConstraints = 0;
								break;
							}
							if (isUncovered(branch)) {
								logger.info("Branch is not covered!");
								if (!newChromosome.getLastExecutionResult().exceptions.isEmpty()) {
									logger.info("Test has exception");
								} else {
									logger.info("Old test: " + expandedTest.toCode());
									logger.info("New test: " + newTest.toCode());
									setUncoverable(branch);
									//assert (false);
								}
								nrCurrConstraints = 0;
							} else {
								testsToHandle.add(newChromosome);
								Collections.shuffle(testsToHandle);
								nrSolvedConstraints += nrCurrConstraints;
								nrCurrConstraints = 0;
							}

							logger.info("-> Remaining " + uncoveredBranches.size()
							        + " candidate branches");
							logger.info("Resulting suite has size " + individual.size());
						} else {
							nrCurrConstraints = 0;
						}
					} else {
						logger.debug("Already covered or uncoverable branch "
						        + branch.ins.getInstructionIndex());// + ": " + branch);

					}
				}
				logger.info("Remaining " + uncoveredBranches.size()
				        + " candidate branches");
			} else {
				logger.info("Test no uncovered branches");
			}
		}

		logger.info("Resulting suite has size " + individual.size());

	}

	private boolean coverable(BranchCondition branch) {
		return !(uncoverableBranches.contains(branch.ins.getInstructionIndex()));
	}

	private void setUncoverable(BranchCondition branch) {
		uncoverableBranches.add(branch.ins.getInstructionIndex());
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
				for (Integer branchId : test.getLastExecutionResult().getTrace().covered_predicates.keySet()) {
					logger.debug("Distances "
					        + branchId
					        + ": "
					        + test.getLastExecutionResult().getTrace().true_distances.get(branchId)
					        + "/"
					        + test.getLastExecutionResult().getTrace().false_distances.get(branchId));

				}
				*/
			}

			for (Integer branchId : test.getLastExecutionResult().getTrace().covered_predicates.keySet()) {
				if (test.getLastExecutionResult().getTrace().true_distances.get(branchId) == 0.0)
					coveredTrue.add(branchId);
				if (test.getLastExecutionResult().getTrace().false_distances.get(branchId) == 0.0)
					coveredFalse.add(branchId);
				logger.debug("Distances "
				        + branchId
				        + ": "
				        + test.getLastExecutionResult().getTrace().true_distances.get(branchId)
				        + "/"
				        + test.getLastExecutionResult().getTrace().false_distances.get(branchId));

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
	 * Update the information about branches we need to cover
	 * 
	 * @param test
	 */
	private void updateTestSuite(TestSuiteChromosome suite, TestChromosome test) {
		suite.addTest(test);
		clearBranches();
		determineCoveredBranches(suite);
	}

	/**
	 * Determine whether this test suite has covered this branch in one of its
	 * tests
	 * 
	 * @param branch
	 * @return
	 */
	private boolean isUncovered(BranchCondition branch) {
		if (!StaticTestCluster.isTargetClassName(branch.ins.getMethodInfo().getClassName())) {
			return false;
		}

		String jpfName = branch.ins.getMethodInfo().getFullName();
		if (!jpfBranchMap.containsKey(jpfName)) {
			return false;
		}

		if (jpfBranchMap.get(jpfName).contains(branch.ins.getInstructionIndex())) {
			return true;
		}

		return false;
	}

	/**
	 * Determine whether this test case touches a branch that is not fully
	 * covered
	 * 
	 * @param test
	 * @return
	 */
	private boolean hasUncoveredBranches(ExecutableChromosome test) {
		for (Integer branchId : test.getLastExecutionResult().getTrace().covered_predicates.keySet()) {
			if (uncoveredBranches.contains(branchId)) {
				logger.info("Uncovered branch found: " + branchId + ": "
				        + BranchPool.getBranch(branchId));
				return true;
			}
		}
		return false;
	}

	private TestCase negateCondition(BranchCondition condition, TestCase test) {
		return negateCondition(condition, test, 0);
	}

	/**
	 * Generate new constraint and ask solver for solution
	 * 
	 * @param condition
	 * @param test
	 * @return
	 */
	//@SuppressWarnings("rawtypes")
	@SuppressWarnings("unchecked")
	private TestCase negateCondition(BranchCondition condition, TestCase test,
	        int localConstraint) {
		List<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
		constraints.addAll(condition.reachingConstraints);
		//constraints.addAll(condition.localConstraints);
		Iterator<Constraint<?>> iterator = condition.localConstraints.iterator();
		int num = 0;
		Constraint<Long> c = null;
		while (iterator.hasNext()) {
			c = (Constraint<Long>) iterator.next();
			if (num == localConstraint)
				break;
			num++;
		}
		if (c == null) {
			logger.warn("Local constraint not found!");
			return null;
		}
		//Constraint<Long> c = (Constraint<Long>) condition.localConstraints.iterator().next();
		Constraint<Long> targetConstraint = new IntegerConstraint(c.getLeftOperand(),
		        c.getComparator().not(), c.getRightOperand());
		constraints.add(targetConstraint);
		if (!targetConstraint.isSolveable()) {
			logger.info("Found unsolvable constraint: " + targetConstraint);
			// TODO: This is usually the case when the same variable is used for several parameters of a method
			// Could we treat this as a special case?
			return null;
		}

		int size = constraints.size();
		/*
				int counter = 0;
				for (Constraint cnstr : constraints) {
					logger.debug("Cnstr " + (counter++) + " : " + cnstr + " dist: "
					        + DistanceEstimator.getDistance(constraints));
				}
		*/
		if (size > 0) {
			logger.debug("Calculating cone of influence for " + size + " constraints");
			constraints = reduce(constraints);
			logger.info("Reduced constraints from " + size + " to " + constraints.size());
		}

		nrCurrConstraints = constraints.size();
		nrConstraints += nrCurrConstraints;
		/*
				counter = 0;
				for (Constraint cnstr : constraints) {
					logger.debug("Cnstr " + (counter++) + " : " + cnstr + " dist: "
					        + DistanceEstimator.getDistance(constraints));
				}
		*/
		logger.info("Applying local search");
		Seeker skr = new Seeker();
		Map<String, Object> values = skr.getModel(constraints);

		//TODO Let's hope you get to delete this at some point ;P
		//		CVC3Solver solver = new CVC3Solver();
		//		Map<String, Object> values = solver.getModel(constraints);

		if (values != null && !values.isEmpty()) {
			TestCase newTest = test.clone();

			for (Object key : values.keySet()) {
				Object val = values.get(key);
				if (val != null) {
					logger.info("New value: " + key + ": " + val);
					if (val instanceof Long) {
						Long value = (Long) val;
						String name = ((String) key).replace("__SYM", "");
						//logger.warn("New long value for " + name + " is " + value);
						PrimitiveStatement p = getStatement(newTest, name);
						assert (p != null);
						if (p.getValue().getClass().equals(Character.class))
							p.setValue((char) value.intValue());
						else if (p.getValue().getClass().equals(Long.class))
							p.setValue(value);
						else if (p.getValue().getClass().equals(Integer.class))
							p.setValue(value.intValue());
						else if (p.getValue().getClass().equals(Boolean.class))
							p.setValue(value.intValue() > 0);
						else
							logger.warn("New value is of an unsupported type: "
							        + p.getValue().getClass() + val);
					} else if (val instanceof String) {
						String name = ((String) key).replace("__SYM", "");
						PrimitiveStatement p = getStatement(newTest, name);
						//logger.warn("New string value for " + name + " is " + val);
						assert (p != null);
						if (p.getValue().getClass().equals(Character.class))
							p.setValue((char) Integer.parseInt(val.toString()));
						else
							p.setValue(val.toString());
					} else if (val instanceof Double) {
						Double value = (Double) val;
						String name = ((String) key).replace("__SYM", "");
						PrimitiveStatement p = getStatement(newTest, name);
						//logger.warn("New double value for " + name + " is " + value);
						assert (p != null);

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
	 * @param variables
	 */
	private void getVariables(Expression<?> expr, Set<Variable<?>> variables) {
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

	private class TestCaseExpander {

		private final Set<VariableReference> usedVariables = new HashSet<VariableReference>();

		private int currentPosition = 0;

		public TestCase expandTestCase(TestCase test) {
			TestCase expandedTest = test.clone();
			while (currentPosition < expandedTest.size()) {
				StatementInterface statement = expandedTest.getStatement(currentPosition);
				if (statement instanceof MethodStatement) {
					visitMethodStatement(expandedTest, (MethodStatement) statement);
				} else if (statement instanceof ConstructorStatement) {
					visitConstructorStatement(expandedTest,
					                          (ConstructorStatement) statement);
				}
				currentPosition++;
			}
			return expandedTest;
		}

		private VariableReference duplicateStatement(TestCase test,
		        VariableReference owner) {
			StatementInterface statement = test.getStatement(owner.getStPosition());
			currentPosition++;
			return test.addStatement(statement.clone(test), owner.getStPosition() + 1);
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitMethodStatement(de.unisb.cs.st.evosuite.testcase.MethodStatement)
		 */
		public void visitMethodStatement(TestCase test, MethodStatement statement) {
			// The problem is that at this point in the test case the parameters might have already changed

			int i = 0;
			for (VariableReference var : statement.getParameterReferences()) {
				if (var.isPrimitive() || var.isString()) {
					if (usedVariables.contains(var)
					        && test.getStatement(var.getStPosition()) instanceof PrimitiveStatement<?>) {
						// Duplicate and replace
						VariableReference varCopy = duplicateStatement(test, var);
						statement.replaceParameterReference(varCopy, i);
						usedVariables.add(varCopy);
					}
					usedVariables.add(var);
				}
				i++;
			}
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.testcase.TestVisitor#visitConstructorStatement(de.unisb.cs.st.evosuite.testcase.ConstructorStatement)
		 */
		public void visitConstructorStatement(TestCase test,
		        ConstructorStatement statement) {
			int i = 0;
			for (VariableReference var : statement.getParameterReferences()) {
				if (var.isPrimitive() || var.isString()) {
					if (usedVariables.contains(var)
					        && test.getStatement(var.getStPosition()) instanceof PrimitiveStatement<?>) {
						// Duplicate and replace
						VariableReference varCopy = duplicateStatement(test, var);
						statement.replaceParameterReference(varCopy, i);
						usedVariables.add(varCopy);
					}
					usedVariables.add(var);
				}
				i++;
			}
		}

	}

}
