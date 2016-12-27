package org.evosuite.testcase;

import com.examples.with.different.packagename.test.DowncastExample;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by gordon on 27/12/2016.
 */
public class DowncastTest  {

    @Test
    public void testUnnecessaryDownCast() throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var = builder.appendConstructor(DowncastExample.class.getConstructor());
        VariableReference int0 = builder.appendIntPrimitive(42);
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getANumber", new Class<?>[] {int.class}), int0);
        num0.setType(Integer.class); // This would be set during execution
        VariableReference boolean0 = builder.appendMethod(var, DowncastExample.class.getMethod("testMe", new Class<?>[] {Number.class}), num0);
        PrimitiveAssertion assertion = new PrimitiveAssertion();
        assertion.setSource(boolean0);
        assertion.setValue(false);
        DefaultTestCase test = builder.getDefaultTestCase();
        test.getStatement(boolean0.getStPosition()).addAssertion(assertion);
        test.removeDownCasts();
        assertEquals(Number.class, test.getStatement(2).getReturnClass());
    }

    @Test
    public void testNecessaryDownCast() throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var = builder.appendConstructor(DowncastExample.class.getConstructor());
        VariableReference int0 = builder.appendIntPrimitive(42);
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getANumber", new Class<?>[] {int.class}), int0);
        num0.setType(Integer.class); // This would be set during execution
        VariableReference boolean0 = builder.appendMethod(var, DowncastExample.class.getMethod("testWithInteger", new Class<?>[] {Integer.class}), num0);
        PrimitiveAssertion assertion = new PrimitiveAssertion();
        assertion.setSource(boolean0);
        assertion.setValue(false);
        DefaultTestCase test = builder.getDefaultTestCase();
        test.getStatement(boolean0.getStPosition()).addAssertion(assertion);
        test.removeDownCasts();
        assertEquals(Integer.class, test.getStatement(2).getReturnClass());
    }

}
