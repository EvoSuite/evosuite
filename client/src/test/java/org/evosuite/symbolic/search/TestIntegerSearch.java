/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.symbolic.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.junit.Test;

/**
 * @author fraser
 * 
 */
public class TestIntegerSearch {

	@Test
	public void testEQConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints
				.add(new IntegerConstraint(new IntegerVariable("test1", 0,
						-1000000, 1000000), Comparator.EQ, new IntegerConstant(
						235082)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			assertNotNull(result.get("test1"));
			assertEquals(235082, ((Number) result.get("test1")).intValue());
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testNEConstant() {
		// TODO: Currently, the model returned by the search is null if the
		// constraint is already satisfied,
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				235082, -1000000, 1000000), Comparator.NE, new IntegerConstant(
				235082)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			assertNotNull(result.get("test1"));
			assertTrue(235082 != ((Number) result.get("test1")).intValue());
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testLEConstant() {
		// TODO: Currently, the model returned by the search is null if the
		// constraint is already satisfied,
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				235086, -1000000, 1000000), Comparator.LE, new IntegerConstant(
				235082)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			assertNotNull(result.get("test1"));
			assertTrue(235082 >= ((Number) result.get("test1")).intValue());
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testLTConstant() {
		// TODO: Currently, the model returned by the search is null if the
		// constraint is already satisfied,
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				235086, -1000000, 1000000), Comparator.LT, new IntegerConstant(
				235082)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			assertNotNull(result.get("test1"));
			assertTrue(235082 > ((Number) result.get("test1")).intValue());
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testGEConstant() {
		// TODO: Currently, the model returned by the search is null if the
		// constraint is already satisfied,
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints
				.add(new IntegerConstraint(new IntegerVariable("test1", 0,
						-1000000, 1000000), Comparator.GE, new IntegerConstant(
						235082)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			assertNotNull(result.get("test1"));
			assertTrue(235082 <= ((Number) result.get("test1")).intValue());
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testGTConstant() {
		// TODO: Currently, the model returned by the search is null if the
		// constraint is already satisfied,
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints
				.add(new IntegerConstraint(new IntegerVariable("test1", 0,
						-1000000, 1000000), Comparator.GT, new IntegerConstant(
						235082)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			assertNotNull(result.get("test1"));
			assertTrue(235082 < ((Number) result.get("test1")).intValue());
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testEQVariable() {
		int var1 = 0;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.EQ, new IntegerVariable(
				"test2", var2, -1000000, 1000000)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			assertEquals(var1, var2);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testNEVariable() {
		int var1 = 1;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.NE, new IntegerVariable(
				"test2", var2, -1000000, 1000000)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			assertTrue(var1 != var2);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testLEVariable() {
		int var1 = 2;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.LE, new IntegerVariable(
				"test2", var2, -1000000, 1000000)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			assertTrue(var1 <= var2);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testLTVariable() {
		int var1 = 2;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.LT, new IntegerVariable(
				"test2", var2, -1000000, 1000000)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			assertTrue(var1 < var2);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testGEVariable() {
		int var1 = 0;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.GE, new IntegerVariable(
				"test2", var2, -1000000, 1000000)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			assertTrue(var1 >= var2);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testGTVariable() {
		int var1 = 0;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.GT, new IntegerVariable(
				"test2", var2, -1000000, 1000000)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			assertTrue(var1 > var2);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testEQArithmetic() {
		int var1 = 0;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 != var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.EQ,
				new IntegerBinaryExpression(new IntegerVariable("test2", var2,
						-1000000, 1000000), Operator.PLUS, new IntegerVariable(
						"test3", var3, -1000000, 1000000), 0L)));

		ConstraintSolver solver = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = solver.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			if (result.containsKey("test3"))
				var3 = ((Number) result.get("test3")).intValue();
			assertTrue(var1 == var2 + var3);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testNEArithmetic() {
		int var1 = 2;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 == var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.NE,
				new IntegerBinaryExpression(new IntegerVariable("test2", var2,
						-1000000, 1000000), Operator.PLUS, new IntegerVariable(
						"test3", var3, -1000000, 1000000), 0L)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			if (result.containsKey("test3"))
				var3 = ((Number) result.get("test3")).intValue();
			assertTrue(var1 != var2 + var3);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testLEArithmetic() {
		int var1 = 3;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 > var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.LE,
				new IntegerBinaryExpression(new IntegerVariable("test2", var2,
						-1000000, 1000000), Operator.PLUS, new IntegerVariable(
						"test3", var3, -1000000, 1000000), 0L)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			if (result.containsKey("test3"))
				var3 = ((Number) result.get("test3")).intValue();
			assertTrue(var1 <= var2 + var3);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testLTArithmetic() {
		int var1 = 2;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 >= var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.LT,
				new IntegerBinaryExpression(new IntegerVariable("test2", var2,
						-1000000, 1000000), Operator.PLUS, new IntegerVariable(
						"test3", var3, -1000000, 1000000), 0L)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			if (result.containsKey("test3"))
				var3 = ((Number) result.get("test3")).intValue();
			assertTrue(var1 < var2 + var3);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testGEArithmetic() {
		int var1 = 0;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 < var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.GT,
				new IntegerBinaryExpression(new IntegerVariable("test2", var2,
						-1000000, 1000000), Operator.PLUS, new IntegerVariable(
						"test3", var3, -1000000, 1000000), 0L)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			if (result.containsKey("test3"))
				var3 = ((Number) result.get("test3")).intValue();
			assertTrue(var1 >= var2 + var3);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testGTArithmetic() {
		int var1 = 0;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 <= var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.GE,
				new IntegerBinaryExpression(new IntegerVariable("test2", var2,
						-1000000, 1000000), Operator.PLUS, new IntegerVariable(
						"test3", var3, -1000000, 1000000), 0L)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			if (result.containsKey("test3"))
				var3 = ((Number) result.get("test3")).intValue();
			assertTrue(var1 >= var2 + var3);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testEvosuiteExample1() {
		int var1 = 1;
		int var2 = 1;

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints
				.add(new IntegerConstraint(new IntegerVariable("test1", var1,
						-1000000, 1000000), Comparator.LE, new IntegerConstant(
						0)));
		constraints.add(new IntegerConstraint(new IntegerVariable("test1",
				var1, -1000000, 1000000), Comparator.LT, new IntegerVariable(
				"test2", var2, -1000000, 1000000)));
		constraints
				.add(new IntegerConstraint(new IntegerVariable("test1", var1,
						-1000000, 1000000), Comparator.GE, new IntegerConstant(
						0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			assertEquals(0, var1);
			assertTrue(var1 < var2);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	@Test
	public void testEvosuiteExample3() {
		// (var42__SYM(25721) * (var22__SYM(-1043) - 6860)) == 8275
		int var1 = 25721;
		int var2 = -1043;
		IntegerConstant iconst1 = new IntegerConstant(6860);
		IntegerConstant iconst2 = new IntegerConstant(8275);
		IntegerVariable ivar1 = new IntegerVariable("test1", var1,
				Integer.MIN_VALUE, Integer.MAX_VALUE);
		IntegerVariable ivar2 = new IntegerVariable("test2", var2,
				Integer.MIN_VALUE, Integer.MAX_VALUE);
		IntegerBinaryExpression sub = new IntegerBinaryExpression(ivar2,
				Operator.MINUS, iconst1, -7903L);
		IntegerBinaryExpression mul = new IntegerBinaryExpression(ivar1,
				Operator.MUL, sub, -203273063L);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(mul, Comparator.EQ, iconst2));
		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			assertFalse(result.isEmpty());
			if (result.containsKey("test1"))
				var1 = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				var2 = ((Number) result.get("test2")).intValue();
			assertTrue(var1 * (var2 - 6860) == 8275);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}

	private static IntegerValue mul(IntegerValue left, IntegerValue right) {
		int left_val = left.getConcreteValue().intValue();
		int right_val = right.getConcreteValue().intValue();
		return new IntegerBinaryExpression(left, Operator.MUL, right,
				(long) left_val * right_val);
	}

	private static IntegerValue div(IntegerValue left, IntegerValue right) {
		int left_val = left.getConcreteValue().intValue();
		int right_val = right.getConcreteValue().intValue();
		return new IntegerBinaryExpression(left, Operator.DIV, right,
				(long) left_val / right_val);
	}

	private static IntegerValue sub(IntegerValue left, IntegerValue right) {
		int left_val = left.getConcreteValue().intValue();
		int right_val = right.getConcreteValue().intValue();
		return new IntegerBinaryExpression(left, Operator.MINUS, right,
				(long) left_val - right_val);
	}

	private static IntegerValue rem(IntegerValue left, IntegerValue right) {
		int left_val = left.getConcreteValue().intValue();
		int right_val = right.getConcreteValue().intValue();
		return new IntegerBinaryExpression(left, Operator.REM, right,
				(long) left_val % right_val);
	}

	@Test
	public void testEvosuiteExample4_1() {
		IntegerVariable var24 = new IntegerVariable("var24", 21458,
				Integer.MIN_VALUE, Integer.MAX_VALUE);

		IntegerVariable var10 = new IntegerVariable("var10", 1172,
				Integer.MIN_VALUE, Integer.MAX_VALUE);

		IntegerVariable var14 = new IntegerVariable("var14", -1903,
				Integer.MIN_VALUE, Integer.MAX_VALUE);

		IntegerConstant c_19072 = new IntegerConstant(19072);
		IntegerConstant c_11060 = new IntegerConstant(11060);

		IntegerValue left = mul(sub(var24, div(var10, var14)), c_19072);
		IntegerValue right = c_11060;
		IntegerConstraint constr = new IntegerConstraint(left, Comparator.LT,
				right);

		List<Constraint<?>> constraints = Collections
				.<Constraint<?>> singletonList(constr);
		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			int v_24 = ((Number) result.get("var24")).intValue();
			int v_10 = ((Number) result.get("var10")).intValue();
			int v_14 = ((Number) result.get("var14")).intValue();

			assertTrue((v_24 - (v_10 / v_14) * 19072) < 11060);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}

	}

	@Test
	public void testEvosuiteExample4_2() {

		IntegerVariable var20 = new IntegerVariable("var20", 17433,
				Integer.MIN_VALUE, Integer.MAX_VALUE);

		IntegerVariable var39 = new IntegerVariable("var39", -1819,
				Integer.MIN_VALUE, Integer.MAX_VALUE);

		IntegerVariable var40 = new IntegerVariable("var40", -1819,
				Integer.MIN_VALUE, Integer.MAX_VALUE);

		IntegerConstant c_11060 = new IntegerConstant(11060);
		IntegerConstant c_12089 = new IntegerConstant(12089);
		IntegerConstant c_14414 = new IntegerConstant(14414);

		IntegerValue left = sub(mul(c_12089, var40),
				rem(mul(var39, c_14414), var20));
		IntegerValue right = c_11060;
		IntegerConstraint constr = new IntegerConstraint(left, Comparator.GT,
				right);

		List<Constraint<?>> constraints = Collections
				.<Constraint<?>> singletonList(constr);
		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			int v_20 = ((Number) result.get("var20")).intValue();
			int v_39 = ((Number) result.get("var39")).intValue();
			int v_40 = ((Number) result.get("var40")).intValue();

			assertTrue((12089 * v_40) - ((v_39 * 14414) % v_20) > 11060);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}

	}

	@Test
	public void testEvosuiteExample5() {
		// TestSuiteDSE.setStart();

		// Cnstr 0 : var6__SYM(84) != (y charAt 0) dist: 8.0
		// Cnstr 1 : var6__SYM(84) != 115 dist: 8.0
		// Cnstr 2 : var6__SYM(84) == 108 dist: 8.0

		int var1 = 84;
		int const1 = 115;
		int const2 = 108;
		String const3 = "y";

		IntegerConstant iconst1 = new IntegerConstant(const1);
		IntegerConstant iconst2 = new IntegerConstant(const2);
		StringConstant strConst = new StringConstant(const3);

		IntegerVariable ivar1 = new IntegerVariable("test1", var1,
				Integer.MIN_VALUE, Integer.MAX_VALUE);
		StringBinaryToIntegerExpression sBExpr = new StringBinaryToIntegerExpression(
				strConst, Operator.CHARAT, new IntegerConstant(0),
				(long) "y".charAt(0));

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(ivar1, Comparator.NE, sBExpr));
		constraints.add(new IntegerConstraint(ivar1, Comparator.NE, iconst1));
		constraints.add(new IntegerConstraint(ivar1, Comparator.EQ, iconst2));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			assertFalse(result.isEmpty());

			var1 = ((Number) result.get("test1")).intValue();

			assertTrue(var1 == 108);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}

	}

	@Test
	public void testEvosuiteExample6() {
		// Cnstr 0 : var2__SYM(1890) >= 0 dist: 682.3333333333334
		// Cnstr 1 : var1__SYM(-157) <= 0 dist: 682.3333333333334
		// Cnstr 2 : var2__SYM(1890) <= var1__SYM(-157) dist: 682.3333333333334
		// y >= 0
		// x <= 0
		// y <= x

		int x = -157;
		int y = 1890;

		// TestSuiteDSE.setStart();

		// int x = 879254357;
		// int y = 1013652704;

		IntegerVariable ivar1 = new IntegerVariable("test1", x,
				Integer.MIN_VALUE, Integer.MAX_VALUE);
		IntegerVariable ivar2 = new IntegerVariable("test2", y,
				Integer.MIN_VALUE, Integer.MAX_VALUE);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(ivar2, Comparator.GE,
				new IntegerConstant(0)));
		constraints.add(new IntegerConstraint(ivar1, Comparator.LE,
				new IntegerConstant(0)));
		constraints.add(new IntegerConstraint(ivar2, Comparator.LE, ivar1));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result;
		try {
			result = skr.solve(constraints);
			assertNotNull(result);
			assertFalse(result.isEmpty());
			if (result.containsKey("test1"))
				x = ((Number) result.get("test1")).intValue();
			if (result.containsKey("test2"))
				y = ((Number) result.get("test2")).intValue();
			assertTrue(y >= 0);
			assertTrue(x <= 0);
			assertTrue(y <= x);
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}
	}
}
