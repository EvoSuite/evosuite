/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import com.examples.with.different.packagename.NullString;
import com.examples.with.different.packagename.junit.Foo;
import org.evosuite.Properties;
import org.evosuite.assertion.SimpleMutationAssertionGenerator;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.testcase.statements.numeric.BooleanPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jose Rojas
 *
 */
public class VariableNamesTestVisitorTest {

    @Test
    public void testVariableNamesMethodCallDummyStrategy() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

        Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DUMMY;

        // test
        DefaultTestCase tc = createTestCaseWithPrimitiveStatements();
        System.out.println(tc);

        // check variable names
        VariableReference var0 = tc.getStatement(0).getReturnValue();
        VariableReference var1 = tc.getStatement(1).getReturnValue();
        VariableReference var2 = tc.getStatement(2).getReturnValue();
        VariableReference var3 = tc.getStatement(3).getReturnValue();

	    TestCodeVisitor tcv = new TestCodeVisitor();
	    tcv.visitTestCase(tc);
        Assert.assertEquals("Unexpected variable name", "var0", tcv.getVariableName(var0));
        Assert.assertEquals("Unexpected variable name", "var1", tcv.getVariableName(var1));
        Assert.assertEquals("Unexpected variable name", "var2", tcv.getVariableName(var2));
        Assert.assertEquals("Unexpected variable name", "var3", tcv.getVariableName(var3));
    }

    @Test
    public void testVariableNamesMethodCallExplanatoryStrategy() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

        Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.EXPLANATORY;

        // test
        DefaultTestCase tc = createTestCaseWithPrimitiveStatements();

        // generate variable names
        Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.EXPLANATORY;

        // check variable names
        VariableReference var0 = tc.getStatement(0).getReturnValue();
        VariableReference var1 = tc.getStatement(1).getReturnValue();
        VariableReference var2 = tc.getStatement(2).getReturnValue();
        VariableReference var3 = tc.getStatement(3).getReturnValue();

        TestCodeVisitor tcv = new TestCodeVisitor();
        tcv.visitTestCase(tc);
        System.out.println(tc.toCode());
        Assert.assertEquals("Unexpected variable name", "const_5", tcv.getVariableName(var0));
        Assert.assertEquals("Unexpected variable name", "const_3", tcv.getVariableName(var1));
        Assert.assertEquals("Unexpected variable name", "foo0", tcv.getVariableName(var2));
        Assert.assertEquals("Unexpected variable name", "resultFromAdd", tcv.getVariableName(var3));
    }

    private DefaultTestCase createTestCaseWithPrimitiveStatements() throws NoSuchMethodException {
        Class<?> sut = Foo.class;
        DefaultTestCase tc0 = new DefaultTestCase();

        // int int0 = 5;
        VariableReference int0 = tc0.addStatement(new IntPrimitiveStatement(tc0, 5));
        // int int1 = 3;
        VariableReference int1 = tc0.addStatement(new IntPrimitiveStatement(tc0, 3));

        // Foo foo0 = new Foo();
        GenericConstructor fooConstructor = new GenericConstructor(sut.getConstructors()[0], sut);
        ConstructorStatement fooConstructorStatement = new ConstructorStatement(tc0, fooConstructor, Arrays.asList(new VariableReference[] {}));
        VariableReference foo0 = tc0.addStatement(fooConstructorStatement);

        // int int2 = foo0.add(int0,int1);
        Method fooIncMethod = sut.getMethod("add", new Class<?>[] { Integer.TYPE, Integer.TYPE});
        GenericMethod incMethod = new GenericMethod(fooIncMethod, sut);
        VariableReference int2 = tc0.addStatement(new MethodStatement(tc0, incMethod, foo0, Arrays.asList(new VariableReference[] {int0, int1})));

        // foo0.add(int0,int2);
        tc0.addStatement(new MethodStatement(tc0, incMethod, foo0, Arrays.asList(new VariableReference[] {int0, int2})));
        return tc0;
    }

    @Test
    public void testVariableNamesArray() throws NoSuchMethodException {
        DefaultTestCase test = new DefaultTestCase();

        ArrayStatement as = new ArrayStatement(test, String[].class, 2);
        test.addStatement(as);

        ArrayReference arrayVar = as.getArrayReference();

        ArrayIndex ai0 = new ArrayIndex(test, arrayVar, 0);
        ArrayIndex ai1 = new ArrayIndex(test, arrayVar, 1);

        ConstructorStatement cs = new ConstructorStatement(test, new GenericConstructor(String.class.getConstructor(), String.class), new ArrayList<>());
        VariableReference objectVar = test.addStatement(cs);
        test.addStatement(new AssignmentStatement(test, ai0, objectVar));

        NullStatement nullStmt = new NullStatement(test, String.class);
        test.addStatement(nullStmt);
        test.addStatement(new AssignmentStatement(test, ai1, nullStmt.getReturnValue()));

        IntPrimitiveStatement intStmt = new IntPrimitiveStatement(test, 42);
        test.addStatement(intStmt);

        ConstructorStatement sutCS = new ConstructorStatement(test, new GenericConstructor(NullString.class.getConstructor(), NullString.class), new ArrayList<VariableReference>());
        VariableReference sut = test.addStatement(sutCS);

        List<VariableReference> parameters = new ArrayList<>();
        parameters.add(ai0);
        test.addStatement(new MethodStatement(test, new GenericMethod(NullString.class.getMethods()[0], NullString.class), sut, parameters));
        parameters = new ArrayList<>();
        parameters.add(ai1);
        test.addStatement(new MethodStatement(test, new GenericMethod(NullString.class.getMethods()[0], NullString.class), sut, parameters));
        parameters = new ArrayList<>();
        parameters.add(intStmt.getReturnValue());
        VariableReference call = test.addStatement(new MethodStatement(test, new GenericMethod(NullString.class.getMethods()[0], NullString.class), sut, parameters));

        BooleanPrimitiveStatement boolStmt = new BooleanPrimitiveStatement(test, true);
        test.addStatement(boolStmt);
        test.addStatement(new AssignmentStatement(test, boolStmt.getReturnValue(), call));

        (new SimpleMutationAssertionGenerator()).addAssertions(test);
        System.out.println(test.toCode());

        //Visit the tests
        VariableNamesTestVisitor visitor = new VariableNamesTestVisitor();
        test.accept(visitor);
        visitor.printAll();
    }
}
