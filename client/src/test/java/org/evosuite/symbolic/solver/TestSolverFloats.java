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

import static org.junit.Assert.*;

public abstract class TestSolverFloats extends TestSolver {

    private static DefaultTestCase buildTestCaseFraction() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference double0 = tc.appendDoublePrimitive(Math.PI - 3);

        Method method = TestCaseFloatFraction.class.getMethod("test", double.class);
        tc.appendMethod(null, method, double0);
        return tc.getDefaultTestCase();
    }

    private static final double DELTA = 1e-15;

    @Test
    public void testEq() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseEq();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertEquals(var0.intValue(), var1.intValue());
    }

    @Test
    public void testFraction() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseFraction();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");

        assertTrue(var0 > 0);
        assertTrue(var0 < 1);
    }

    @Test
    public void testGt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseGt();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertTrue(var0 > var1);
    }

    @Test
    public void testGte() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseGte();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertTrue(var0 >= var1);
    }

    @Test
    public void testLt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseLt();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertTrue(var0 < var1);
    }

    @Test
    public void testLte() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseLte();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertTrue(var0 <= var1);
    }

    @Test
    public void testNeq() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
        DefaultTestCase tc = buildTestCaseNeq();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertTrue(var0.intValue() != var1.intValue());
    }

    private static DefaultTestCase buildTestCaseEq() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference double0 = tc.appendDoublePrimitive(Math.PI);
        VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

        Method method = TestCaseFloatEq.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, double0, double1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseNeq() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference double0 = tc.appendDoublePrimitive(Math.PI);
        VariableReference double1 = tc.appendDoublePrimitive(Math.E);

        Method method = TestCaseFloatNeq.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, double0, double1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseLt() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendDoublePrimitive(Math.PI - 1);
        VariableReference int1 = tc.appendDoublePrimitive(Math.PI);

        Method method = TestCaseFloatLt.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseLte() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendDoublePrimitive(Math.PI - 1);
        VariableReference int1 = tc.appendDoublePrimitive(Math.PI);

        Method method = TestCaseFloatLte.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseGt() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendDoublePrimitive(Math.PI);
        VariableReference int1 = tc.appendDoublePrimitive(Math.PI - 1);

        Method method = TestCaseFloatGt.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseGte() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendDoublePrimitive(Math.PI);
        VariableReference int1 = tc.appendDoublePrimitive(Math.PI - 1);

        Method method = TestCaseFloatGte.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseFloatAdd() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference double0 = tc.appendDoublePrimitive(0);
        VariableReference double1 = tc.appendDoublePrimitive(Math.PI);

        Method method = TestCaseFloatAdd.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, double0, double1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseFloatSub() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference double0 = tc.appendDoublePrimitive(Math.PI);
        VariableReference double1 = tc.appendDoublePrimitive(0);

        Method method = TestCaseFloatSub.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, double0, double1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseFloatMul() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference double0 = tc.appendDoublePrimitive(2.2);
        VariableReference double1 = tc.appendDoublePrimitive(1.1);

        Method method = TestCaseFloatMul.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, double0, double1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseFloatDiv() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference double0 = tc.appendDoublePrimitive(1.1);
        VariableReference double1 = tc.appendDoublePrimitive(2.2);

        Method method = TestCaseFloatDiv.class.getMethod("test", double.class, double.class);
        tc.appendMethod(null, method, double0, double1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseFloatMod() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference double0 = tc.appendDoublePrimitive(10.0 % 3.0);

        Method method = TestCaseFloatMod.class.getMethod("test", double.class);
        tc.appendMethod(null, method, double0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testAdd() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseFloatAdd();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertEquals(Math.PI, var0 + var1, DELTA);
    }

    @Test
    public void testSub() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseFloatSub();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertEquals(Math.PI, var0 - var1, DELTA);
    }

    @Test
    public void testMul() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseFloatMul();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertTrue(var0 == var1 * 2.0);
    }

    @Test
    public void testDiv() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseFloatDiv();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");
        Double var1 = (Double) solution.get("var1");

        assertTrue(var0 == var1 / 2.0);
    }

    @Test
    public void testMod() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseFloatMod();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Double var0 = (Double) solution.get("var0");

        assertEquals(1.0, var0, DELTA);
    }

}
