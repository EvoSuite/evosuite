package org.evosuite.symbolic.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Map;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.junit.Test;

public class TestStringSearch3 {

	// (var3("<V6h") charAt 0) >= 0,
	// (var3("<V6h").length() - 1) >= 0,
	// (var3("<V6h") charAt 0) == 10]
	@Test
	public void testCharAt() {
		StringVariable var3 = new StringVariable("var3", "<\n V6h");
		StringBinaryToIntegerExpression var3_charAt_0 = new StringBinaryToIntegerExpression(
				var3, Operator.CHARAT, new IntegerConstant(0),
				(long) "<\n V6h".charAt(0));
		IntegerConstraint cnstr1 = new IntegerConstraint(var3_charAt_0,
				Comparator.GE, new IntegerConstant(0));

		StringUnaryToIntegerExpression var3_length = new StringUnaryToIntegerExpression(
				var3, Operator.LENGTH, (long) "<\n V6h".length());

		IntegerBinaryExpression length_minus_one = new IntegerBinaryExpression(
				var3_length, Operator.MINUS, new IntegerConstant(1),
				(long) "<\n V6h".length() - 1);
		IntegerConstraint cnstr2 = new IntegerConstraint(length_minus_one,
				Comparator.GE, new IntegerConstant(0));

		IntegerConstraint cnstr3 = new IntegerConstraint(var3_charAt_0,
				Comparator.EQ, new IntegerConstant(10));

		ArrayList<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(cnstr1);
		constraints.add(cnstr2);
		constraints.add(cnstr3);

		ConstraintSolver solver = new ConstraintSolver();
		Map<String, Object> solution;
		try {
			solution = solver.solve(constraints);
			assertNotNull(solution);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

}
