/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.RandomizedTC;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringUnaryExpression;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.evosuite.symbolic.solver.TestSolver.solve;
import static org.junit.Assert.*;

public class TestStringSearch extends RandomizedTC {

    @Test
    public void testEqualsTrueConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "foo";
        String const2 = "test";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);
        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.EQUALS, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.NE, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertEquals(const2, result.get("test1").toString());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testEqualsFalseConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "foo";
        String const2 = "foo";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);
        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.EQUALS, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.EQ, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertFalse(const2.equals(result.get("test1").toString()));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testEqualsIgnoreCaseTrueConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "foo";
        String const2 = "Fest";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);
        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.EQUALSIGNORECASE, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.NE, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(const2.equalsIgnoreCase(result.get("test1").toString()));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testEqualsIgnoreCaseFalseConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "foo";
        String const2 = "FOO";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);
        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.EQUALSIGNORECASE, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.EQ, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertFalse(const2.equalsIgnoreCase(result.get("test1").toString()));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testStartsWithTrueConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "foo";
        String const2 = "test";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);
        IntegerConstant offs_expr = new IntegerConstant(2);
        ArrayList<Expression<?>> other = new ArrayList<>();
        other.add(offs_expr);

        StringMultipleComparison strComp = new StringMultipleComparison(strVar, Operator.STARTSWITH, strConst, other,
                0L);
        constraints.add(new StringConstraint(strComp, Comparator.NE, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue((result.get("test1").toString()).startsWith(const2, 2));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testStartsWithFalseConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "footest";
        String const2 = "test";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);
        IntegerConstant offs_expr = new IntegerConstant(3);
        ArrayList<Expression<?>> other = new ArrayList<>();
        other.add(offs_expr);

        StringMultipleComparison strComp = new StringMultipleComparison(strVar, Operator.STARTSWITH, strConst, other,
                0L);
        constraints.add(new StringConstraint(strComp, Comparator.EQ, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertFalse((result.get("test1").toString()).startsWith(const2, 3));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testEndsWithTrueConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "foo";
        String const2 = "test";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);

        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.ENDSWITH, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.NE, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue((result.get("test1").toString()).endsWith(const2));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testEndsWithFalseConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "footest";
        String const2 = "test";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);

        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.ENDSWITH, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.EQ, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertFalse((result.get("test1").toString()).endsWith(const2));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testContainsTrueConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "foo";
        String const2 = "test";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);

        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.CONTAINS, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.NE, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue((result.get("test1").toString()).contains(const2));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testContainsFalseConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "fotesto";
        String const2 = "test";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);

        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.CONTAINS, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.EQ, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertFalse((result.get("test1").toString()).contains(const2));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testRegionMatchesICTrueConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "teXto";
        String const2 = "rtestooo";
        boolean ignore_case = true;
        int offset1 = 0;
        int offset2 = 1;
        int len = 4;

        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);
        IntegerConstant len_expr = new IntegerConstant(len);
        IntegerConstant offs_one = new IntegerConstant(offset1);
        IntegerConstant offs_two = new IntegerConstant(offset2);
        IntegerConstant ign_case = new IntegerConstant(ignore_case ? 1 : 0);

        ArrayList<Expression<?>> other = new ArrayList<>();
        other.add(offs_one);
        other.add(offs_two);
        other.add(len_expr);
        other.add(ign_case);

        StringMultipleComparison strComp = new StringMultipleComparison(strVar, Operator.REGIONMATCHES, strConst, other,
                0L);
        constraints.add(new StringConstraint(strComp, Comparator.NE, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue((result.get("test1").toString()).regionMatches(ignore_case, offset1, const2, offset2, len));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testRegionMatchesICFalseConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
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

        ArrayList<Expression<?>> other = new ArrayList<>();
        other.add(offs_one);
        other.add(offs_two);
        other.add(len_expr);
        other.add(ign_case);

        StringMultipleComparison strComp = new StringMultipleComparison(strVar, Operator.REGIONMATCHES, strConst, other,
                0L);
        constraints.add(new StringConstraint(strComp, Comparator.EQ, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertFalse((result.get("test1").toString()).regionMatches(ignore_case, offset1, const2, offset2, len));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testInversionOfRegex() {
        List<Constraint<?>> constraints = new ArrayList<>();

        String var = "a+";
        String regex = "aaa";

        // so we need to solve it
        assertFalse(var.matches(regex));

        String variableName = "test1";

        StringVariable strVar = new StringVariable(variableName, var);
        StringConstant strConst = new StringConstant(regex);

        StringBinaryComparison strComp = new StringBinaryComparison(strConst, Operator.PATTERNMATCHES, strVar, 0L);

        // the constraint should evaluate to true
        constraints.add(new StringConstraint(strComp, Comparator.NE, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get(variableName));
            String solution = result.get(variableName).toString();
            assertTrue(solution.matches(regex));
            /*
             * as the regex defines only one possible matching string, then the
             * solution has to be equal to the regex
             */
            assertEquals(regex, solution);
        } catch (SolverTimeoutException e) {
            fail();
        }

        // now let's invert them
        strVar = new StringVariable(variableName, regex);
        strConst = new StringConstant(var);

        // the inversion should match immediately
        assertTrue(regex.matches(var));

        // recreate the same type of constraint
        strComp = new StringBinaryComparison(strConst, Operator.PATTERNMATCHES, strVar, 0L);
        constraints.clear();
        constraints.add(new StringConstraint(strComp, Comparator.NE, new IntegerConstant(0)));

        try {
            result = solve(skr, constraints);
            assertNotNull(result);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testRegexMatchesTrue() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "test";
        String const2 = "TEST";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);
        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.PATTERNMATCHES, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.NE, new IntegerConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(result.get("test1").toString().matches(const2));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testChopOffIndexOfC() {
        String var1value = "D<E\u001Exqaa:saksajij1§n";
        StringVariable var1 = new StringVariable("var1", var1value);

        IntegerConstant colon_code = new IntegerConstant(58);
        IntegerConstant minus_one = new IntegerConstant(-1);

        int colon_int_code = ':';
        int concrete_value = var1value.indexOf(colon_int_code);
        StringBinaryToIntegerExpression index_of_colon = new StringBinaryToIntegerExpression(var1, Operator.INDEXOFC,
                colon_code, (long) concrete_value);

        IntegerConstraint constr1 = new IntegerConstraint(index_of_colon, Comparator.EQ, minus_one);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(constr1);

        EvoSuiteSolver solver = new EvoSuiteSolver();
        Map<String, Object> solution;
        try {
            solution = solve(solver, constraints);
            assertNotNull(solution);
        } catch (SolverTimeoutException e) {
            fail();
        }

    }

    @Test
    public void testInsertIndexOfC() {
        String var1value = "D<E\u001Exqaasaksajij1§n";
        StringVariable var1 = new StringVariable("var1", var1value);

        IntegerConstant colon_code = new IntegerConstant(58);
        IntegerConstant minus_one = new IntegerConstant(-1);

        int colon_int_code = ':';
        int concrete_value = var1value.indexOf(colon_int_code);
        StringBinaryToIntegerExpression index_of_colon = new StringBinaryToIntegerExpression(var1, Operator.INDEXOFC,
                colon_code, (long) concrete_value);

        IntegerConstraint constr1 = new IntegerConstraint(index_of_colon, Comparator.NE, minus_one);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(constr1);

        EvoSuiteSolver solver = new EvoSuiteSolver();
        Map<String, Object> solution;
        try {
            solution = solve(solver, constraints);
            assertNotNull(solution);
        } catch (SolverTimeoutException e) {
            fail();
        }

    }

    @Test
    public void testIndexOfC() {
        String var1value = "D<E\u001E";
        StringVariable var1 = new StringVariable("var1", var1value);

        IntegerConstant colon_code = new IntegerConstant(35);
        IntegerConstant numeral_code = new IntegerConstant(58);
        IntegerConstant minus_one = new IntegerConstant(-1);

        StringBinaryToIntegerExpression index_of_colon = new StringBinaryToIntegerExpression(var1, Operator.INDEXOFC,
                colon_code, -1L);
        StringBinaryToIntegerExpression index_of_numeral = new StringBinaryToIntegerExpression(var1, Operator.INDEXOFC,
                numeral_code, -1L);

        IntegerConstraint constr1 = new IntegerConstraint(index_of_colon, Comparator.EQ, minus_one);
        IntegerConstraint constr2 = new IntegerConstraint(index_of_numeral, Comparator.NE, minus_one);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(constr1);
        constraints.add(constr2);

        EvoSuiteSolver solver = new EvoSuiteSolver();
        Map<String, Object> solution;
        try {
            solution = solve(solver, constraints);
            assertNotNull(solution);
        } catch (SolverTimeoutException e) {
            fail();
        }

    }

    @Test
    public void testRegexMatchesFalse() {
        List<Constraint<?>> constraints = new ArrayList<>();
        String var1 = "testsomestring";
        String const2 = "testsomestring";
        StringVariable strVar = new StringVariable("test1", var1);
        StringConstant strConst = new StringConstant(const2);
        StringBinaryComparison strComp = new StringBinaryComparison(strVar, Operator.PATTERNMATCHES, strConst, 0L);
        constraints.add(new StringConstraint(strComp, Comparator.EQ, new IntegerConstant(0)));
        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertFalse("Result should not match TEST: " + result.get("test1").toString(),
                    result.get("test1").toString().matches(const2));
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testEqualsToLowerCase() {
        // (wed equals var1("f|").toLowerCase()) != 0
        StringVariable var1 = new StringVariable("var1", "f|");

        StringBinaryComparison cmp3 = new StringBinaryComparison(new StringConstant("wed"), Operator.EQUALS,
                new StringUnaryExpression(var1, Operator.TOLOWERCASE, "f|".toLowerCase()), 0L);

        StringConstraint constr3 = new StringConstraint(cmp3, Comparator.NE, new IntegerConstant(0));

        Collection<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(constr3);

        EvoSuiteSolver solver = new EvoSuiteSolver();
        Map<String, Object> solution;
        try {
            solution = solve(solver, constraints);

            assertNotNull(solution);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testIndexOfC2() {

        String var1value = ":cc]#0l";
        StringVariable var1 = new StringVariable("var0", var1value);

        IntegerConstant colon_code = new IntegerConstant(58);
        IntegerConstant numeral_code = new IntegerConstant(35);
        IntegerConstant minus_one = new IntegerConstant(-1);

        StringBinaryToIntegerExpression index_of_colon = new StringBinaryToIntegerExpression(var1, Operator.INDEXOFC,
                colon_code, -1L);
        StringBinaryToIntegerExpression index_of_numeral = new StringBinaryToIntegerExpression(var1, Operator.INDEXOFC,
                numeral_code, -1L);

        /*
         * Here we are trying to modify the string such that the first '#' comes
         * before the first ':', and both are present
         */
        IntegerConstraint constr1 = new IntegerConstraint(index_of_colon, Comparator.NE, minus_one);
        IntegerConstraint constr2 = new IntegerConstraint(index_of_numeral, Comparator.NE, minus_one);
        IntegerConstraint constr3 = new IntegerConstraint(index_of_numeral, Comparator.LT, index_of_colon);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(constr1);
        constraints.add(constr2);
        constraints.add(constr3);

        EvoSuiteSolver solver = new EvoSuiteSolver();
        Map<String, Object> solution = null;
        try {
            /*
             * The constraint is not trivial, as there are search plateaus. So
             * it is ok if sometimes it fails (tried 10 times, failed 3).
             */
            final int TRIES = 20;
            for (int i = 0; i < TRIES; i++) {
                solution = solve(solver, constraints);
                if (solution != null) {
                    break;
                }
            }
            assertNotNull(solution);
            String result = solution.get("var0").toString();
            int colonPos = result.indexOf(':');
            int numeralPos = result.indexOf('#');
            assertTrue("Colon not found in " + result, colonPos >= 0);
            assertTrue("Numeral not found in " + result, numeralPos >= 0);
            assertTrue(colonPos > numeralPos);
        } catch (SolverTimeoutException e) {
            fail();
        }

    }
}
