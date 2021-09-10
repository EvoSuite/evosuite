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

import com.examples.with.different.packagename.solver.TestCaseAbs;
import com.examples.with.different.packagename.solver.TestCaseMax;
import com.examples.with.different.packagename.solver.TestCaseMin;
import com.examples.with.different.packagename.solver.TestCaseNeg;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

public abstract class TestSolverMath extends TestSolver {

    private static DefaultTestCase buildTestCaseAbs() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(Integer.MIN_VALUE + 1);

        Method method = TestCaseAbs.class.getMethod("test", int.class);
        tc.appendMethod(null, method, int0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testAbs() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseAbs();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Long var0 = (Long) solution.get("var0");

        assertTrue(Math.abs(var0.intValue()) > 0);
    }

    private static DefaultTestCase buildTestCaseMax() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(Integer.MIN_VALUE);
        VariableReference int1 = tc.appendIntPrimitive(10);

        Method method = TestCaseMax.class.getMethod("test", int.class, int.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseNeg() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(-10);

        Method method = TestCaseNeg.class.getMethod("test", int.class);
        tc.appendMethod(null, method, int0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestCaseMin() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(Integer.MAX_VALUE);
        VariableReference int1 = tc.appendIntPrimitive(10);

        Method method = TestCaseMin.class.getMethod("test", int.class, int.class);
        tc.appendMethod(null, method, int0, int1);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testMax() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseMax();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Long var0 = (Long) solution.get("var0");
        Long var1 = (Long) solution.get("var1");

        assertEquals(10, Math.max(var0.intValue(), var1.intValue()));
    }

    @Test
    public void testMin() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseMin();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Long var0 = (Long) solution.get("var0");
        Long var1 = (Long) solution.get("var1");

        assertEquals(10, Math.min(var0.intValue(), var1.intValue()));
    }

    @Test
    public void testNeg() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCaseNeg();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);
        assertNotNull(solution);
        Long var0 = (Long) solution.get("var0");

        assertEquals(-10, var0.intValue());

    }
}
