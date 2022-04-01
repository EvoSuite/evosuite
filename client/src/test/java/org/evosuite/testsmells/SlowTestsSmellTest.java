package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.SlowTests;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SlowTestsSmellTest {

    AbstractNormalizedTestCaseSmell slowTests;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.slowTests = new SlowTests();
    }

    @Test
    public void testLastExecutionResultNull() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        long smellCount = this.slowTests.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.slowTests.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testLastExecutionResultNotNull() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        ExecutionResult result = TestCaseExecutor.runTest(testCase.getTestCase());
        testCase.setLastExecutionResult(result);

        long smellCount = this.slowTests.computeNumberOfTestSmells(testCase);
        assertTrue(smellCount > 0);

        double computedMetric = this.slowTests.computeTestSmellMetric(testCase);
        assertTrue(computedMetric > 0);
    }

    private DefaultTestCase createTestCase0() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        Method getNumberMethod0 = TestSmellsTestingClass1.class.getMethod("getNumber");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, getNumberMethod0);

        Method wasteTimeMethod0 = TestSmellsTestingClass1.class.getMethod("wasteTime");
        builder.appendMethod(constructorStatement0, wasteTimeMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        Statement currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(5);
        currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(primitiveAssertion1);

        return testCase;
    }
}
