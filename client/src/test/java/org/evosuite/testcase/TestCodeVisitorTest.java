/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.evosuite.utils.generic.WildcardTypeImpl;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Andrea Arcuri on 02/07/15.
 */
public class TestCodeVisitorTest {

    public static <T extends Servlet> T foo(T servlet) {
        return servlet;
    }

    public static <T> T bar(T obj) {
        return obj;
    }

    public static class ClassWithGeneric<T extends Servlet> {
        public T hello(T servlet) {
            return servlet;
        }
    }

    public static class FakeServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        public FakeServlet() {
        }
    }

    @Test
    public void testGenerics_methodWithExtends() throws NoSuchMethodException, ConstructionFailedException {

        //first construct a test case for the Generic method
        TestCase tc = new DefaultTestCase();
        TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);
        VariableReference genericClass = TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(ClassWithGeneric.class.getDeclaredConstructor(), ClassWithGeneric.class), 1, 0);


        Method m = ClassWithGeneric.class.getDeclaredMethod("hello", Servlet.class);
        GenericMethod gm = new GenericMethod(m, ClassWithGeneric.class);
        TestFactory.getInstance().addMethodFor(tc, genericClass, gm, 2);


        //Check if generic types were correctly analyzed/inferred
        Type[] types = gm.getParameterTypes();
        Assert.assertEquals(1, types.length); //only 1 input
        Type type = types[0];
        Assert.assertNotNull(type);
        TypeVariable<?> tv = (TypeVariable<?>) type;
        Assert.assertEquals(1, tv.getBounds().length);

        Class<?> upper = (Class<?>) tv.getBounds()[0];
        Assert.assertEquals(Servlet.class, upper);


        //Finally, visit the test
        TestCodeVisitor visitor = new TestCodeVisitor();
        tc.accept(visitor); //should not throw exception        
    }

    @Test
    public void testGenerics_staticMethod() throws NoSuchMethodException, ConstructionFailedException {

        //first construct a test case for the Generic method
        TestCase tc = new DefaultTestCase();
        TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(Object.class.getDeclaredConstructor(), Object.class), 0, 0);

        Method m = TestCodeVisitorTest.class.getDeclaredMethod("bar", Object.class);
        GenericMethod gm = new GenericMethod(m, TestCodeVisitorTest.class);
        TestFactory.getInstance().addMethod(tc, gm, 1, 0);


        //Check if generic types were correctly analyzed/inferred
        Type[] types = gm.getParameterTypes();

        Assert.assertEquals(1, types.length); //only 1 input
        Type type = types[0];
        Assert.assertNotNull(type);
        WildcardTypeImpl wt = (WildcardTypeImpl) type;
        Assert.assertEquals(0, wt.getLowerBounds().length);
        Assert.assertEquals(1, wt.getUpperBounds().length);

        Class<?> upper = (Class<?>) wt.getUpperBounds()[0];
        Assert.assertEquals(Object.class, upper);

        //Finally, visit the test
        TestCodeVisitor visitor = new TestCodeVisitor();
        tc.accept(visitor); //should not throw exception
        System.out.println(visitor.getCode());
    }

    @Test
    public void testGenerics_staticMethodWithExtends() throws NoSuchMethodException, ConstructionFailedException {

        //first construct a test case for the Generic method
        TestCase tc = new DefaultTestCase();
        TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(FakeServlet.class.getDeclaredConstructor(), FakeServlet.class), 0, 0);

        Method m = TestCodeVisitorTest.class.getDeclaredMethod("foo", Servlet.class);
        GenericMethod gm = new GenericMethod(m, TestCodeVisitorTest.class);
        TestFactory.getInstance().addMethod(tc, gm, 1, 0);


        //Check if generic types were correctly analyzed/inferred
        Type[] types = gm.getParameterTypes();
        Assert.assertEquals(1, types.length); //only 1 input
        Type type = types[0];
        Assert.assertNotNull(type);
        WildcardTypeImpl wt = (WildcardTypeImpl) type;
        Assert.assertEquals(0, wt.getLowerBounds().length);
        Assert.assertEquals(1, wt.getUpperBounds().length);

        Class<?> upper = (Class<?>) wt.getUpperBounds()[0];
        Assert.assertEquals(Object.class, upper);

        //Finally, visit the test
        TestCodeVisitor visitor = new TestCodeVisitor();
        tc.accept(visitor); //should not throw exception
    }

    @Test
    public void testClashingImportNames() throws NoSuchMethodException, ConstructionFailedException {
        TestCase tc = new DefaultTestCase();
        TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass.class.getDeclaredConstructor(), com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass.class), 0, 0);
        TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.class.getDeclaredConstructor(), com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.class), 1, 0);
        TestCodeVisitor visitor = new TestCodeVisitor();
        tc.accept(visitor);
        System.out.println(visitor.getCode());
        Set<Class<?>> imports = visitor.getImports();

        // Imported
        assertTrue(imports.contains(com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass.class));

        // Not imported as the fully qualified name is used
        assertFalse(imports.contains(com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.class));
        Assert.assertEquals("ExampleWithInnerClass", visitor.getClassName(com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass.class));
        Assert.assertEquals("com.examples.with.different.packagename.subpackage.ExampleWithInnerClass", visitor.getClassName(com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.class));
    }

    @Test
    public void testClashingImportNamesSubClasses() throws NoSuchMethodException, ConstructionFailedException {
        TestCase tc = new DefaultTestCase();
        TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass.Foo.class.getDeclaredConstructor(), com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass.Foo.class), 0, 0);
        TestFactory.getInstance().addConstructor(tc,
                new GenericConstructor(com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.Bar.class.getDeclaredConstructor(), com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.Bar.class), 1, 0);
        TestCodeVisitor visitor = new TestCodeVisitor();
        tc.accept(visitor);
        System.out.println(visitor.getCode());
        Set<Class<?>> imports = visitor.getImports();

        // Imported
        assertTrue(imports.contains(com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass.class));

        // Not imported as the fully qualified name is used
        assertFalse(imports.contains(com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.class));
        Assert.assertEquals("ExampleWithInnerClass", visitor.getClassName(com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass.class));
        Assert.assertEquals("com.examples.with.different.packagename.subpackage.ExampleWithInnerClass", visitor.getClassName(com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.class));
        Assert.assertEquals("ExampleWithInnerClass.Foo", visitor.getClassName(com.examples.with.different.packagename.otherpackage.ExampleWithInnerClass.Foo.class));
        Assert.assertEquals("com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.Bar", visitor.getClassName(com.examples.with.different.packagename.subpackage.ExampleWithInnerClass.Bar.class));
    }
}