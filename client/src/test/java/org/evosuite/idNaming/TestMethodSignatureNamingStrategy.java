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
        assertEquals("x", tcv.getVariableName(var1));
        assertEquals("y", tcv.getVariableName(var2));
        System.out.println("Code: "+tcv.getCode());
    }
}
