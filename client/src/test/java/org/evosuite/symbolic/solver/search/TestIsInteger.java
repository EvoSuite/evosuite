package org.evosuite.symbolic.solver.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.junit.Test;

public class TestIsInteger {

	@Test
	public void testIsInteger() {

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(
				new StringUnaryToIntegerExpression(new StringVariable("var0",
						"hello"), Operator.IS_INTEGER, 0L), Comparator.NE,
				new IntegerConstant(0)));

		EvoSuiteSolver solver = new EvoSuiteSolver();
		Map<String, Object> result;
		try {
			result = solver.solve(constraints);
			assertNotNull(result);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}
}
