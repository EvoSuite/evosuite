package org.evosuite.symbolic.search;

import static org.evosuite.symbolic.SymbolicObserverTest.printConstraints;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.VariableReference;
import org.junit.Test;

public class TestStringSearch2 {

	private static int getFragmentLocation(String s) {
		int fragmentLocation = s.indexOf('#');
		if (fragmentLocation == -1)
			return s.length();

		return fragmentLocation;
	}

	public static void checkPathURN(String nuri) {

		// URI code
		String uri = nuri;
		int colonLocation = nuri.indexOf(':');

		int fragmentLocation = getFragmentLocation(nuri);

		if (colonLocation == -1 || colonLocation > fragmentLocation
				|| colonLocation == 0)
			throw new RuntimeException("No scheme in URI \"" + uri + "\"");

		// URN code
		String nurn = nuri;
		int secondColonLocation = nurn.indexOf(':', colonLocation + 1);

		if (secondColonLocation == -1 || secondColonLocation > fragmentLocation
				|| secondColonLocation == colonLocation + 1)
			throw new RuntimeException("No protocol part in URN \"" + nurn
					+ "\".");

		if (!nurn.regionMatches(0, "urn", 0, colonLocation))
			throw new RuntimeException("The identifier was no URN \"" + nurn
					+ "\".");

		// PathURN code
		if (uri.length() == secondColonLocation + 1)
			throw new RuntimeException("Empty Path URN");

		if (uri.charAt(secondColonLocation + 1) != '/')
			throw new RuntimeException("Path URN has no '/': \"" + uri + "\"");

		if (!uri.regionMatches(colonLocation + 1, "path", 0,
				secondColonLocation - colonLocation - 1))
			throw new RuntimeException("The identifier was no Path URN \""
					+ uri + "\".");
	}

	@Test
	public void testValidPathURN() {
		String pathURN = "urn:path:/A/B/C/doc.html#gilada";
		checkPathURN(pathURN);
	}

	@Test
	public void testValidPathURN2() {
		String pathURN = "u:path:/";
		checkPathURN(pathURN);
	}

	@Test
	public void testInvalidPathURN() {
		try {
			String pathURN = "urn:paxth:/A/B/C/doc.html#gilada";
			checkPathURN(pathURN);
			fail();
		} catch (RuntimeException ex) {
		}
	}

	private DefaultTestCase buildTestCase(String stringVal)
			throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive(stringVal);
		Method method = TestStringSearch2.class.getMethod("checkPathURN",
				String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private List<BranchCondition> executeTest(DefaultTestCase tc) {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000;
		Properties.CONCOLIC_TIMEOUT = 5000000;

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		// ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);

		printConstraints(branch_conditions);
		return branch_conditions;
	}

	@Test
	public void testCreatePathConstraint() throws SecurityException,
			NoSuchMethodException {
		DefaultTestCase tc = buildTestCase("urn:pBth:/A/B/C/doc.html#gilada");
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(11, branch_conditions.size());
	}

	@Test
	public void testSolvePathConstraint() throws SecurityException,
			NoSuchMethodException {
		DefaultTestCase tc = buildTestCase("urn:pBth:/A/B/C/doc.html#gilada");
		List<BranchCondition> branch_conditions = executeTest(tc);

		BranchCondition last_branch = branch_conditions.get(branch_conditions
				.size() - 1);

		Collection<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.addAll(last_branch.getReachingConstraints());
		constraints.add(last_branch.getLocalConstraint().negate());

		ConstraintSolver solver = new ConstraintSolver();
		Map<String, Object> solution;
		try {
			solution = solver.solve(constraints);
			assertNotNull(solution);
			System.out.println(solution);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}

	}

	@Test
	public void testSolveIndexOfConstant() throws SecurityException,
			NoSuchMethodException {
		DefaultTestCase tc = buildTestCase("V*X-:o%tp");
		List<BranchCondition> branch_conditions = executeTest(tc);

		BranchCondition last_branch = branch_conditions.get(branch_conditions
				.size() - 2);

		Collection<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.addAll(last_branch.getReachingConstraints());
		constraints.add(last_branch.getLocalConstraint().negate());

		ConstraintSolver solver = new ConstraintSolver();
		Map<String, Object> solution;
		try {
			solution = solver.solve(constraints);
			assertNotNull(solution);
			System.out.println(solution);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}

	}
}
