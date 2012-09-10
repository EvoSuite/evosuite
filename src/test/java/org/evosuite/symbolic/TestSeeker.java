package org.evosuite.symbolic;

import static org.evosuite.symbolic.SymbolicObserverTest.printConstraints;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.search.Seeker;
import org.evosuite.symbolic.search.TestInput1;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.VariableReference;
import org.junit.Test;

public class TestSeeker {

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

		printConstraints(branch_conditions);
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
		Map<String, Object> model = executeSeeker(branch_conditions);
		assertNotNull(model);

	}

	private Map<String, Object> executeSeeker(
			List<BranchCondition> branch_conditions) {

		BranchCondition last_branch = branch_conditions.get(branch_conditions
				.size() - 1);

		List<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
		constraints.addAll(last_branch.getReachingConstraints());
		constraints.add(last_branch.getLocalConstraint());

		Seeker seeker = new Seeker();
		Map<String, Object> model = seeker.getModel(constraints);

		if (model == null)
			System.out.println("No new model was found");
		else {
			System.out.println(model.toString());
		}
		
		return model;
	}

}
