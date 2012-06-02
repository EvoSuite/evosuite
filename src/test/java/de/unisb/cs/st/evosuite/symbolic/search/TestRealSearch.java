/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.Ignore;

import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;
import de.unisb.cs.st.evosuite.symbolic.expr.RealBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.RealConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.RealConstraint;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;

public class TestRealSearch {
	@Test
	public void testEQConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 0.675464,
		        -1000000.0, 1000000.0), Comparator.EQ, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 == ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testNEConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.35082, -1000000.0,
		        1000000.0), Comparator.NE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 != ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testLEConstant() {

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 5.35086, -1000000.0,
		        1000000.0), Comparator.LE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 >= ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testLTConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 5.35086, -1000000.0,
		        1000000.0), Comparator.LT, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 > ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testGEConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0,
		        1000000.0), Comparator.GE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 <= ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testGTConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0,
		        1000000.0), Comparator.GT, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 < ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testEQConstantAfterComma() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0,
		        1000000.0), Comparator.EQ, new RealConstant(0.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(0.35082 == ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testLEConstantAfterComma() {

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.35086, -1000000.0,
		        1000000.0), Comparator.LE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 >= ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testLTConstantAfterComma() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.35086, -1000000.0,
		        1000000.0), Comparator.LT, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 > ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testGEConstantAfterComma() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.0, -1000000.0,
		        1000000.0), Comparator.GE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 <= ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testGTConstantAfterComma() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.0, -1000000.0,
		        1000000.0), Comparator.GT, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 < ((Number) result.get("test1")).doubleValue());
	}

	@Ignore
	@Test
	public void testEQVariable() {
		double var1 = 0.23123;
		double var2 = 1.12321;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.EQ, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		//assertTrue(var1 == var2);
		assertEquals(var1, var2, 0.001);
	}

	@Test
	public void testNEVariable() {
		double var1 = 1.5546;
		double var2 = 1.5546;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.NE, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 != var2);
	}

	@Test
	public void testLEVariable() {
		double var1 = 2.6576;
		double var2 = 1.434;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.LE, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 <= var2);
	}

	@Test
	public void testLTVariable() {
		double var1 = 2.6576;
		double var2 = 1.434;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.LT, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 < var2);
	}

	@Test
	public void testGEVariable() {
		double var1 = 0.7868;
		double var2 = 1.9765;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.GE, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 >= var2);
	}

	@Test
	public void testGTVariable() {
		double var1 = 0.7868;
		double var2 = 1.9765;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.GT, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 > var2);
	}

	@Test
	public void testEvosuiteExample1() {
		double var1 = 1;
		double var2 = 1;

		RealVariable realVar1 = new RealVariable("test1", var1, -1000000, 1000000);
		RealVariable realVar2 = new RealVariable("test2", var2, -1000000, 1000000);

		// x <= 0
		// x < y
		// x >= 0

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(realVar1, Comparator.LE, new RealConstant(0)));
		constraints.add(new RealConstraint(realVar1, Comparator.LT, realVar2));
		constraints.add(new RealConstraint(realVar1, Comparator.GE, new RealConstant(0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertEquals(0, var1, 0.0001);
		assertTrue(var1 < var2);
	}

	@Test
	public void testEvosuiteExample2() {
		double var1 = 355.80758027529504;
		// var3__SYM(355.80758027529504) >= 0.0 dist: 177.90379013764752
		// var3__SYM(355.80758027529504) == 0.0 dist: 177.90379013764752

		RealVariable realVar = new RealVariable("test1", var1, -1000000, 1000000);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(realVar, Comparator.GE, new RealConstant(0.0)));
		constraints.add(new RealConstraint(realVar, Comparator.EQ, new RealConstant(0.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		assertEquals(0, var1, 0.0001);
	}

	//	@Test
	public void testEvosuiteExample3() {
		// ((1102.5 + var22__SYM(12.220999717712402)) * var19__SYM(-45.633541107177734)) == 2.772399987618165E32
		double var1 = 12.220999717712402;
		double var2 = -45.633541107177734;

		RealVariable realVar1 = new RealVariable("test1", var1, -1000000, 1000000);
		RealVariable realVar2 = new RealVariable("test2", var2, -1000000, 1000000);
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealBinaryExpression(
		        new RealBinaryExpression(new RealConstant(1102.5), Operator.PLUS,
		                realVar1, 1.22209997177135E16), Operator.MUL, realVar2,
		        -5.57687492989087E32), Comparator.EQ, new RealConstant(
		        2.772399987618165E32)));

		assert (DistanceEstimator.getDistance(constraints) > 0);
		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertEquals(var1, var2, 0.0001);

	}

	@Test
	public void testAddition() {
		double var1 = 1.0;

		RealVariable realVar1 = new RealVariable("test1", var1, -1000000, 1000000);
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealBinaryExpression(new RealConstant(
		        1102.5), Operator.PLUS, realVar1, 1103.5), Comparator.EQ,
		        new RealConstant(2000.0876588346346)));

		assert (DistanceEstimator.getDistance(constraints) > 0);
		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		//		assertEquals(var1, var2, 0.0001);

	}
}
