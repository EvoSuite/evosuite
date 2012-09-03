package org.evosuite.symbolic.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.Properties.DSEBudgetType;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.junit.Test;

public class TestSeeker3 {

	private static final String INIT_STRING = "125";
	private static final int EXPECTED_INTEGER = 126;

	private static Collection<Constraint<?>> buildConstraintSystem() {

		StringVariable var0 = new StringVariable("var0", INIT_STRING);

		StringToIntegerCast castStr = new StringToIntegerCast(var0,
		        (long) Integer.parseInt(INIT_STRING));

		IntegerConstant const126 = new IntegerConstant(EXPECTED_INTEGER);

		IntegerConstraint constr1 = new IntegerConstraint(castStr, Comparator.EQ,
		        const126);

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

		if (model == null) {
			fail("search was unsuccessfull");
		} else {
			Object var0 = model.get("var0");
			System.out.println("Expected: " + EXPECTED_INTEGER);
			System.out.println("Found: " + var0);

			assertEquals(EXPECTED_INTEGER, var0);
		}
	}
}
