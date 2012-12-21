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
package org.evosuite.symbolic.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.junit.Test;

public class TestStringSearch {

	@Test
	public void testEqualsTrueConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "foo";
		String const2 = "test";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.EQUALS, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.NE,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(const2.equals(result.get("test1").toString()));
	}

	@Test
	public void testEqualsFalseConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "foo";
		String const2 = "foo";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.EQUALS, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.EQ,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(!const2.equals(result.get("test1").toString()));
	}

	@Test
	public void testEqualsIgnoreCaseTrueConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "foo";
		String const2 = "Fest";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.EQUALSIGNORECASE, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.NE,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(const2.equalsIgnoreCase(result.get("test1").toString()));
	}

	@Test
	public void testEqualsIgnoreCaseFalseConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "foo";
		String const2 = "FOO";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.EQUALSIGNORECASE, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.EQ,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertFalse(const2.equalsIgnoreCase(result.get("test1").toString()));
	}

	@Test
	public void testStartsWithTrueConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "foo";
		String const2 = "test";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		IntegerConstant offs_expr = new IntegerConstant(2);
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(offs_expr);

		StringMultipleComparison strComp = new StringMultipleComparison(strVar,
				Operator.STARTSWITH, strConst, other, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.NE,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue((result.get("test1").toString()).startsWith(const2, 2));
	}

	@Test
	public void testStartsWithFalseConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "footest";
		String const2 = "test";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		IntegerConstant offs_expr = new IntegerConstant(3);
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(offs_expr);

		StringMultipleComparison strComp = new StringMultipleComparison(strVar,
				Operator.STARTSWITH, strConst, other, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.EQ,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertFalse((result.get("test1").toString()).startsWith(const2, 3));
	}

	@Test
	public void testEndsWithTrueConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "foo";
		String const2 = "test";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);

		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.ENDSWITH, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.NE,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue((result.get("test1").toString()).endsWith(const2));
	}

	@Test
	public void testEndsWithFalseConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "footest";
		String const2 = "test";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);

		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.ENDSWITH, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.EQ,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertFalse((result.get("test1").toString()).endsWith(const2));
	}

	@Test
	public void testContainsTrueConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "foo";
		String const2 = "test";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);

		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.CONTAINS, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.NE,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue((result.get("test1").toString()).contains(const2));
	}

	@Test
	public void testContainsFalseConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "fotesto";
		String const2 = "test";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);

		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.CONTAINS, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.EQ,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertFalse((result.get("test1").toString()).contains(const2));
	}

	@Test
	public void testRegionMatchesICTrueConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "fotesto";
		String const2 = "rtestooo";
		boolean ignore_case = true;
		int offset1 = 0;
		int offset2 = 1;
		int len = 4;

		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		IntegerConstant len_expr = new IntegerConstant(len);
		IntegerConstant offs_two = new IntegerConstant(offset2);
		IntegerConstant offs_one = new IntegerConstant(offset1);
		IntegerConstant ign_case = new IntegerConstant(ignore_case ? 1 : 0);

		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(offs_one);
		other.add(offs_two);
		other.add(len_expr);
		other.add(ign_case);

		StringMultipleComparison strComp = new StringMultipleComparison(strVar,
				Operator.REGIONMATCHES, strConst, other, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.NE,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue((result.get("test1").toString()).regionMatches(ignore_case,
				offset1, const2, offset2, len));
	}

	@Test
	public void testRegionMatchesICFalseConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "foTESTo";
		String const2 = "rtestooo";
		boolean ignore_case = true;
		int offset1 = 2;
		int offset2 = 1;
		int len = 4;

		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		IntegerConstant len_expr = new IntegerConstant(len);
		IntegerConstant offs_two = new IntegerConstant(offset2);
		IntegerConstant offs_one = new IntegerConstant(offset1);
		IntegerConstant ign_case = new IntegerConstant(ignore_case ? 1 : 0);

		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(offs_one);
		other.add(offs_two);
		other.add(len_expr);
		other.add(ign_case);

		StringMultipleComparison strComp = new StringMultipleComparison(strVar,
				Operator.REGIONMATCHES, strConst, other, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.EQ,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertFalse((result.get("test1").toString()).regionMatches(ignore_case,
				offset1, const2, offset2, len));
	}
	
	@Test
	public void testRegexMatchesTrue() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "test";
		String const2 = "TEST";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.PATTERNMATCHES, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.NE,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(result.get("test1").toString().matches(const2));
	}
	
	@Test
	public void testRegexMatchesFalse() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		String var1 = "test";
		String const2 = "TEST";
		StringVariable strVar = new StringVariable("test1", var1);
		StringConstant strConst = new StringConstant(const2);
		StringBinaryComparison strComp = new StringBinaryComparison(strVar,
				Operator.PATTERNMATCHES, strConst, 0L);
		constraints.add(new StringConstraint(strComp, Comparator.EQ,
				new IntegerConstant(0)));

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> result = skr.solve(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertFalse("Result should not match TEST: "+result.get("test1").toString(), result.get("test1").toString().matches(const2));
	}
}
