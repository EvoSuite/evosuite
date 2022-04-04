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
package org.evosuite.symbolic.solver;

import com.examples.with.different.packagename.solver.*;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class TestSolverBitwise extends TestSolver {

    private static DefaultTestCase buildTestCaseBitAnd() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(10 & 1);
        VariableReference int1 = tc.appendIntPrimitive(10);

        Method method = TestCaseBitAnd.class.getMethod("test", int.class, int.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseBitOr() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(10 | 1);
        VariableReference int1 = tc.appendIntPrimitive(10);

        Method method = TestCaseBitOr.class.getMethod("test", int.class, int.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseBitXor() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(10 ^ 1);
        VariableReference int1 = tc.appendIntPrimitive(10);

        Method method = TestCaseBitXor.class.getMethod("test", int.class, int.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseBitNot() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(~10);
        VariableReference int1 = tc.appendIntPrimitive(10);

        Method method = TestCaseBitNot.class.getMethod("test", int.class, int.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testBitAnd() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        Solver solver = getSolver();
        DefaultTestCase tc = buildTestCaseBitAnd();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(solver, constraints);
        assertNotNull(solution);
        Long var0 = (Long) solution.get("var0");
        Long var1 = (Long) solution.get("var1");

        assertEquals(var0.intValue(), (var1.intValue() & 1));
    }

    @Test
    public void testBitOr() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        Solver solver = getSolver();
        DefaultTestCase tc = buildTestCaseBitOr();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(solver, constraints);
    }

    @Test
    public void testBitXor() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        try {

            Solver solver = getSolver();

            DefaultTestCase tc = buildTestCaseBitXor();
            Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
            Map<String, Object> solution = solve(solver, constraints);
            assertNotNull(solution);
            Long var0 = (Long) solution.get("var0");
            Long var1 = (Long) solution.get("var1");

            assertEquals(var0.intValue(), (var1.intValue() ^ 1));
        } catch (SolverTimeoutException ex) {

        }
    }

    @Test
    public void testBitNot() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        Solver solver = getSolver();

        DefaultTestCase tc = buildTestCaseBitNot();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(solver, constraints);
    }

    @Test
    public void testShiftLeft() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
        Solver solver = getSolver();

        DefaultTestCase tc = buildTestCaseShiftLeft();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(solver, constraints);

    }

    @Test
    public void testShiftRight() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
        Solver solver = getSolver();

        DefaultTestCase tc = buildTestCaseShiftRight();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(solver, constraints);
        assertNotNull(solution);
        Long var0 = (Long) solution.get("var0");
        Long var1 = (Long) solution.get("var1");

        assertEquals(var0.intValue(), var1.intValue() >> 1);
    }

    @Test
    public void testShiftRightUnsigned() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        Solver solver = getSolver();

        DefaultTestCase tc = buildTestCaseShiftRightUnsigned();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(solver, constraints);
        assertNotNull(solution);
        Long var0 = (Long) solution.get("var0");
        Long var1 = (Long) solution.get("var1");

        assertEquals(var0.intValue(), var1.intValue() >>> 1);
    }

    private static DefaultTestCase buildTestCaseShiftLeft() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(10 << 1);
        VariableReference int1 = tc.appendIntPrimitive(10);

        Method method = TestCaseShiftLeft.class.getMethod("test", int.class, int.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseShiftRight() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(10 >> 1);
        VariableReference int1 = tc.appendIntPrimitive(10);

        Method method = TestCaseShiftRight.class.getMethod("test", int.class, int.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseShiftRightUnsigned() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(10 >>> 1);
        VariableReference int1 = tc.appendIntPrimitive(10);

        Method method = TestCaseShiftRightUnsigned.class.getMethod("test", int.class, int.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }
}
