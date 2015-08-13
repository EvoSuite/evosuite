package org.evosuite.symbolic.solver.search;

import static org.evosuite.symbolic.solver.TestSolver.solve;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.junit.Test;

public class TestStringEqualsIgnoreCase {

	@Test
	public void testStringEqualsIgnoreCase() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

		IntegerConstant zero = new IntegerConstant(0);
		StringVariable stringVar0 = new StringVariable("var0", "");
		StringConstant strConst = new StringConstant("bar");

		StringBinaryComparison cmp1 = new StringBinaryComparison(stringVar0, Operator.EQUALS, strConst, 0L);
		StringConstraint constr1 = new StringConstraint(cmp1, Comparator.EQ, zero);

		StringBinaryComparison cmp2 = new StringBinaryComparison(stringVar0, Operator.EQUALSIGNORECASE, strConst, 1L);
		StringConstraint constr2 = new StringConstraint(cmp2, Comparator.NE, zero);

		Collection<Constraint<?>> constraints = Arrays.<Constraint<?>> asList(constr1, constr2);

		EvoSuiteSolver solver = new EvoSuiteSolver();

		Map<String, Object> solution = solve(solver, constraints);
		assertNotNull(solution);
		String var0 = (String) solution.get("var0");

		assertNotNull(var0);
		assertTrue(!var0.equals("bar"));
		assertTrue(var0.equalsIgnoreCase("bar"));
	}

}
