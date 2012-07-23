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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.IntegerVariable;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringBinaryExpression;
import org.evosuite.symbolic.expr.StringConstant;
import org.evosuite.symbolic.search.Seeker;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author fraser
 * 
 */
public class TestIntegerSearch {

	@Test
	public void testEQConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.EQ, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertEquals(235082, ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testNEConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235082,
		        -1000000, 1000000), Comparator.NE, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 != ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testLEConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235086,
		        -1000000, 1000000), Comparator.LE, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 >= ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testLTConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235086,
		        -1000000, 1000000), Comparator.LT, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 > ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testGEConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.GE, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 <= ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testGTConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.GT, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 < ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testEQVariable() {
		int var1 = 0;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.EQ, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertEquals(var1, var2);
	}

	@Test
	public void testNEVariable() {
		int var1 = 1;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.NE, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 != var2);
	}

	@Test
	public void testLEVariable() {
		int var1 = 2;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LE, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 <= var2);
	}

	@Test
	public void testLTVariable() {
		int var1 = 2;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LT, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 < var2);
	}

	@Test
	public void testGEVariable() {
		int var1 = 0;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GE, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 >= var2);
	}

	@Test
	public void testGTVariable() {
		int var1 = 0;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GT, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 > var2);
	}

	@Test
	public void testEQArithmetic() {
		int var1 = 0;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 != var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.EQ, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 == var2 + var3);
	}

	@Test
	public void testNEArithmetic() {
		int var1 = 2;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 == var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.NE, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 != var2 + var3);
	}

	@Test
	public void testLEArithmetic() {
		int var1 = 3;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 > var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LE, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 <= var2 + var3);
	}

	@Test
	public void testLTArithmetic() {
		int var1 = 2;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 >= var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LT, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 < var2 + var3);
	}

	@Test
	public void testGEArithmetic() {
		int var1 = 0;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 < var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GT, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 >= var2 + var3);
	}

	@Test
	public void testGTArithmetic() {
		int var1 = 0;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 <= var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GE, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 >= var2 + var3);
	}

	@Test
	public void testEvosuiteExample1() {
		int var1 = 1;
		int var2 = 1;

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LE, new IntegerConstant(0)));
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LT, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GE, new IntegerConstant(0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertEquals(0, var1);
		assertTrue(var1 < var2);
	}

	@Test
	public void testEvosuiteExample3() {
		// (var42__SYM(25721) * (var22__SYM(-1043) - 6860)) == 8275
		int var1 = 25721;
		int var2 = -1043;
		IntegerConstant iconst1 = new IntegerConstant(6860);
		IntegerConstant iconst2 = new IntegerConstant(8275);
		IntegerVariable ivar1 = new IntegerVariable("test1", var1, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);
		IntegerVariable ivar2 = new IntegerVariable("test2", var2, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);
		IntegerBinaryExpression sub = new IntegerBinaryExpression(ivar2, Operator.MINUS,
		        iconst1, -7903L);
		IntegerBinaryExpression mul = new IntegerBinaryExpression(ivar1, Operator.MUL,
		        sub, -203273063L);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(mul, Comparator.EQ, iconst2));
		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 * (var2 - 6860) == 8275);
	}

	@Test
	public void testEvosuiteExample4() {
		// (((var24__SYM(21458) - (var10__SYM(1172) / var14__SYM(-1903))) * 19072) * ((12089 * var40__SYM(-1819)) - ((var39__SYM(-1819) * 14414) % var20__SYM(17433)))) < 11060
		IntegerVariable var24 = new IntegerVariable("var24", 21458, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);
		IntegerVariable var10 = new IntegerVariable("var10", -1903, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);
		IntegerVariable var40 = new IntegerVariable("var40", -1819, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);
		IntegerVariable var39 = new IntegerVariable("var39", -1819, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);
		IntegerVariable var20 = new IntegerVariable("var20", 17433, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);

	}

	@Test
	public void testEvosuiteExample5() {
		// TestSuiteDSE.setStart();

		//Cnstr 0 : var6__SYM(84) != (y charAt 0) dist: 8.0
		//Cnstr 1 : var6__SYM(84) != 115 dist: 8.0
		//Cnstr 2 : var6__SYM(84) == 108 dist: 8.0

		int var1 = 84;
		int const1 = 115;
		int const2 = 108;
		String const3 = "y";

		IntegerConstant iconst1 = new IntegerConstant(const1);
		IntegerConstant iconst2 = new IntegerConstant(const2);
		StringConstant strConst = new StringConstant(const3);

		IntegerVariable ivar1 = new IntegerVariable("test1", var1, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);
		StringBinaryExpression sBExpr = new StringBinaryExpression(strConst,
		        Operator.CHARAT, new IntegerConstant(0), "y");

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(ivar1, Comparator.NE, sBExpr));
		constraints.add(new IntegerConstraint(ivar1, Comparator.NE, iconst1));
		constraints.add(new IntegerConstraint(ivar1, Comparator.EQ, iconst2));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);

		assertNotNull(result);
		assertFalse(result.isEmpty());

		var1 = ((Number) result.get("test1")).intValue();

		assertTrue(var1 == 108);
	}

	@Ignore
	@Test
	public void testEvosuiteExample6() {
		//Cnstr 0 : var2__SYM(1890) >= 0 dist: 682.3333333333334
		//Cnstr 1 : var1__SYM(-157) <= 0 dist: 682.3333333333334
		//Cnstr 2 : var2__SYM(1890) <= var1__SYM(-157) dist: 682.3333333333334
		//	y >= 0
		//	x <= 0
		//	y <= x

		int x = -157;
		int y = 1890;

		// TestSuiteDSE.setStart();

		//		int x = 879254357;
		//		int y = 1013652704;

		IntegerVariable ivar1 = new IntegerVariable("test1", x, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);
		IntegerVariable ivar2 = new IntegerVariable("test2", y, Integer.MIN_VALUE,
		        Integer.MAX_VALUE);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(ivar2, Comparator.GE,
		        new IntegerConstant(0)));
		constraints.add(new IntegerConstraint(ivar1, Comparator.LE,
		        new IntegerConstant(0)));
		constraints.add(new IntegerConstraint(ivar2, Comparator.LE, ivar1));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		if (result.containsKey("test1"))
			x = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			y = ((Number) result.get("test2")).intValue();
		assertTrue(y >= 0);
		assertTrue(x <= 0);
		assertTrue(y <= x);
	}
}
