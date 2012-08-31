package org.evosuite.symbolic.search;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.Properties.DSEBudgetType;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringComparison;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.junit.Test;

public class TestSeeker2 {

	private static final String INIT_STRING = "abc_e";
	private static final String EXPECTED_STRING = "abcbb";

	private static Collection<Constraint<?>> buildConstraintSystem() {

		StringVariable var0 = new StringVariable("var0", INIT_STRING);

		StringConstant const0 = new StringConstant(EXPECTED_STRING);

		StringComparison strEqual = new StringComparison(var0, Operator.EQUALS, const0,
		        (long) 0);

		IntegerConstant const1 = new IntegerConstant(1);

		IntegerConstraint constr1 = new IntegerConstraint(strEqual, Comparator.EQ, const1);

		return Arrays.<Constraint<?>> asList(constr1);
	}

	@Test
	public void test() {
		Properties.DSE_BUDGET = 5000000000000L;
		Properties.DSE_BUDGET_TYPE = DSEBudgetType.INDIVIDUALS;

		Collection<Constraint<?>> constraints = buildConstraintSystem();

		System.out.println("Constraints:");
		for (Constraint<?> c : constraints) {
			System.out.println(c.toString());
		}

		System.out.println("");
		System.out.println("Initial: " + INIT_STRING);

		Seeker seeker = new Seeker();
		Map<String, Object> model = seeker.getModel(constraints);

		Object var0 = model.get("var0");
		System.out.println("Expected: " + EXPECTED_STRING);
		System.out.println("Found: " + var0);

		assertEquals(EXPECTED_STRING, var0);
	}
}
