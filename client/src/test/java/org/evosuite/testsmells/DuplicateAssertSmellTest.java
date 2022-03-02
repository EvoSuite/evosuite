package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.DuplicateAssert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class DuplicateAssertSmellTest {

    AbstractTestCaseSmell duplicateAssert;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.duplicateAssert = new DuplicateAssert();
    }

    @Test
    public void redundantTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        int smellCount = this.duplicateAssert.computeNumberOfSmells(testCase);
        int expected = 2;
        assertEquals(expected, smellCount);
    }

    private DefaultTestCase createTestCase0 () throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0, intStatement0);

        Method getNameMethod1 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, getNameMethod1, intStatement0);

        Method getNameMethod2 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        VariableReference methodStatement2 = builder.appendMethod(constructorStatement0, getNameMethod2, intStatement0);

        Method getNameMethod3 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement3 = builder.appendMethod(constructorStatement0, getNameMethod3);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(true);
        currentStatement = testCase.getStatement(3);
        currentStatement.addAssertion(primitiveAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(true);
        currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion1);

        PrimitiveAssertion primitiveAssertion2 = new PrimitiveAssertion();
        primitiveAssertion2.setSource(methodStatement2);
        primitiveAssertion2.setValue(true);
        currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(primitiveAssertion2);

        PrimitiveAssertion primitiveAssertion3 = new PrimitiveAssertion();
        primitiveAssertion3.setSource(methodStatement3);
        primitiveAssertion3.setValue("Bob");
        currentStatement = testCase.getStatement(6);
        currentStatement.addAssertion(primitiveAssertion3);

        return testCase;
    }
}
