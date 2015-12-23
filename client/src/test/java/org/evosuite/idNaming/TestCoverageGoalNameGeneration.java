package org.evosuite.idNaming;

import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageGoal;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageGoal;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    public void testConstructorWithAndWithoutException() {
        TestCase test1 = new DefaultTestCase();
        MethodCoverageTestFitness goal1 = new MethodCoverageTestFitness("FooClass", "<init>");
        test1.addCoveredGoal(goal1);
        MethodNoExceptionCoverageTestFitness goal1a = new MethodNoExceptionCoverageTestFitness("FooClass", "<init>");
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
        assertEquals("testGeneratesFooClass", naming.getName(test1));
        assertEquals("testFailsToGenerateFooClassThrowsRuntimeException", naming.getName(test2));
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
        assertEquals("testFooWithoutArguments",     naming.getName(test1));
        assertEquals("testFooWithList", naming.getName(test2));
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
    public void testMultipleMethods() throws NoSuchMethodException {
        TestCase test = new DefaultTestCase();
        GenericConstructor gc = new GenericConstructor(Object.class.getConstructor(), Object.class);
        VariableReference callee = test.addStatement(new ConstructorStatement(test, gc, new ArrayList<VariableReference>()));
        GenericMethod gm = new GenericMethod(Object.class.getMethod("toString"), Object.class);
        test.addStatement(new MethodStatement(test, gm, callee, new ArrayList<VariableReference>()));
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


}
