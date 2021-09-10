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

import com.examples.with.different.packagename.test.AbstractSuperclass;
import com.examples.with.different.packagename.test.ConcreteSubclass;
import com.examples.with.different.packagename.test.DowncastExample;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.assertion.PrimitiveFieldAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericField;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by gordon on 27/12/2016.
 */
public class DowncastTest {

    @Test
    public void testUnnecessaryDownCast() throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var = builder.appendConstructor(DowncastExample.class.getConstructor());
        VariableReference int0 = builder.appendIntPrimitive(42);
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getANumber", new Class<?>[]{int.class}), int0);
        num0.setType(Integer.class); // This would be set during execution
        VariableReference boolean0 = builder.appendMethod(var, DowncastExample.class.getMethod("testMe", new Class<?>[]{Number.class}), num0);
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
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getANumber", new Class<?>[]{int.class}), int0);
        num0.setType(Integer.class); // This would be set during execution
        VariableReference boolean0 = builder.appendMethod(var, DowncastExample.class.getMethod("testWithInteger", new Class<?>[]{Integer.class}), num0);
        PrimitiveAssertion assertion = new PrimitiveAssertion();
        assertion.setSource(boolean0);
        assertion.setValue(false);
        DefaultTestCase test = builder.getDefaultTestCase();
        test.getStatement(boolean0.getStPosition()).addAssertion(assertion);
        test.removeDownCasts();
        assertEquals(Integer.class, test.getStatement(2).getReturnClass());
    }

    @Test
    public void testDownCastUnnecessaryForInspectorAssertion() throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var = builder.appendConstructor(DowncastExample.class.getConstructor());
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getAbstractFoo"));
        num0.setType(ConcreteSubclass.class); // This would be set during execution

        DefaultTestCase test = builder.getDefaultTestCase();

        Inspector inspector = new Inspector(ConcreteSubclass.class, ConcreteSubclass.class.getMethod("getFoo"));
        InspectorAssertion assertion = new InspectorAssertion(inspector, test.getStatement(num0.getStPosition()), num0, true);

        test.getStatement(num0.getStPosition()).addAssertion(assertion);
        test.removeDownCasts();
        System.out.println(test);
        assertEquals(AbstractSuperclass.class, test.getStatement(1).getReturnClass());
    }

    @Test
    public void testDownCastNecessaryForInspectorAssertion() throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var = builder.appendConstructor(DowncastExample.class.getConstructor());
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getAbstractFoo"));
        num0.setType(ConcreteSubclass.class); // This would be set during execution

        DefaultTestCase test = builder.getDefaultTestCase();

        Inspector inspector = new Inspector(ConcreteSubclass.class, ConcreteSubclass.class.getMethod("getBar"));
        InspectorAssertion assertion = new InspectorAssertion(inspector, test.getStatement(num0.getStPosition()), num0, true);

        test.getStatement(num0.getStPosition()).addAssertion(assertion);
        test.removeDownCasts();
        System.out.println(test);
        assertEquals(ConcreteSubclass.class, test.getStatement(1).getReturnClass());
    }

    @Test
    public void testDownCastUnnecessaryForField() throws NoSuchMethodException, NoSuchFieldException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var = builder.appendConstructor(DowncastExample.class.getConstructor());
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getAbstractFoo"));
        num0.setType(ConcreteSubclass.class); // This would be set during execution

        DefaultTestCase test = builder.getDefaultTestCase();

        PrimitiveFieldAssertion assertion = new PrimitiveFieldAssertion();
        assertion.setValue(true);
        assertion.setSource(num0);
        assertion.setField(AbstractSuperclass.class.getField("fieldInAbstractClass"));

        test.getStatement(num0.getStPosition()).addAssertion(assertion);
        test.removeDownCasts();
        System.out.println(test);
        assertEquals(AbstractSuperclass.class, test.getStatement(1).getReturnClass());
    }

    @Test
    public void testDownCastNecessaryForField() throws NoSuchMethodException, NoSuchFieldException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var = builder.appendConstructor(DowncastExample.class.getConstructor());
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getAbstractFoo"));
        num0.setType(ConcreteSubclass.class); // This would be set during execution

        DefaultTestCase test = builder.getDefaultTestCase();

        PrimitiveFieldAssertion assertion = new PrimitiveFieldAssertion();
        assertion.setValue(true);
        assertion.setSource(num0);
        assertion.setField(ConcreteSubclass.class.getField("fieldInConcreteClass"));

        test.getStatement(num0.getStPosition()).addAssertion(assertion);
        test.removeDownCasts();
        System.out.println(test);
        assertEquals(ConcreteSubclass.class, test.getStatement(1).getReturnClass());
    }

    @Test
    public void testFieldReferenceNeedsDowncast() throws NoSuchMethodException, NoSuchFieldException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var = builder.appendConstructor(DowncastExample.class.getConstructor());
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getAbstractFoo"));
        num0.setType(ConcreteSubclass.class); // This would be set during execution
        VariableReference bool0 = builder.appendBooleanPrimitive(true);
        DefaultTestCase test = builder.getDefaultTestCase();

        FieldReference fr = new FieldReference(test, new GenericField(ConcreteSubclass.class.getField("fieldInConcreteClass"), ConcreteSubclass.class), num0);
        AssignmentStatement statement = new AssignmentStatement(test, fr, bool0);
        test.addStatement(statement);

        test.removeDownCasts();
        System.out.println(test);
        FieldReference fr2 = (FieldReference) test.getStatement(3).getReturnValue();
        assertEquals(ConcreteSubclass.class, fr2.getSource().getVariableClass());
    }

    @Test
    public void testFieldReferenceDoesNotNeedDowncast() throws NoSuchMethodException, NoSuchFieldException {
        TestCaseBuilder builder = new TestCaseBuilder();
        VariableReference var = builder.appendConstructor(DowncastExample.class.getConstructor());
        VariableReference num0 = builder.appendMethod(var, DowncastExample.class.getMethod("getAbstractFoo"));
        num0.setType(ConcreteSubclass.class); // This would be set during execution
        VariableReference bool0 = builder.appendBooleanPrimitive(true);
        DefaultTestCase test = builder.getDefaultTestCase();

        FieldReference fr = new FieldReference(test, new GenericField(AbstractSuperclass.class.getField("fieldInAbstractClass"), AbstractSuperclass.class), num0);
        AssignmentStatement statement = new AssignmentStatement(test, fr, bool0);
        test.addStatement(statement);

        test.removeDownCasts();
        System.out.println(test);
        FieldReference fr2 = (FieldReference) test.getStatement(3).getReturnValue();
        assertEquals(AbstractSuperclass.class, fr2.getSource().getVariableClass());
    }

}
