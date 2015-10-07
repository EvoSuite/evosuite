package org.evosuite.symbolic;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.TestCaseNullAssignment;

public class SymbolicObserverTestNull {

	@Test
	public void testNullAssignment() throws NoSuchFieldException, SecurityException {
		TestCaseBuilder builder = new TestCaseBuilder();
		VariableReference var0 = builder.appendNull(TestCaseNullAssignment.class);
		Field x_field = TestCaseNullAssignment.class.getField("x");
		VariableReference int0 = builder.appendIntPrimitive(10);
		builder.appendAssignment(var0, x_field, int0);
		DefaultTestCase testCase = builder.getDefaultTestCase();
		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(testCase);
		assertTrue(branch_conditions.isEmpty());
	}
}
