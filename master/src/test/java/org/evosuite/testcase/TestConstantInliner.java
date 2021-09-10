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
package org.evosuite.testcase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.examples.with.different.packagename.TrivialInt;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Test;

import com.examples.with.different.packagename.ObjectParameter;
import com.examples.with.different.packagename.StringConstantInliningExample;

public class TestConstantInliner {

    @Test
    public void testArrayIndexInlining() throws NoSuchMethodException, SecurityException {
        DefaultTestCase test = new DefaultTestCase();
        ConstructorStatement cs = new ConstructorStatement(test, new GenericConstructor(Object.class.getConstructor(), Object.class), new ArrayList<>());
        VariableReference objectVar = test.addStatement(cs);

        ArrayStatement as = new ArrayStatement(test, Object[].class, 3);
        test.addStatement(as);

        ArrayReference arrayVar = as.getArrayReference();

        ArrayIndex ai0 = new ArrayIndex(test, arrayVar, 0);
        ArrayIndex ai1 = new ArrayIndex(test, arrayVar, 1);
        ArrayIndex ai2 = new ArrayIndex(test, arrayVar, 2);
        test.addStatement(new AssignmentStatement(test, ai0, objectVar));
        test.addStatement(new AssignmentStatement(test, ai1, objectVar));
        test.addStatement(new AssignmentStatement(test, ai2, objectVar));

        ConstructorStatement sutCS = new ConstructorStatement(test, new GenericConstructor(ObjectParameter.class.getConstructor(), ObjectParameter.class), new ArrayList<>());
        VariableReference sut = test.addStatement(sutCS);

        List<VariableReference> parameters = new ArrayList<>();
        parameters.add(ai0);
        test.addStatement(new MethodStatement(test, new GenericMethod(ObjectParameter.class.getMethods()[0], ObjectParameter.class), sut, parameters));
        parameters = new ArrayList<>();
        parameters.add(ai1);
        test.addStatement(new MethodStatement(test, new GenericMethod(ObjectParameter.class.getMethods()[0], ObjectParameter.class), sut, parameters));
        parameters = new ArrayList<>();
        parameters.add(ai2);
        test.addStatement(new MethodStatement(test, new GenericMethod(ObjectParameter.class.getMethods()[0], ObjectParameter.class), sut, parameters));
        System.out.println(test.toCode());

        ConstantInliner inliner = new ConstantInliner();
        inliner.inline(test);

        String code = test.toCode();
        assertFalse(code.contains("objectParameter0.testMe(objectArray0"));
    }

    @Test
    public void testNumericArrayIndexInlining() throws NoSuchMethodException, SecurityException {
        DefaultTestCase test = new DefaultTestCase();
        PrimitiveStatement<?> primitiveStatement = PrimitiveStatement.getPrimitiveStatement(test, int.class);
        VariableReference intVar = test.addStatement(primitiveStatement);

        ArrayStatement as = new ArrayStatement(test, int[].class, 3);
        test.addStatement(as);

        ArrayReference arrayVar = as.getArrayReference();

        ArrayIndex ai0 = new ArrayIndex(test, arrayVar, 0);
        ArrayIndex ai1 = new ArrayIndex(test, arrayVar, 1);
        ArrayIndex ai2 = new ArrayIndex(test, arrayVar, 2);
        test.addStatement(new AssignmentStatement(test, ai0, intVar));
        test.addStatement(new AssignmentStatement(test, ai1, intVar));
        test.addStatement(new AssignmentStatement(test, ai2, intVar));

        ConstructorStatement sutCS = new ConstructorStatement(test, new GenericConstructor(TrivialInt.class.getConstructor(), TrivialInt.class), new ArrayList<>());
        VariableReference sut = test.addStatement(sutCS);

        List<VariableReference> parameters = new ArrayList<>();
        parameters.add(ai0);
        test.addStatement(new MethodStatement(test, new GenericMethod(TrivialInt.class.getMethods()[0], TrivialInt.class), sut, parameters));
        parameters = new ArrayList<>();
        parameters.add(ai1);
        test.addStatement(new MethodStatement(test, new GenericMethod(TrivialInt.class.getMethods()[0], TrivialInt.class), sut, parameters));
        parameters = new ArrayList<>();
        parameters.add(ai2);
        test.addStatement(new MethodStatement(test, new GenericMethod(TrivialInt.class.getMethods()[0], TrivialInt.class), sut, parameters));
        System.out.println(test.toCode());

        ConstantInliner inliner = new ConstantInliner();
        inliner.inline(test);

        String code = test.toCode();
        System.out.println(test.toCode());
        assertFalse(code.contains("trivialInt0.testMe(int"));
    }


    @Test
    public void testStringQuoting() throws NoSuchMethodException, SecurityException {
        DefaultTestCase test = new DefaultTestCase();
        ConstructorStatement cs = new ConstructorStatement(test, new GenericConstructor(StringConstantInliningExample.class.getConstructor(), StringConstantInliningExample.class), new ArrayList<>());
        VariableReference objectVar = test.addStatement(cs);

        StringPrimitiveStatement stringStatement = new StringPrimitiveStatement(test, "EXAMPLE");
        VariableReference stringParam = test.addStatement(stringStatement);

        List<VariableReference> parameters = new ArrayList<>();
        parameters.add(stringParam);
        test.addStatement(new MethodStatement(test, new GenericMethod(StringConstantInliningExample.class.getMethods()[0], StringConstantInliningExample.class), objectVar, parameters));
        System.out.println(test.toCode());

        ConstantInliner inliner = new ConstantInliner();
        inliner.inline(test);

        String code = test.toCode();
        System.out.println(code);
        assertFalse(code.contains("foo(EXAMPLE)"));
        assertTrue(code.contains("foo(\"EXAMPLE\")"));
    }

    @Test
    public void testStringEndingWithClass() throws NoSuchMethodException, SecurityException {
        DefaultTestCase test = new DefaultTestCase();
        ConstructorStatement cs = new ConstructorStatement(test, new GenericConstructor(StringConstantInliningExample.class.getConstructor(), StringConstantInliningExample.class), new ArrayList<>());
        VariableReference objectVar = test.addStatement(cs);

        StringPrimitiveStatement stringStatement = new StringPrimitiveStatement(test, "test.class");
        VariableReference stringParam = test.addStatement(stringStatement);

        List<VariableReference> parameters = new ArrayList<>();
        parameters.add(stringParam);
        test.addStatement(new MethodStatement(test, new GenericMethod(StringConstantInliningExample.class.getMethods()[0], StringConstantInliningExample.class), objectVar, parameters));
        System.out.println(test.toCode());

        ConstantInliner inliner = new ConstantInliner();
        inliner.inline(test);

        String code = test.toCode();
        System.out.println(code);
        assertFalse(code.contains("foo(test.class)"));
        assertTrue(code.contains("foo(\"test.class\")"));
    }


}
