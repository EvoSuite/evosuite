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
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.constraint.RealConstraint;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealConstant;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.solver.DistanceEstimator;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.evosuite.symbolic.solver.TestSolver.solve;
import static org.junit.Assert.*;

public class TestRealSearch extends RandomizedTC {

    @Test
    public void testEQConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 0.675464, Float.MIN_VALUE, Float.MAX_VALUE),
                Comparator.EQ, new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertEquals(2.35082, ((Number) result.get("test1")).doubleValue(), 0.0);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testNEConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 2.35082, -1000000.0, 1000000.0), Comparator.NE,
                new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(2.35082 != ((Number) result.get("test1")).doubleValue());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testLEConstant() {

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 5.35086, -1000000.0, 1000000.0), Comparator.LE,
                new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(2.35082 >= ((Number) result.get("test1")).doubleValue());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testLTConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 5.35086, -1000000.0, 1000000.0), Comparator.LT,
                new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(2.35082 > ((Number) result.get("test1")).doubleValue());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testGEConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0, 1000000.0), Comparator.GE,
                new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(2.35082 <= ((Number) result.get("test1")).doubleValue());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testGTConstant() {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0, 1000000.0), Comparator.GT,
                new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(2.35082 < ((Number) result.get("test1")).doubleValue());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testEQConstantAfterComma() {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0, 1000000.0), Comparator.EQ,
                new RealConstant(0.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertEquals(0.35082, ((Number) result.get("test1")).doubleValue(), 0.0);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testLEConstantAfterComma() {

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 2.35086, -1000000.0, 1000000.0), Comparator.LE,
                new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(2.35082 >= ((Number) result.get("test1")).doubleValue());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testLTConstantAfterComma() {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 2.35086, -1000000.0, 1000000.0), Comparator.LT,
                new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(2.35082 > ((Number) result.get("test1")).doubleValue());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testGEConstantAfterComma() {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 2.0, -1000000.0, 1000000.0), Comparator.GE,
                new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(2.35082 <= ((Number) result.get("test1")).doubleValue());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testGTConstantAfterComma() {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", 2.0, -1000000.0, 1000000.0), Comparator.GT,
                new RealConstant(2.35082)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            assertNotNull(result.get("test1"));
            assertTrue(2.35082 < ((Number) result.get("test1")).doubleValue());
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testEQVariable() {
        double var1 = 0.23123;
        double var2 = 1.12321;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0, 1000000.0), Comparator.EQ,
                new RealVariable("test2", var2, -1000000.0, 1000000.0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            if (result.containsKey("test2"))
                var2 = ((Number) result.get("test2")).doubleValue();
            // assertTrue(var1 == var2);
            assertEquals(var1, var2, 0.001);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testNEVariable() {
        double var1 = 1.5546;
        double var2 = 1.5546;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0, 1000000.0), Comparator.NE,
                new RealVariable("test2", var2, -1000000.0, 1000000.0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            if (result.containsKey("test2"))
                var2 = ((Number) result.get("test2")).doubleValue();
            assertTrue(var1 != var2);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testLEVariable() {
        double var1 = 2.6576;
        double var2 = 1.434;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0, 1000000.0), Comparator.LE,
                new RealVariable("test2", var2, -1000000.0, 1000000.0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            if (result.containsKey("test2"))
                var2 = ((Number) result.get("test2")).doubleValue();
            assertTrue(var1 <= var2);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testLTVariable() {
        double var1 = 2.6576;
        double var2 = 1.434;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0, 1000000.0), Comparator.LT,
                new RealVariable("test2", var2, -1000000.0, 1000000.0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            if (result.containsKey("test2"))
                var2 = ((Number) result.get("test2")).doubleValue();
            assertTrue(var1 < var2);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testGEVariable() {
        double var1 = 0.7868;
        double var2 = 1.9765;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0, 1000000.0), Comparator.GE,
                new RealVariable("test2", var2, -1000000.0, 1000000.0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            if (result.containsKey("test2"))
                var2 = ((Number) result.get("test2")).doubleValue();
            assertTrue(var1 >= var2);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testGTVariable() {
        double var1 = 0.7868;
        double var2 = 1.9765;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0, 1000000.0), Comparator.GT,
                new RealVariable("test2", var2, -1000000.0, 1000000.0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            if (result.containsKey("test2"))
                var2 = ((Number) result.get("test2")).doubleValue();
            assertTrue(var1 > var2);
        } catch (SolverTimeoutException e) {
            fail();
        }
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

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(realVar1, Comparator.LE, new RealConstant(0)));
        constraints.add(new RealConstraint(realVar1, Comparator.LT, realVar2));
        constraints.add(new RealConstraint(realVar1, Comparator.GE, new RealConstant(0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            if (result.containsKey("test2"))
                var2 = ((Number) result.get("test2")).doubleValue();
            assertEquals(0, var1, 0.0001);
            assertTrue(var1 < var2);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    @Test
    public void testEvosuiteExample2() {
        double var1 = 355.80758027529504;
        // var3__SYM(355.80758027529504) >= 0.0 dist: 177.90379013764752
        // var3__SYM(355.80758027529504) == 0.0 dist: 177.90379013764752

        RealVariable realVar = new RealVariable("test1", var1, -1000000, 1000000);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(realVar, Comparator.GE, new RealConstant(0.0)));
        constraints.add(new RealConstraint(realVar, Comparator.EQ, new RealConstant(0.0)));

        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            assertEquals(0, var1, 0.0001);
        } catch (SolverTimeoutException e) {
            fail();
        }
    }

    // @Test
    public void testEvosuiteExample3() {
        // ((1102.5 + var22__SYM(12.220999717712402)) *
        // var19__SYM(-45.633541107177734)) == 2.772399987618165E32
        double var1 = 12.220999717712402;
        double var2 = -45.633541107177734;

        RealVariable realVar1 = new RealVariable("test1", var1, -1000000, 1000000);
        RealVariable realVar2 = new RealVariable("test2", var2, -1000000, 1000000);
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new RealConstraint(
                new RealBinaryExpression(new RealBinaryExpression(new RealConstant(1102.5), Operator.PLUS, realVar1,
                        1.22209997177135E16), Operator.MUL, realVar2, -5.57687492989087E32),
                Comparator.EQ, new RealConstant(2.772399987618165E32)));

        assert (DistanceEstimator.getDistance(constraints) > 0);
        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            if (result.containsKey("test2"))
                var2 = ((Number) result.get("test2")).doubleValue();
            assertEquals(var1, var2, 0.0001);
        } catch (SolverTimeoutException e) {
            fail();
        }

    }

    @Test
    public void testAddition() {
        double var1 = 1.0;

        RealVariable realVar1 = new RealVariable("test1", var1, Double.MIN_VALUE, Double.MAX_VALUE);
        List<Constraint<?>> constraints = new ArrayList<>();
        final double doubleValue = 2000.087658834634;

        constraints.add(
                new RealConstraint(new RealBinaryExpression(new RealConstant(1102.5), Operator.PLUS, realVar1, 1103.5),
                        Comparator.EQ, new RealConstant(doubleValue)));

        assert (DistanceEstimator.getDistance(constraints) > 0);
        EvoSuiteSolver skr = new EvoSuiteSolver();
        Map<String, Object> result;
        try {
            result = solve(skr, constraints);
            assertNotNull(result);
            if (result.containsKey("test1"))
                var1 = ((Number) result.get("test1")).doubleValue();
            // assertEquals(var1, var2, 0.0001);
        } catch (SolverTimeoutException e) {
            fail();
        }

    }
}
