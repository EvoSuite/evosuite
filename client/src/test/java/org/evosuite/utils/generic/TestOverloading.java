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
package org.evosuite.utils.generic;

import com.examples.with.different.packagename.utils.generic.BigFraction;
import com.examples.with.different.packagename.utils.generic.ClassWithOverloadedMethods;
import com.examples.with.different.packagename.utils.generic.ClassWithoutOverloadedMethods;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by gordon on 19/04/2017.
 */
public class TestOverloading {

    @Test
    public void testOverloadedConstructor() {
        Class<?> clazz = ClassWithOverloadedMethods.class;
        Constructor<?> constructor1 = clazz.getConstructors()[0];
        Constructor<?> constructor2 = clazz.getConstructors()[1];

        GenericConstructor genericConstructor1 = new GenericConstructor(constructor1, clazz);
        GenericConstructor genericConstructor2 = new GenericConstructor(constructor2, clazz);

        TestCase test = new DefaultTestCase();
        ConstantValue intValue = new ConstantValue(test, int.class);
        VariableReference integerVar = new VariableReferenceImpl(test, Integer.class);
        List<VariableReference> parameters = Arrays.asList(intValue, integerVar);

        assertTrue(genericConstructor1.isOverloaded(parameters));
        assertTrue(genericConstructor2.isOverloaded(parameters));
    }

    @Test
    public void testOverloadedConstructorBigFraction() {
        Class<?> clazz = BigFraction.class;
        Constructor<?> constructor1 = clazz.getConstructors()[0];
        Constructor<?> constructor2 = clazz.getConstructors()[1];

        GenericConstructor genericConstructor1 = new GenericConstructor(constructor1, clazz);
        GenericConstructor genericConstructor2 = new GenericConstructor(constructor2, clazz);

        TestCase test = new DefaultTestCase();
        ConstantValue longValue = new ConstantValue(test, long.class);
        ConstantValue intValue = new ConstantValue(test, int.class);
        List<VariableReference> parameters = Arrays.asList(longValue, intValue);

        assertTrue(genericConstructor1.isOverloaded(parameters));
        assertTrue(genericConstructor2.isOverloaded(parameters));
    }

    @Test
    public void testNotOverloadedConstructor() {
        Class<?> clazz = ClassWithoutOverloadedMethods.class;
        Constructor<?> constructor1 = clazz.getConstructors()[0];
        Constructor<?> constructor2 = clazz.getConstructors()[1];

        GenericConstructor genericConstructor1 = new GenericConstructor(constructor1, clazz);
        GenericConstructor genericConstructor2 = new GenericConstructor(constructor2, clazz);

        TestCase test = new DefaultTestCase();
        ConstantValue intValue = new ConstantValue(test, int.class);
        VariableReference stringVar = new VariableReferenceImpl(test, String.class);
        List<VariableReference> parameters1 = Arrays.asList(intValue);
        List<VariableReference> parameters2 = Arrays.asList(stringVar);

        assertFalse(genericConstructor1.isOverloaded(parameters1));
        assertFalse(genericConstructor2.isOverloaded(parameters2));
        assertFalse(genericConstructor1.isOverloaded(parameters2));
        assertFalse(genericConstructor2.isOverloaded(parameters1));
    }

    @Test
    public void testOverloadedMethod() {
        Class<?> clazz = ClassWithOverloadedMethods.class;
        Method method1 = clazz.getMethods()[0];
        Method method2 = clazz.getMethods()[1];

        GenericMethod genericMethod1 = new GenericMethod(method1, clazz);
        GenericMethod genericMethod2 = new GenericMethod(method2, clazz);

        TestCase test = new DefaultTestCase();
        ConstantValue intValue = new ConstantValue(test, int.class);
        VariableReference integerVar = new VariableReferenceImpl(test, Integer.class);
        List<VariableReference> parameters1 = Arrays.asList(intValue);
        List<VariableReference> parameters2 = Arrays.asList(integerVar);

        assertTrue(genericMethod1.isOverloaded());
        assertTrue(genericMethod2.isOverloaded());
        assertTrue(genericMethod1.isOverloaded(parameters1));
        assertTrue(genericMethod2.isOverloaded(parameters1));
        assertTrue(genericMethod1.isOverloaded(parameters2));
        assertTrue(genericMethod2.isOverloaded(parameters2));
    }


    @Test
    public void testNotOverloadedMethod() {
        Class<?> clazz = ClassWithoutOverloadedMethods.class;
        Method method1 = clazz.getMethods()[0];
        Method method2 = clazz.getMethods()[1];

        GenericMethod genericMethod1 = new GenericMethod(method1, clazz);
        GenericMethod genericMethod2 = new GenericMethod(method2, clazz);

        TestCase test = new DefaultTestCase();
        ConstantValue intValue = new ConstantValue(test, int.class);
        VariableReference stringVar = new VariableReferenceImpl(test, String.class);
        List<VariableReference> parameters1 = Arrays.asList(intValue);
        List<VariableReference> parameters2 = Arrays.asList(stringVar);

        assertFalse(genericMethod1.isOverloaded());
        assertFalse(genericMethod2.isOverloaded());
        assertFalse(genericMethod1.isOverloaded(parameters1));
        assertFalse(genericMethod2.isOverloaded(parameters1));
        assertFalse(genericMethod1.isOverloaded(parameters2));
        assertFalse(genericMethod2.isOverloaded(parameters2));
    }
}
