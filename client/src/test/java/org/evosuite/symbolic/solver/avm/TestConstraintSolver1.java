/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver.avm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.RandomizedTC;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.SolverEmptyQueryException;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.avm.EvoSuiteSolver;
import org.junit.Test;

public class TestConstraintSolver1 extends RandomizedTC {
	public void testMe(String x) {
		if (x.length() == 5 && x.charAt(4) == '_') {
			System.out.println("Juhu");
		}
	}

	// ((INT)var0(abc_e).length()) == 5
	// ((INT)(var0(abc_e) charAt 3)) == 95
	// ((INT)(var0(abc_e) charAt 4)) != 43

	private static final String INIT_STRING = "abc_e";
	private static final String EXPECTED_STRING = "abcbb";

	private static Collection<Constraint<?>> buildConstraintSystem() {
		StringVariable var0 = new StringVariable("var0", INIT_STRING);
		StringUnaryToIntegerExpression length = new StringUnaryToIntegerExpression(var0, Operator.LENGTH,
				(long) INIT_STRING.length());
		IntegerConstant const3 = new IntegerConstant(3);
		StringBinaryToIntegerExpression charAt3 = new StringBinaryToIntegerExpression(var0, Operator.CHARAT, const3,
				(long) INIT_STRING.charAt(3));
		IntegerConstant const4 = new IntegerConstant(4);
		StringBinaryToIntegerExpression charAt4 = new StringBinaryToIntegerExpression(var0, Operator.CHARAT, const4,
				(long) INIT_STRING.charAt(4));

		IntegerConstant const5 = new IntegerConstant(INIT_STRING.length());
		IntegerConstant const95 = new IntegerConstant(EXPECTED_STRING.charAt(3));
		IntegerConstant const43 = new IntegerConstant(EXPECTED_STRING.charAt(4));

		IntegerConstraint constr1 = new IntegerConstraint(length, Comparator.EQ, const5);
		IntegerConstraint constr2 = new IntegerConstraint(charAt3, Comparator.EQ, const95);
		IntegerConstraint constr3 = new IntegerConstraint(charAt4, Comparator.EQ, const43);

		return Arrays.<Constraint<?>> asList(constr1, constr2, constr3);
	}

	@Test
	public void test() throws SolverEmptyQueryException {
		Properties.LOCAL_SEARCH_BUDGET = 100; // 5000000000000L; TODO - ??
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.FITNESS_EVALUATIONS;

		Collection<Constraint<?>> constraints = buildConstraintSystem();

		System.out.println("Constraints:");
		for (Constraint<?> c : constraints) {
			System.out.println(c.toString());
		}

		EvoSuiteSolver seeker = new EvoSuiteSolver();
		try {
			SolverResult solverResult = seeker.solve(constraints);
			assertTrue(solverResult.isSAT());

			Map<String, Object> model = solverResult.getModel();
			System.out.println(model);

			Object var0 = model.get("var0");
			System.out.println("Expected: " + EXPECTED_STRING);
			System.out.println("Found: " + var0);

			assertEquals(EXPECTED_STRING, var0);
		} catch (SolverTimeoutException e) {
			fail();
		}
	}
}
