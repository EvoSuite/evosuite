package org.evosuite.idNaming;

import com.examples.with.different.packagename.SimpleInteger;
import org.evosuite.Properties;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;


/**
 * Created by gordon on 29/12/2015.
 */
public class TestMethodSignatureNamingStrategy {

    @Test
    public void testSimple() throws NoSuchMethodException {
        Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DECLARATIONS;
        VariableNameCollector.getInstance().addParameterName(SimpleInteger.class.getCanonicalName(), "testInt(II)I", 0, "x");
        VariableNameCollector.getInstance().addParameterName(SimpleInteger.class.getCanonicalName(), "testInt(II)I", 1, "y");

        TestCase test = new DefaultTestCase();
        VariableReference var1 = test.addStatement(new IntPrimitiveStatement(test, 0));
        VariableReference var2 = test.addStatement(new IntPrimitiveStatement(test, 0));
        GenericConstructor gc = new GenericConstructor(SimpleInteger.class.getConstructor(), SimpleInteger.class);
        VariableReference callee = test.addStatement(new ConstructorStatement(test, gc, new ArrayList<>()));
        GenericMethod gm = new GenericMethod(SimpleInteger.class.getMethod("testInt", new Class<?>[] {int.class, int.class}), SimpleInteger.class);
        test.addStatement(new MethodStatement(test, gm, callee, Arrays.asList(var1, var2)));

        TestCodeVisitor tcv = new TestCodeVisitor();
        tcv.visitTestCase(test);
        test.accept(tcv);
        assertEquals("x", tcv.getVariableName(var1));
        assertEquals("y", tcv.getVariableName(var2));
    }


    @Test
    public void testTwoCallsToSameMethodWithSameVariables() throws NoSuchMethodException {
        Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DECLARATIONS;
        VariableNameCollector.getInstance().addParameterName(SimpleInteger.class.getCanonicalName(), "testInt(II)I", 0, "x");
        VariableNameCollector.getInstance().addParameterName(SimpleInteger.class.getCanonicalName(), "testInt(II)I", 1, "y");

        TestCase test = new DefaultTestCase();
        VariableReference var1 = test.addStatement(new IntPrimitiveStatement(test, 0));
        VariableReference var2 = test.addStatement(new IntPrimitiveStatement(test, 0));
        GenericConstructor gc = new GenericConstructor(SimpleInteger.class.getConstructor(), SimpleInteger.class);
        VariableReference callee = test.addStatement(new ConstructorStatement(test, gc, new ArrayList<>()));
        GenericMethod gm = new GenericMethod(SimpleInteger.class.getMethod("testInt", new Class<?>[] {int.class, int.class}), SimpleInteger.class);
        test.addStatement(new MethodStatement(test, gm, callee, Arrays.asList(var1, var2)));
        test.addStatement(new MethodStatement(test, gm, callee, Arrays.asList(var1, var2)));

        TestCodeVisitor tcv = new TestCodeVisitor();
        tcv.visitTestCase(test);
        assertEquals("x", tcv.getVariableName(var1));
        assertEquals("y", tcv.getVariableName(var2));
    }

    @Test
    public void testTwoCallsToSameMethodWithDifferentVariables() throws NoSuchMethodException {
        Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DECLARATIONS;
        VariableNameCollector.getInstance().addParameterName(SimpleInteger.class.getCanonicalName(), "testInt(II)I", 0, "x");
        VariableNameCollector.getInstance().addParameterName(SimpleInteger.class.getCanonicalName(), "testInt(II)I", 1, "y");

        TestCase test = new DefaultTestCase();
        VariableReference var1 = test.addStatement(new IntPrimitiveStatement(test, 0));
        VariableReference var2 = test.addStatement(new IntPrimitiveStatement(test, 0));
        VariableReference var3 = test.addStatement(new IntPrimitiveStatement(test, 0));
        VariableReference var4 = test.addStatement(new IntPrimitiveStatement(test, 0));
        GenericConstructor gc = new GenericConstructor(SimpleInteger.class.getConstructor(), SimpleInteger.class);
        VariableReference callee = test.addStatement(new ConstructorStatement(test, gc, new ArrayList<>()));
        GenericMethod gm = new GenericMethod(SimpleInteger.class.getMethod("testInt", new Class<?>[] {int.class, int.class}), SimpleInteger.class);
        test.addStatement(new MethodStatement(test, gm, callee, Arrays.asList(var1, var2)));
        test.addStatement(new MethodStatement(test, gm, callee, Arrays.asList(var3, var4)));

        TestCodeVisitor tcv = new TestCodeVisitor();
        tcv.visitTestCase(test);
        assertEquals("x0", tcv.getVariableName(var1));
        assertEquals("y0", tcv.getVariableName(var2));
        assertEquals("x1", tcv.getVariableName(var3));
        assertEquals("y1", tcv.getVariableName(var4));
    }

    @Test
    public void testTwoDifferentCallsWithSameVariables() throws NoSuchMethodException {
        Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DECLARATIONS;
        VariableNameCollector.getInstance().addParameterName(SimpleInteger.class.getCanonicalName(), "testInt(II)I", 0, "x");
        VariableNameCollector.getInstance().addParameterName(SimpleInteger.class.getCanonicalName(), "testInt(II)I", 1, "y");

        TestCase test = new DefaultTestCase();
        VariableReference var1 = test.addStatement(new IntPrimitiveStatement(test, 0));
        VariableReference var2 = test.addStatement(new IntPrimitiveStatement(test, 0));
        GenericConstructor gc = new GenericConstructor(SimpleInteger.class.getConstructor(), SimpleInteger.class);
        VariableReference callee = test.addStatement(new ConstructorStatement(test, gc, new ArrayList<>()));
        GenericMethod gm = new GenericMethod(SimpleInteger.class.getMethod("testInt", new Class<?>[] {int.class, int.class}), SimpleInteger.class);
        test.addStatement(new MethodStatement(test, gm, callee, Arrays.asList(var1, var2)));
        test.addStatement(new MethodStatement(test, gm, callee, Arrays.asList(var2, var1)));

        TestCodeVisitor tcv = new TestCodeVisitor();
        tcv.visitTestCase(test);
        assertEquals("x", tcv.getVariableName(var1));
        assertEquals("y", tcv.getVariableName(var2));
    }

}
