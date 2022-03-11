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
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class TestSolverStringFunctions extends TestSolver {

    private static DefaultTestCase buildTestLength() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("01234");

        Method method = TestCaseStringLength.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringLength() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestLength();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testNegativeLength() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        IntegerConstraint newIntegerConstraint = new IntegerConstraint(
                new StringUnaryToIntegerExpression(new StringVariable("var0", "01234"), Operator.LENGTH, (long) 5),
                Comparator.LT, new IntegerConstant(0));

        Collection<Constraint<?>> constraints = Collections.<Constraint<?>>singleton(newIntegerConstraint);

        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestEquals() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("Hello World");

        Method method = TestCaseStringEquals.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestEqualsIgnoreCase() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("Hello World");

        Method method = TestCaseStringEqualsIgnoreCase.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestNotEquals() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("Not equal to Hello World");

        Method method = TestCaseStringNotEquals.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestStartsWith() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("Hello World");

        Method method = TestCaseStringStartsWith.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestEndsWith() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("Hello World");

        Method method = TestCaseStringEndsWith.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestConcat() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("ha");

        Method method = TestCaseStringConcat.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestAppendString() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("ha");

        Method method = TestCaseStringAppendString.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestStartsWithIndex() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive(".....Hello");

        Method method = TestCaseStringStartsWithIndex.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestIndexOfChar() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive(".....Xello");

        Method method = TestCaseStringIndexOfChar.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestIndexOfCharInt() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("H....Xello");

        Method method = TestCaseStringIndexOfCharInt.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestIndexOfStringInt() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("H....Xello");

        Method method = TestCaseStringIndexOfStringInt.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestIndexOfString() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive(".....Xello");

        Method method = TestCaseStringIndexOfString.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringEquals() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestEquals();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringEqualsIgnoreCase() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestEqualsIgnoreCase();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringNotEquals() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestNotEquals();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringStartsWith() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestStartsWith();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringStartsWithIndex() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestStartsWithIndex();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringEndsWith() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
        DefaultTestCase tc = buildTestEndsWith();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestCharAt() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("XHello");

        Method method = TestCaseStringCharAt.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringCharAt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {
        DefaultTestCase tc = buildTestCharAt();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestContains() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("xxxHelloyyyyy");

        Method method = TestCaseStringContains.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringContains() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestContains();

        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringIndexOfChar() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestIndexOfChar();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringIndexOfCharInt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestIndexOfCharInt();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringIndexOfString() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestIndexOfString();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringIndexOfStringInt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestIndexOfStringInt();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestTrim() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("   Hello World   ");

        Method method = TestCaseStringTrim.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestLowerCase() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("Hello World");

        Method method = TestCaseStringLowerCase.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestUpperCase() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("Hello worlD");

        Method method = TestCaseStringUpperCase.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringTrim() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestTrim();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringAppendString() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestAppendString();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringSubstringFromTo() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestSubstringFromTo();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringUpperCase() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestUpperCase();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringLowerCase() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestLowerCase();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestLastIndexOfChar() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive(".....Xello");

        Method method = TestCaseStringLastIndexOfChar.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestLastIndexOfCharInt() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("H....Xello");

        Method method = TestCaseStringLastIndexOfCharInt.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestLastIndexOfStringInt() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("H....Xello");

        Method method = TestCaseStringLastIndexOfStringInt.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestLastIndexOfString() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive(".....Xello");

        Method method = TestCaseStringLastIndexOfString.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringLastIndexOfChar() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestLastIndexOfChar();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringLastIndexOfCharInt() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestLastIndexOfCharInt();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringLastIndexOfString() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestLastIndexOfString();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringLastIndexOfStringInt()
            throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestLastIndexOfStringInt();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestSubstring() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("unhappy");

        Method method = TestCaseStringSubstring.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestSubstringFromTo() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("hamburger");

        Method method = TestCaseStringSubstringFromTo.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringSubstring() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestSubstring();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringConcat() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestConcat();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestReplaceChar() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("happx");

        Method method = TestCaseStringReplaceChar.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestReplaceFirst() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("happx");

        Method method = TestCaseStringReplaceFirst.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestReplaceCharSequence() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("happx");

        Method method = TestCaseStringReplaceCharSequence.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringReplaceChar() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestReplaceChar();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringReplaceCharSequence()
            throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestReplaceCharSequence();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringReplaceFirst() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestReplaceFirst();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestCompareTo() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("Hello");

        Method method = TestCaseStringCompareTo.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringCompareTo() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestCompareTo();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestStringToInteger() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("1200");

        Method method = TestCaseStringToInteger.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringToInteger() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestStringToInteger();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testIntegerToString() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestIntegerToString();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    private static DefaultTestCase buildTestIntegerToString() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendIntPrimitive(1200);

        Method method = TestCaseIntegerToString.class.getMethod("test", int.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestAppendBoolean() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("ha");

        Method method = TestCaseStringAppendBoolean.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestAppendChar() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("ha");

        Method method = TestCaseStringAppendChar.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestAppendInteger() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("ha");

        Method method = TestCaseStringAppendInteger.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    private static DefaultTestCase buildTestAppendFloat() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference string0 = tc.appendStringPrimitive("ha");

        Method method = TestCaseStringAppendFloat.class.getMethod("test", String.class);
        tc.appendMethod(null, method, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testStringAppendChar() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestAppendChar();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringAppendBoolean() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestAppendBoolean();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringAppendFloat() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestAppendFloat();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

    @Test
    public void testStringAppendInteger() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        DefaultTestCase tc = buildTestAppendInteger();
        Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
        Map<String, Object> solution = solve(getSolver(), constraints);

    }

}
