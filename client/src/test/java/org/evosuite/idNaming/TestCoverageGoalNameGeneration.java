package org.evosuite.idNaming;

import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageGoal;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageGoal;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        assertEquals("testGeneratesFooClass", generatedName);
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
        assertEquals("testToString",     naming.getName(test1));
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
        assertEquals("testToString",     naming.getName(test1));
        assertEquals("testGetSomeStuff", naming.getName(test2));
    }

    @Test
    public void testMethodWithAndWithoutException() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString");
        test1.addCoveredGoal(goal1);
        MethodNoExceptionCoverageTestFitness goal1a = new MethodNoExceptionCoverageTestFitness("FooClass", "toString");
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
    public void testMethodWithOutputGoals() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString");
        OutputCoverageGoal outputGoal1 = new OutputCoverageGoal("FooClass", "toString", "String", "Null");
        OutputCoverageTestFitness goal2 = new OutputCoverageTestFitness(outputGoal1);
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal1);
        OutputCoverageGoal outputGoal2 = new OutputCoverageGoal("FooClass", "toString", "String", "NonNull");
        OutputCoverageTestFitness goal3 = new OutputCoverageTestFitness(outputGoal2);
        test2.addCoveredGoal(goal3);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToStringReturningNull",    naming.getName(test1));
        assertEquals("testToStringReturningNonNull", naming.getName(test2));
    }

    @Test
    public void testMethodWithInputGoals() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString");
        InputCoverageGoal inputGoal1 = new InputCoverageGoal("FooClass", "toString", 0, "String", "Null");
        InputCoverageTestFitness goal2 = new InputCoverageTestFitness(inputGoal1);
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal1);
        InputCoverageGoal inputGoal2 = new InputCoverageGoal("FooClass", "toString", 0, "String", "NonNull");
        InputCoverageTestFitness goal3 = new InputCoverageTestFitness(inputGoal2);
        test2.addCoveredGoal(goal3);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToStringWithNull",    naming.getName(test1));
        assertEquals("testToStringWithNonNull", naming.getName(test2));
    }

    @Test
    public void testMethodWithInputOutputGoals() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "toString");
        InputCoverageGoal inputGoal1 = new InputCoverageGoal("FooClass", "toString", 0, "String", "Null");
        InputCoverageTestFitness goal2 = new InputCoverageTestFitness(inputGoal1);
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);

        TestCase test2 = new DefaultTestCase();
        test2.addStatement(new IntPrimitiveStatement(test2, 0)); // Need to add statements to change hashCode
        test2.addCoveredGoal(goal1);
        OutputCoverageGoal outputGoal2 = new OutputCoverageGoal("FooClass", "toString", "String", "NonNull");
        OutputCoverageTestFitness goal3 = new OutputCoverageTestFitness(outputGoal2);
        test2.addCoveredGoal(goal3);


        List<TestCase> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
        assertEquals("testToStringWithNull",    naming.getName(test1));
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
        assertEquals("testFooWithoutArguments",     naming.getName(test1));
        assertEquals("testFooWithInt", naming.getName(test2));
        assertEquals("testFooWith2Arguments", naming.getName(test3));
    }
}
