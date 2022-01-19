package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTemporaryClass;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.EagerTest;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class EagerTestSmellTest {

    AbstractTestSmell eagerTest;

    @Before
    public void setUp() {
        this.eagerTest = new EagerTest();
    }

    @Test
    public void testATestCase() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        int smellCount = this.eagerTest.obtainSmellCount(testCase);

        //This value should be 2, but it is necessary to define the target class in Properties
        int expected = 0;

        assertEquals(expected, smellCount);
    }

    @Test
    public void testATestSuite() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase1();
        suite.addTest(test0);
        suite.addTest(test1);

        int smellCount = this.eagerTest.obtainSmellCount(suite);

        //This value should be 3, but it is necessary to define the target class in Properties
        int expected = 0;

        assertEquals(expected, smellCount);
    }

    private DefaultTestCase createTestCase0 () throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("MySever1");
        VariableReference stringStatement1 = builder.appendStringPrimitive("Bob");
        VariableReference stringStatement2 = builder.appendStringPrimitive("qwerty123");

        Constructor<TestSmellsTemporaryClass> const0 = TestSmellsTemporaryClass.class.getConstructor(String.class, String.class, String.class);

        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0, stringStatement1, stringStatement2);

        VariableReference stringStatement3 = builder.appendStringPrimitive("Alice");
        VariableReference stringStatement4 = builder.appendStringPrimitive("12345");

        Method addUserMethod0 = TestSmellsTemporaryClass.class.getMethod("addUser", String.class, String.class, String.class, String.class);

        builder.appendMethod(constructorStatement0, addUserMethod0, stringStatement1, stringStatement2, stringStatement3, stringStatement4);

        Method addUserMethod1 = TestSmellsTemporaryClass.class.getMethod("getServerID");

        builder.appendMethod(constructorStatement0, addUserMethod1);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase1 () throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("MySever2");
        VariableReference stringStatement1 = builder.appendStringPrimitive("Jon");
        VariableReference stringStatement2 = builder.appendStringPrimitive("asdf6789");

        Constructor<TestSmellsTemporaryClass> const0 = TestSmellsTemporaryClass.class.getConstructor(String.class, String.class, String.class);

        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0, stringStatement1, stringStatement2);

        VariableReference stringStatement3 = builder.appendStringPrimitive("George");
        VariableReference stringStatement4 = builder.appendStringPrimitive("12345");

        Method addUserMethod0 = TestSmellsTemporaryClass.class.getMethod("addUser", String.class, String.class, String.class, String.class);

        builder.appendMethod(constructorStatement0, addUserMethod0, stringStatement1, stringStatement2, stringStatement1, stringStatement2);

        builder.appendMethod(constructorStatement0, addUserMethod0, stringStatement1, stringStatement2, stringStatement3, stringStatement4);

        return builder.getDefaultTestCase();
    }
}
