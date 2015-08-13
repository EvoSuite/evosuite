package org.evosuite.symbolic.solver.search;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.SolverEmptyQueryException;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.junit.Test;

public class TestIsInteger {

	@Test
	public void testIsInteger() throws SolverEmptyQueryException {

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(
				new StringUnaryToIntegerExpression(new StringVariable("var0",
						"hello"), Operator.IS_INTEGER, 0L), Comparator.NE,
				new IntegerConstant(0)));

		EvoSuiteSolver solver = new EvoSuiteSolver();
		try {
			SolverResult result = solver.solve(constraints);
			assertTrue(result.isSAT());
		} catch (SolverTimeoutException e) {
			fail();
		}
	}
}
