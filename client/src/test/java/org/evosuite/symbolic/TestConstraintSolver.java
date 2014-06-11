package org.evosuite.symbolic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.search.ConstraintSolver;
import org.evosuite.symbolic.search.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.search.TestInput1;
import org.evosuite.symbolic.search.TestInput2;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.VariableReference;
import org.junit.Test;

public class TestConstraintSolver {

	private List<BranchCondition> executeTest(DefaultTestCase tc) {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000000;
		Properties.CONCOLIC_TIMEOUT = 5000000;

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		// ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		return branch_conditions;
	}

	private DefaultTestCase buildTestCase1() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(-15);
		VariableReference long0 = tc.appendLongPrimitive(Long.MAX_VALUE);
		VariableReference string0 = tc
				.appendStringPrimitive("Togliere sta roba");

		Method method = TestInput1.class.getMethod("test", int.class,
				long.class, String.class);
		tc.appendMethod(null, method, int0, long0, string0);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase1() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase1();
		// build patch condition
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(2, branch_conditions.size());

		// invoke seeker
		Map<String, Object> model;
		try {
			model = executeSeeker(branch_conditions);
			assertNotNull(model);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}

	}

	private Map<String, Object> executeSeeker(
			List<BranchCondition> branch_conditions)
			throws ConstraintSolverTimeoutException {

		final int lastBranchIndex = branch_conditions.size() - 1;
		BranchCondition last_branch = branch_conditions.get(lastBranchIndex);

		List<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
		constraints.addAll(last_branch.getReachingConstraints());

		Constraint<?> lastConstraint = last_branch.getLocalConstraint();

		Constraint<?> targetConstraint = lastConstraint.negate();

		constraints.add(targetConstraint);

		System.out.println("Target constraints");
		printConstraints(constraints);

		ConstraintSolver seeker = new ConstraintSolver();
		Map<String, Object> model = seeker.solve(constraints);

		if (model == null)
			System.out.println("No new model was found");
		else {
			System.out.println(model.toString());
		}

		return model;
	}

	private static void printConstraints(List<Constraint<?>> constraints) {
		for (Constraint<?> constraint : constraints) {
			System.out.println(constraint);
		}

	}

	/**
	 * @param int0
	 *            ==5
	 * @param int1
	 *            ==16
	 * @param int2
	 *            ==16
	 * @param int3
	 *            ==22
	 * @param int4
	 *            ==22
	 * 
	 */
	private DefaultTestCase buildTestCase2() throws SecurityException,
			NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference int0 = tc.appendIntPrimitive(5);
		VariableReference int1 = tc.appendIntPrimitive(16);
		VariableReference int2 = tc.appendIntPrimitive(16);
		VariableReference int3 = tc.appendIntPrimitive(22);
		VariableReference int4 = tc.appendIntPrimitive(22);

		Method method = TestInput2.class.getMethod("test", int.class,
				int.class, int.class, int.class, int.class);
		tc.appendMethod(null, method, int0, int1, int2, int3, int4);
		return tc.getDefaultTestCase();
	}

	@Test
	public void testCase2() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase2();
		// build patch condition
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(57, branch_conditions.size());

		// keep only 2 top-most branch conditions
		List<BranchCondition> sublist = new ArrayList<BranchCondition>();
		sublist.add(branch_conditions.get(0));
		sublist.add(branch_conditions.get(1));

		// invoke seeker
		Map<String, Object> model;
		try {
			model = executeSeeker(sublist);
			assertNotNull(model);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}

	}

}
