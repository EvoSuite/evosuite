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
package org.evosuite.junit.naming.methods;

import com.examples.with.different.packagename.ClassWithOverloadedConstructor;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.io.input.InputCoverageGoal;
import org.evosuite.coverage.io.input.InputCoverageTestFitness;
import org.evosuite.coverage.io.output.OutputCoverageGoal;
import org.evosuite.coverage.io.output.OutputCoverageTestFitness;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageTestFitness;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.runtime.mock.java.lang.MockArithmeticException;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.evosuite.coverage.io.IOCoverageConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by gordon on 22/12/2015.
 */
public class TestCoverageGoalNameGeneration {

    @Test
    public void testUniqueMethod() {
        TestCase test = new DefaultTestCase();
        MethodCoverageTestFitness goal = new MethodCoverageTestFitness("FooClass", "toString");
        test.addCoveredGoal(goal);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testToString", generatedName);
    }

    @Test
    public void testUniqueConstructor() {
        TestCase test = new DefaultTestCase();
        MethodCoverageTestFitness goal = new MethodCoverageTestFitness("FooClass", "<init>");
        test.addCoveredGoal(goal);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testCreatesFooClass", generatedName);
    }

    @Test
    public void testTwoTestsUniqueMethods() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString");
        test1.addCoveredGoal(goal1);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "getSomeStuff");
        test2.addCoveredGoal(goal2);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToString", naming.getName(test1));
        assertEquals("testGetSomeStuff", naming.getName(test2));
    }

    @Test
    public void testTwoTestsMethodsWithSharedGoals() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString");
        test1.addCoveredGoal(goal1);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "getSomeStuff");
        test2.addCoveredGoal(goal2);

        MethodCoverageTestFitness goal3 = new MethodCoverageTestFitness("FooClass", "doFooBar");
        test1.addCoveredGoal(goal3);
        test2.addCoveredGoal(goal3);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToString", naming.getName(test1));
        assertEquals("testGetSomeStuff", naming.getName(test2));
    }

    @Test
    public void testMethodWithAndWithoutException() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString()");
        test1.addCoveredGoal(goal1);
        MethodNoExceptionCoverageTestFitness goal1a = new MethodNoExceptionCoverageTestFitness("FooClass", "toString()");
        test1.addCoveredGoal(goal1a);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal1);
        ExceptionCoverageTestFitness goal2 = new ExceptionCoverageTestFitness("FooClass", "toString()", RuntimeException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test2.addCoveredGoal(goal2);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToString", naming.getName(test1));
        assertEquals("testToStringThrowsRuntimeException", naming.getName(test2));
    }

    @Test
    public void testMethodWithAndWithoutMockException() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString()");
        test1.addCoveredGoal(goal1);
        MethodNoExceptionCoverageTestFitness goal1a = new MethodNoExceptionCoverageTestFitness("FooClass", "toString()");
        test1.addCoveredGoal(goal1a);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal1);
        ExceptionCoverageTestFitness goal2 = new ExceptionCoverageTestFitness("FooClass", "toString()", MockArithmeticException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test2.addCoveredGoal(goal2);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToString", naming.getName(test1));
        assertEquals("testToStringThrowsArithmeticException", naming.getName(test2));
    }


    @Test
    public void testMethodWithExceptions() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString()");
        test1.addCoveredGoal(goal1);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "toString2()");
        test2.addCoveredGoal(goal2);

        ExceptionCoverageTestFitness goal3 = new ExceptionCoverageTestFitness("FooClass", "toString()", MockArithmeticException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test1.addCoveredGoal(goal3);
        test2.addCoveredGoal(goal3);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToString", naming.getName(test1));
        assertEquals("testToString2", naming.getName(test2));
    }

    @Test
    public void testConstructorWithAndWithoutException() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "<init>()");
        test1.addCoveredGoal(goal1);
        MethodNoExceptionCoverageTestFitness goal1a = new MethodNoExceptionCoverageTestFitness("FooClass", "<init>()");
        test1.addCoveredGoal(goal1a);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal1);
        ExceptionCoverageTestFitness goal2 = new ExceptionCoverageTestFitness("FooClass", "<init>()", RuntimeException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test2.addCoveredGoal(goal2);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testCreatesFooClass", naming.getName(test1));
        assertEquals("testFailsToCreateFooClassThrowsRuntimeException", naming.getName(test2));
    }

    @Test
    public void testMethodWithOutputGoals() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString");
        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "toString", objectType(), REF_NULL);
        OutputCoverageTestFitness goal2 = new OutputCoverageTestFitness(outputGoal1);
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal1);
        OutputCoverageGoal outputGoal2 = new OutputCoverageGoal("FooClass", "toString", objectType(), REF_NONNULL);
        OutputCoverageTestFitness goal3 = new OutputCoverageTestFitness(outputGoal2);
        test2.addCoveredGoal(goal3);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToStringReturningNull", naming.getName(test1));
        assertEquals("testToStringReturningNonNull", naming.getName(test2));
    }

    @Test
    public void testMethodWithInputGoals() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString");
        InputCoverageGoal inputGoal1 = new InputCoverageGoal("FooClass", "toString", 0, objectType(), REF_NULL);
        InputCoverageTestFitness goal2 = new InputCoverageTestFitness(inputGoal1);
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal1);
        InputCoverageGoal inputGoal2 = new InputCoverageGoal("FooClass", "toString", 0, objectType(), REF_NONNULL);
        InputCoverageTestFitness goal3 = new InputCoverageTestFitness(inputGoal2);
        test2.addCoveredGoal(goal3);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToStringWithNull", naming.getName(test1));
        assertEquals("testToStringWithNonNull", naming.getName(test2));
    }

    @Test
    public void testMethodWithInputOutputGoals() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString");
        InputCoverageGoal inputGoal1 = new InputCoverageGoal("FooClass", "toString", 0, objectType(), REF_NULL);
        InputCoverageTestFitness goal2 = new InputCoverageTestFitness(inputGoal1);
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal1);
        OutputCoverageGoal outputGoal2 = new OutputCoverageGoal("FooClass", "toString", objectType(), REF_NONNULL);
        OutputCoverageTestFitness goal3 = new OutputCoverageTestFitness(outputGoal2);
        test2.addCoveredGoal(goal3);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToStringWithNull", naming.getName(test1));
        assertEquals("testToStringReturningNonNull", naming.getName(test2));
    }

    @Test
    public void testOverloadedMethod() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "foo()");
        test1.addCoveredGoal(goal1);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "foo(I)");
        test2.addCoveredGoal(goal2);

        TestCase test3 = new DefaultTestCase();
        test3.addStatement(new IntPrimitiveStatement(test3, 0)); // Need to add statements to change hashCode
        MethodCoverageTestFitness goal3 = new MethodCoverageTestFitness("FooClass", "foo(II)");
        test3.addCoveredGoal(goal3);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooTakingNoArguments", naming.getName(test1));
        assertEquals("testFooTakingInt", naming.getName(test2));
        assertEquals("testFooTaking2Arguments", naming.getName(test3));
    }

    @Test
    public void testOverloadedMethodDifferentArgArrayType() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "foo([B)");
        test1.addCoveredGoal(goal1);
        OutputCoverageTestFitness outputGoal = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "foo", stringType(), BOOL_FALSE));
        test1.addCoveredGoal(outputGoal);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "foo([I)");
        test2.addCoveredGoal(goal2);
        test2.addCoveredGoal(outputGoal);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooTakingByteArray", naming.getName(test1));
        assertEquals("testFooTakingIntArray", naming.getName(test2));
    }

    @Test
    public void testMultipleRedundantGoals() {
        MethodCoverageTestFitness methodGoal1 = new MethodCoverageTestFitness("FooClass", "foo(SS)Z");
        MethodCoverageTestFitness methodGoal2 = new MethodCoverageTestFitness("FooClass", "foo(Ljava/lang/Object;Ljava/lang/Object;)Z");

        OutputCoverageTestFitness outputGoal1 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "foo(SS)Z", Type.BOOLEAN_TYPE, BOOL_FALSE));
        OutputCoverageTestFitness outputGoal2 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "foo(Ljava/lang/Object;Ljava/lang/Object;)Z", Type.BOOLEAN_TYPE, BOOL_FALSE));

        TestCase test1 = new DefaultTestCase();
        test1.addCoveredGoal(methodGoal1);
        test1.addCoveredGoal(outputGoal1);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(methodGoal2);
        test2.addCoveredGoal(outputGoal2);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooTaking2Shorts", naming.getName(test1));
        assertEquals("testFooTaking2Objects", naming.getName(test2));
    }

    @Test
    public void testOverloadedMethodWithObject() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "foo()");
        test1.addCoveredGoal(goal1);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "foo(Ljava/util/List;)");
        test2.addCoveredGoal(goal2);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooTakingNoArguments", naming.getName(test1));
        assertEquals("testFooTakingList", naming.getName(test2));
    }

    @Test
    public void testOverloadedMethodWithArray() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "foo(I)");
        test1.addCoveredGoal(goal1);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "foo([I)");
        test2.addCoveredGoal(goal2);

        TestCase test3 = new DefaultTestCase();
        test3.addStatement(new IntPrimitiveStatement(test3, 0)); // Need to add statements to change hashCode
        MethodCoverageTestFitness goal3 = new MethodCoverageTestFitness("FooClass", "foo([[I)");
        test3.addCoveredGoal(goal3);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooTakingInt", naming.getName(test1));
        assertEquals("testFooTakingIntArray", naming.getName(test2));
        assertEquals("testFooTakingIntArrayArray", naming.getName(test3));
    }


    @Test
    public void testTwoTestsDifferOnlyInBranches() {
        Branch b1 = mock(Branch.class);
        BytecodeInstruction bi = mock(BytecodeInstruction.class);
        when(b1.getInstruction()).thenReturn(bi);
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness methodGoal = new MethodCoverageTestFitness("FooClass", "toString");
        test1.addCoveredGoal(methodGoal);
        BranchCoverageTestFitness branchGoal1 = new BranchCoverageTestFitness(new BranchCoverageGoal(b1, true, "FooClass", "toStringBarFooBlubb", 0));
        test1.addCoveredGoal(branchGoal1);

        TestCase test2 = new DefaultTestCase();
        test2.addCoveredGoal(methodGoal);
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        BranchCoverageTestFitness branchGoal2 = new BranchCoverageTestFitness(new BranchCoverageGoal(b1, false, "FooClass", "toString", 0));
        test2.addCoveredGoal(branchGoal2);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToString0", naming.getName(test1));
        assertEquals("testToString1", naming.getName(test2));
    }

    @Test
    public void testTwoUniqueMethods() {
        TestCase test = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "foo");
        test.addCoveredGoal(goal1);
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "bar");
        test.addCoveredGoal(goal2);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testBarAndFoo", generatedName);
    }

    @Test
    public void testTwoUniqueMethodsWithLongNames() {
        TestCase test = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "loremIpsumDolorSitAmetConsectetuerAdipiscingElit()V");
        test.addCoveredGoal(goal1);
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "sedDiamNonummNibhEuismodTinciduntUtLaoreetDoloreMagnaAliquamEratVolutpat()V");
        test.addCoveredGoal(goal2);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testLoremIpsumDolorSitAmetConsectetuerAdipiscingElit", generatedName);
    }

    @Test
    public void testMultipleMethods() throws NoSuchMethodException {
        TestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(Object.class.getConstructor(), Object.class);
        VariableReference callee = test.addStatement(new ConstructorStatement(test, gc, new ArrayList<>()));
        GenericMethod gm = new GenericMethod(Object.class.getMethod("toString"), Object.class);
        test.addStatement(new MethodStatement(test, gm, callee, new ArrayList<>()));
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString()Ljava/lang/String;");
        test.addCoveredGoal(goal1);
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "foo()Ljava/lang/String;");
        test.addCoveredGoal(goal2);
        MethodCoverageTestFitness goal3 = new MethodCoverageTestFitness("FooClass", "bar()Ljava/lang/String;");
        test.addCoveredGoal(goal3);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testToString", generatedName);
        // TODO: What should be the name now? Need some heuristic, currently sorted alphabetically
        //       Better heuristic would consider other things, like e.g. which method has more goals covered
        //       or which one is the last one called?
    }

    @Test
    public void testTwoOutputGoals() {
        TestCase test = new DefaultTestCase();
        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "toString", stringType(), STRING_EMPTY);
        OutputCoverageTestFitness goal1 = new OutputCoverageTestFitness(outputGoal1);
        OutputCoverageGoal outputGoal2 = new OutputCoverageGoal("FooClass", "bar", objectType(), REF_NONNULL);
        OutputCoverageTestFitness goal2 = new OutputCoverageTestFitness(outputGoal2);
        test.addCoveredGoal(goal1);
        test.addCoveredGoal(goal2);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testBarReturningNonNullAndToStringReturningEmptyString", generatedName);
    }

    @Test
    public void testTwoInputGoals() {
        TestCase test = new DefaultTestCase();
        InputCoverageGoal inputGoal1 = new InputCoverageGoal("FooClass", "foo", 0, objectType(), REF_NONNULL);
        InputCoverageTestFitness goal1 = new InputCoverageTestFitness(inputGoal1);
        InputCoverageGoal inputGoal2 = new InputCoverageGoal("FooClass", "foo", 1, objectType(), REF_NULL);
        InputCoverageTestFitness goal2 = new InputCoverageTestFitness(inputGoal2);
        test.addCoveredGoal(goal1);
        test.addCoveredGoal(goal2);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testFooWithNonNullAndNull", generatedName);
    }

    @Test
    public void testTwoMethodsOneWithException() {
        TestCase test = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "foo");
        test.addCoveredGoal(goal1);
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "bar");
        test.addCoveredGoal(goal2);
        ExceptionCoverageTestFitness goal3 = new ExceptionCoverageTestFitness("FooClass", "bar", RuntimeException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test.addCoveredGoal(goal3);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testBarThrowsRuntimeException", generatedName);
    }

    @Test
    public void testTwoTestsTwoMethodsOneWithException() {
        TestCase test1 = new DefaultTestCase();
        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode

        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "foo");
        test1.addCoveredGoal(goal1);
        test2.addCoveredGoal(goal1);

        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "bar()I");
        test1.addCoveredGoal(goal2);
        test2.addCoveredGoal(goal2);

        ExceptionCoverageTestFitness goal3 = new ExceptionCoverageTestFitness("FooClass", "bar()I", RuntimeException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test1.addCoveredGoal(goal3);
        MethodNoExceptionCoverageTestFitness goal4 = new MethodNoExceptionCoverageTestFitness("FooClass", "bar()I");
        test2.addCoveredGoal(goal4);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName1 = naming.getName(test1);
        String generatedName2 = naming.getName(test2);
        assertEquals("testBarThrowsRuntimeException", generatedName1);
        assertEquals("testBar", generatedName2);
    }

    @Test
    public void testTwoConstructorsDifferentClasses() {
        TestCase test = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("Foo", "<init>()");
        test.addCoveredGoal(goal1);
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("Bar", "<init>()");
        test.addCoveredGoal(goal2);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testCreatesBarAndCreatesFoo", generatedName);
    }

    @Test
    public void testTwoConstructorsSameClass() {
        TestCase test = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "<init>()");
        test.addCoveredGoal(goal1);
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("FooClass", "<init>(I)");
        test.addCoveredGoal(goal2);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testCreatesFooClassTakingNoArgumentsAndCreatesFooClassTakingInt", generatedName);
    }

    @Test
    public void testConstructorAndMethod() {
        TestCase test = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("Foo", "<init>()");
        test.addCoveredGoal(goal1);
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness("Foo", "bar()I");
        test.addCoveredGoal(goal2);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testCreatesFooAndCallsBar", generatedName);
    }


    @Test
    public void testConstructorWithFullyQualifiedClassName() {
        TestCase test = new DefaultTestCase();
        MethodCoverageTestFitness goal = new MethodCoverageTestFitness("org.package.name.FooClass", "<init>()");
        test.addCoveredGoal(goal);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testCreatesFooClass", generatedName);
    }

    @Test
    public void testConstructorExceptionWithFullyQualifiedClassName() {
        TestCase test = new DefaultTestCase();
        ExceptionCoverageTestFitness goal = new ExceptionCoverageTestFitness("org.package.name.FooClass", "<init>()", RuntimeException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test.addCoveredGoal(goal);
        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName = naming.getName(test);
        assertEquals("testFailsToCreateFooClassThrowsRuntimeException", generatedName);
    }

    @Test
    public void testOverloadedConstructor() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness(ClassWithOverloadedConstructor.class.getCanonicalName(), "<init>()");
        test1.addCoveredGoal(goal1);

        TestCase test2 = new DefaultTestCase();
        MethodCoverageTestFitness goal2 = new MethodCoverageTestFitness(ClassWithOverloadedConstructor.class.getCanonicalName(), "<init>(Ljava/lang/String;)");
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal2);

        TestCase test3 = new DefaultTestCase();
        MethodCoverageTestFitness goal3 = new MethodCoverageTestFitness(ClassWithOverloadedConstructor.class.getCanonicalName(), "<init>(II)");
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(goal3);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName1 = naming.getName(test1);
        String generatedName2 = naming.getName(test2);
        String generatedName3 = naming.getName(test3);
        assertEquals("testCreatesClassWithOverloadedConstructorTakingNoArguments", generatedName1);// TODO: I would remove the WithOverloadedConstructor
        assertEquals("testCreatesClassWithOverloadedConstructorTakingString", generatedName2);
        assertEquals("testCreatesClassWithOverloadedConstructorTaking2Arguments", generatedName3);
    }

    @Test
    public void testExceptionInOverloadedConstructor() {
        TestCase test1 = new DefaultTestCase();
        ExceptionCoverageTestFitness goal1 = new ExceptionCoverageTestFitness(ClassWithOverloadedConstructor.class.getCanonicalName(), "<init>()", NullPointerException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test1.addCoveredGoal(goal1);

        TestCase test2 = new DefaultTestCase();
        ExceptionCoverageTestFitness goal2 = new ExceptionCoverageTestFitness(ClassWithOverloadedConstructor.class.getCanonicalName(), "<init>(Ljava/lang/String;)", NullPointerException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal2);

        TestCase test3 = new DefaultTestCase();
        ExceptionCoverageTestFitness goal3 = new ExceptionCoverageTestFitness(ClassWithOverloadedConstructor.class.getCanonicalName(), "<init>(II)", NullPointerException.class, ExceptionCoverageTestFitness.ExceptionType.EXPLICIT);
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(goal3);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        String generatedName1 = naming.getName(test1);
        String generatedName2 = naming.getName(test2);
        String generatedName3 = naming.getName(test3);
        assertEquals("testFailsToCreateClassWithOverloadedConstructorTakingNoArgumentsThrowsNullPointerException", generatedName1);
        assertEquals("testFailsToCreateClassWithOverloadedConstructorTakingStringThrowsNullPointerException", generatedName2);
        assertEquals("testFailsToCreateClassWithOverloadedConstructorTaking2ArgumentsThrowsNullPointerException", generatedName3);
    }

    @Test
    public void testBooleanOutputGoal() {
        TestCase test = new DefaultTestCase();
        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "foo", Type.BOOLEAN_TYPE, BOOL_TRUE);
        OutputCoverageTestFitness goal1 = new OutputCoverageTestFitness(outputGoal1);
        test.addCoveredGoal(goal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningTrue", naming.getName(test));
    }

    @Test
    public void testNumericOutputGoal() {
        TestCase test = new DefaultTestCase();
        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "foo", Type.INT_TYPE, NUM_POSITIVE);
        OutputCoverageTestFitness goal1 = new OutputCoverageTestFitness(outputGoal1);
        test.addCoveredGoal(goal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningPositive", naming.getName(test));
    }

    @Test
    public void testCharOutputGoal() {
        TestCase test = new DefaultTestCase();
        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "foo", Type.CHAR_TYPE, CHAR_ALPHA);
        OutputCoverageTestFitness goal1 = new OutputCoverageTestFitness(outputGoal1);
        test.addCoveredGoal(goal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningAlphabeticChar", naming.getName(test));
    }

    @Test
    public void testNullOutputGoal() {
        TestCase test = new DefaultTestCase();
        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "foo", Type.getType("Ljava.lang.Object;"), REF_NULL);
        OutputCoverageTestFitness goal1 = new OutputCoverageTestFitness(outputGoal1);
        test.addCoveredGoal(goal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningNull", naming.getName(test));
    }

    @Test
    public void testEmptyArray() {
        TestCase test = new DefaultTestCase();
        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "foo", Type.getType("[Ljava.lang.Object;"), ARRAY_EMPTY);
        OutputCoverageTestFitness goal1 = new OutputCoverageTestFitness(outputGoal1);
        test.addCoveredGoal(goal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningEmptyArray", naming.getName(test));
    }

    @Test
    public void testStringOutputGoal() {
        TestCase test = new DefaultTestCase();
        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "foo", stringType(), STRING_NONEMPTY);
        OutputCoverageTestFitness goal1 = new OutputCoverageTestFitness(outputGoal1);
        test.addCoveredGoal(goal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningNonEmptyString", naming.getName(test));
    }

    @Test
    public void testInspectorOutputGoal() {
        TestCase test = new DefaultTestCase();

        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "foo", stringType(), REF_NONNULL + ":" + "Bar" + ":" + "isFoo" + ":" + NUM_NEGATIVE);
        OutputCoverageTestFitness goal1 = new OutputCoverageTestFitness(outputGoal1);
        test.addCoveredGoal(goal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningBarWhereIsFooIsNegative", naming.getName(test));
    }


    @Test
    public void testLineCoverageIsExcluded() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness methodGoal = new MethodCoverageTestFitness("FooClass", "toString()");
        test1.addCoveredGoal(methodGoal);
        LineCoverageTestFitness lineGoal1 = new LineCoverageTestFitness("FooClass", "toString()", 0);
        test1.addCoveredGoal(lineGoal1);

        TestCase test2 = new DefaultTestCase();
        test2.addCoveredGoal(methodGoal);
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        LineCoverageTestFitness lineGoal2 = new LineCoverageTestFitness("FooClass", "toString()", 10);
        test2.addCoveredGoal(lineGoal2);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToString0", naming.getName(test1));
        assertEquals("testToString1", naming.getName(test2));
    }

    @Test
    public void testResolveBasicConflict() {
        // T1: A, B -> B
        // T2: A, C -> C
        // T3: A, C, B, D -> D
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness methodGoal = new MethodCoverageTestFitness("FooClass", "foo(I)I");
        test1.addCoveredGoal(methodGoal);
        OutputCoverageGoal outputGoalHelper = new OutputCoverageGoal("FooClass", "foo(I)I", Type.INT_TYPE, NUM_ZERO);
        OutputCoverageTestFitness outputGoal1 = new OutputCoverageTestFitness(outputGoalHelper);
        test1.addCoveredGoal(outputGoal1);


        TestCase test2 = new DefaultTestCase();
        test2.addCoveredGoal(methodGoal);
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        InputCoverageGoal inputGoalHelper = new InputCoverageGoal("FooClass", "foo(I)I", 0, Type.INT_TYPE, NUM_POSITIVE);
        InputCoverageTestFitness inputGoal1 = new InputCoverageTestFitness(inputGoalHelper);
        test2.addCoveredGoal(inputGoal1);

        TestCase test3 = new DefaultTestCase();
        MethodCoverageTestFitness methodGoal2 = new MethodCoverageTestFitness("FooClass", "toString()L/java/lang/String;");
        test3.addCoveredGoal(methodGoal2);

        test3.addCoveredGoal(methodGoal);
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(outputGoal1);
        test3.addCoveredGoal(inputGoal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningZero", naming.getName(test1));
        assertEquals("testFooWithPositive", naming.getName(test2));
        assertEquals("testToString", naming.getName(test3));
    }

    @Test
    public void testResolveConflict2() {
        // T1: A, B -> B
        // T2: A, C -> C
        // T3: A, B, C -> B & C

        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness methodGoal = new MethodCoverageTestFitness("FooClass", "foo(I)I");
        test1.addCoveredGoal(methodGoal);
        OutputCoverageGoal outputGoalHelper = new OutputCoverageGoal("FooClass", "foo(I)I", Type.INT_TYPE, NUM_ZERO);
        OutputCoverageTestFitness outputGoal1 = new OutputCoverageTestFitness(outputGoalHelper);
        test1.addCoveredGoal(outputGoal1);


        TestCase test2 = new DefaultTestCase();
        test2.addCoveredGoal(methodGoal);
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        InputCoverageGoal inputGoalHelper = new InputCoverageGoal("FooClass", "foo(I)I", 0, Type.INT_TYPE, NUM_POSITIVE);
        InputCoverageTestFitness inputGoal1 = new InputCoverageTestFitness(inputGoalHelper);
        test2.addCoveredGoal(inputGoal1);

        TestCase test3 = new DefaultTestCase();
        test3.addCoveredGoal(methodGoal);
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(outputGoal1);
        test3.addCoveredGoal(inputGoal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningZero", naming.getName(test1));
        assertEquals("testFooWithPositive", naming.getName(test2));
        assertEquals("testFooReturningZeroAndFooWithPositive", naming.getName(test3));
    }

    @Test
    public void testResolveConflict3() {
        // T1: A, B
        // T2: A, C
        // T3: B, C

        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness methodGoal = new MethodCoverageTestFitness("FooClass", "foo(I)I");
        test1.addCoveredGoal(methodGoal);
        OutputCoverageGoal outputGoalHelper = new OutputCoverageGoal("FooClass", "foo(I)I", Type.INT_TYPE, NUM_ZERO);
        OutputCoverageTestFitness outputGoal1 = new OutputCoverageTestFitness(outputGoalHelper);
        test1.addCoveredGoal(outputGoal1);


        TestCase test2 = new DefaultTestCase();
        test2.addCoveredGoal(methodGoal);
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        InputCoverageGoal inputGoalHelper = new InputCoverageGoal("FooClass", "foo(I)I", 0, Type.INT_TYPE, NUM_POSITIVE);
        InputCoverageTestFitness inputGoal1 = new InputCoverageTestFitness(inputGoalHelper);
        test2.addCoveredGoal(inputGoal1);

        TestCase test3 = new DefaultTestCase();
        // test3.addCoveredGoal(methodGoal);
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(outputGoal1);
        test3.addCoveredGoal(inputGoal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooAndFooReturningZero", naming.getName(test1));
        assertEquals("testFooAndFooWithPositive", naming.getName(test2));
        assertEquals("testFooReturningZero", naming.getName(test3)); // TODO: Is this acceptable? This happens because test 3 has no method goals
    }

    @Test
    public void testResolveConflict3WithMethodGoals() {
        // T1: A, B
        // T2: A, C
        // T3: B, C

        MethodCoverageTestFitness methodGoal1 = new MethodCoverageTestFitness("FooClass", "foo(I)I");
        MethodCoverageTestFitness methodGoal2 = new MethodCoverageTestFitness("FooClass", "bar(I)I");
        MethodCoverageTestFitness methodGoal3 = new MethodCoverageTestFitness("FooClass", "zoo(I)I");

        TestCase test1 = new DefaultTestCase();
        test1.addCoveredGoal(methodGoal1);
        test1.addCoveredGoal(methodGoal2);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(methodGoal1);
        test2.addCoveredGoal(methodGoal3);

        TestCase test3 = new DefaultTestCase();
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(methodGoal2);
        test3.addCoveredGoal(methodGoal3);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testBarAndFoo", naming.getName(test1));
        assertEquals("testFooAndZoo", naming.getName(test2));
        assertEquals("testBarAndZoo", naming.getName(test3));
    }

    @Test
    public void testResolveConflictWithManyTests() {
        // T1: A, B
        // T2: A, C
        // T3: A, D
        // T4: B, C
        // T5: B, D
        // T6: C, D

        Randomness.setSeed(0);
        MethodCoverageTestFitness methodGoal1 = new MethodCoverageTestFitness("FooClass", "foo(I)I");
        MethodCoverageTestFitness methodGoal2 = new MethodCoverageTestFitness("FooClass", "bar(I)I");
        MethodCoverageTestFitness methodGoal3 = new MethodCoverageTestFitness("FooClass", "zoo(I)I");
        MethodCoverageTestFitness methodGoal4 = new MethodCoverageTestFitness("FooClass", "gnu(I)I");

        TestCase test1 = new DefaultTestCase();
        test1.addCoveredGoal(methodGoal1);
        test1.addCoveredGoal(methodGoal2);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(methodGoal1);
        test2.addCoveredGoal(methodGoal3);

        TestCase test3 = new DefaultTestCase();
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(methodGoal1);
        test3.addCoveredGoal(methodGoal4);

        TestCase test4 = new DefaultTestCase();
        test4.addStatement(new IntPrimitiveStatement(test4, 2)); // Need to add statements to change hashCode
        test4.addCoveredGoal(methodGoal2);
        test4.addCoveredGoal(methodGoal3);

        TestCase test5 = new DefaultTestCase();
        test5.addStatement(new IntPrimitiveStatement(test5, 3)); // Need to add statements to change hashCode
        test5.addCoveredGoal(methodGoal2);
        test5.addCoveredGoal(methodGoal4);

        TestCase test6 = new DefaultTestCase();
        test6.addStatement(new IntPrimitiveStatement(test6, 4)); // Need to add statements to change hashCode
        test6.addCoveredGoal(methodGoal3);
        test6.addCoveredGoal(methodGoal4);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        tests.add(test4);
        tests.add(test5);
        tests.add(test6);

        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testBarAndFoo", naming.getName(test1));
        assertEquals("testFooAndZoo", naming.getName(test2));
        assertEquals("testFooAndGnu", naming.getName(test3));
        assertEquals("testBarAndZoo", naming.getName(test4));
        assertEquals("testBarAndGnu", naming.getName(test5));
        assertEquals("testGnuAndZoo", naming.getName(test6));
    }

    @Test
    public void testResolveConflictWithTwoMethods() {
        // T1: A, B
        // T2: A, C
        // T3: A, B, C
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness methodGoal = new MethodCoverageTestFitness("FooClass", "foo(I)I");
        test1.addCoveredGoal(methodGoal);
        OutputCoverageGoal outputGoalHelper = new OutputCoverageGoal("FooClass", "foo(I)I", Type.INT_TYPE, NUM_ZERO);
        OutputCoverageTestFitness outputGoal1 = new OutputCoverageTestFitness(outputGoalHelper);
        test1.addCoveredGoal(outputGoal1);


        TestCase test2 = new DefaultTestCase();
        test2.addCoveredGoal(methodGoal);
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        InputCoverageGoal inputGoalHelper = new InputCoverageGoal("FooClass", "foo(I)I", 0, Type.INT_TYPE, NUM_POSITIVE);
        InputCoverageTestFitness inputGoal1 = new InputCoverageTestFitness(inputGoalHelper);
        test2.addCoveredGoal(inputGoal1);

        TestCase test3 = new DefaultTestCase();
        test3.addCoveredGoal(methodGoal);
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(outputGoal1);
        test3.addCoveredGoal(inputGoal1);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningZero", naming.getName(test1));
        assertEquals("testFooWithPositive", naming.getName(test2));
        assertEquals("testFooReturningZeroAndFooWithPositive", naming.getName(test3));
    }

    @Test
    public void testResolveConflictInputsOutputs() {
        // T1: A, I1, O1
        // T2: A, I2, O2
        // T3: A, I1, O2

        MethodCoverageTestFitness methodGoal = new MethodCoverageTestFitness("FooClass", "foo(I)I");
        OutputCoverageGoal outputGoalHelper1 = new OutputCoverageGoal("FooClass", "foo(I)I", Type.INT_TYPE, NUM_POSITIVE);
        OutputCoverageTestFitness outputGoal1 = new OutputCoverageTestFitness(outputGoalHelper1);
        OutputCoverageGoal outputGoalHelper2 = new OutputCoverageGoal("FooClass", "foo(I)I", Type.INT_TYPE, NUM_NEGATIVE);
        OutputCoverageTestFitness outputGoal2 = new OutputCoverageTestFitness(outputGoalHelper2);
        InputCoverageGoal inputGoalHelper1 = new InputCoverageGoal("FooClass", "foo(I)I", 0, Type.INT_TYPE, NUM_POSITIVE);
        InputCoverageTestFitness inputGoal1 = new InputCoverageTestFitness(inputGoalHelper1);
        InputCoverageGoal inputGoalHelper2 = new InputCoverageGoal("FooClass", "foo(I)I", 0, Type.INT_TYPE, NUM_NEGATIVE);
        InputCoverageTestFitness inputGoal2 = new InputCoverageTestFitness(inputGoalHelper2);

        TestCase test1 = new DefaultTestCase();
        test1.addCoveredGoal(methodGoal);
        test1.addCoveredGoal(inputGoal1);
        test1.addCoveredGoal(outputGoal1);


        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(methodGoal);
        test2.addCoveredGoal(inputGoal2);
        test2.addCoveredGoal(outputGoal2);

        TestCase test3 = new DefaultTestCase();
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(methodGoal);
        test3.addCoveredGoal(inputGoal2);
        test3.addCoveredGoal(outputGoal1);

        TestCase test4 = new DefaultTestCase();
        test4.addStatement(new IntPrimitiveStatement(test4, 2)); // Need to add statements to change hashCode
        test4.addCoveredGoal(methodGoal);
        test4.addCoveredGoal(inputGoal1);
        test4.addCoveredGoal(outputGoal2);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        tests.add(test4);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testFooReturningPositiveAndFooWithPositive", naming.getName(test1));
        assertEquals("testFooReturningNegativeAndFooWithNegative", naming.getName(test2));
        assertEquals("testFooReturningPositiveAndFooWithNegative", naming.getName(test3));
        assertEquals("testFooReturningNegativeAndFooWithPositive", naming.getName(test4));
    }

    @Test
    public void testResolveMethodNames() {

        MethodCoverageTestFitness methodGoal = new MethodCoverageTestFitness("org.apache.commons.scxml.Builtin", "isMember(Ljava/util/Set;Ljava/lang/String;)Z");
        MethodNoExceptionCoverageTestFitness methodNoExGoal = new MethodNoExceptionCoverageTestFitness("org.apache.commons.scxml.Builtin", "isMember(Ljava/util/Set;Ljava/lang/String;)Z");

        OutputCoverageGoal outputGoalHelper1 = new OutputCoverageGoal("org.apache.commons.scxml.Builtin", "isMember(Ljava/util/Set;Ljava/lang/String;)Z", Type.BOOLEAN_TYPE, BOOL_FALSE);
        OutputCoverageTestFitness outputGoalFalse = new OutputCoverageTestFitness(outputGoalHelper1);
        OutputCoverageGoal outputGoalHelper2 = new OutputCoverageGoal("org.apache.commons.scxml.Builtin", "isMember(Ljava/util/Set;Ljava/lang/String;)Z", Type.BOOLEAN_TYPE, BOOL_TRUE);
        OutputCoverageTestFitness outputGoalTrue = new OutputCoverageTestFitness(outputGoalHelper2);

        InputCoverageGoal inputGoalHelper1 = new InputCoverageGoal("org.apache.commons.scxml.Builtin", "isMember(Ljava/util/Set;Ljava/lang/String;)Z", 0, Type.getType(Set.class), SET_EMPTY);
        InputCoverageTestFitness inputGoalEmptySet = new InputCoverageTestFitness(inputGoalHelper1);

        InputCoverageGoal inputGoalHelper2 = new InputCoverageGoal("org.apache.commons.scxml.Builtin", "isMember(Ljava/util/Set;Ljava/lang/String;)Z", 0, Type.getType(Set.class), SET_NONEMPTY);
        InputCoverageTestFitness inputGoalNonEmptySet = new InputCoverageTestFitness(inputGoalHelper2);

        InputCoverageGoal inputGoalHelper3 = new InputCoverageGoal("org.apache.commons.scxml.Builtin", "isMember(Ljava/util/Set;Ljava/lang/String;)Z", 1, Type.getType(String.class), STRING_NONEMPTY);
        InputCoverageTestFitness inputGoalStringNonEmpty = new InputCoverageTestFitness(inputGoalHelper3);


        TestCase test1 = new DefaultTestCase();
        test1.addCoveredGoal(methodGoal);
        test1.addCoveredGoal(methodNoExGoal);
        test1.addCoveredGoal(inputGoalEmptySet);
        test1.addCoveredGoal(inputGoalStringNonEmpty);
        test1.addCoveredGoal(outputGoalFalse);


        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(methodGoal);
        test2.addCoveredGoal(methodNoExGoal);
        test2.addCoveredGoal(inputGoalNonEmptySet);
        test2.addCoveredGoal(inputGoalStringNonEmpty);
        test2.addCoveredGoal(outputGoalFalse);

        TestCase test3 = new DefaultTestCase();
        test3.addStatement(new IntPrimitiveStatement(test3, 1)); // Need to add statements to change hashCode
        test3.addCoveredGoal(methodGoal);
        test3.addCoveredGoal(methodNoExGoal);
        test3.addCoveredGoal(inputGoalNonEmptySet);
        test3.addCoveredGoal(inputGoalStringNonEmpty);
        test3.addCoveredGoal(outputGoalTrue);

        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);

        assertEquals("testIsMemberWithEmptySet", naming.getName(test1));
        assertEquals("testIsMemberReturningFalse", naming.getName(test2));
        assertEquals("testIsMemberReturningTrue", naming.getName(test3));
    }


    private Type stringType() {
        return Type.getType("Ljava.lang.String;");
    }

    private Type objectType() {
        return Type.getType("Ljava.lang.Object;");
    }
}
