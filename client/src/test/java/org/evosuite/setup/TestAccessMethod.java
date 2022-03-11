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
package org.evosuite.setup;

import org.evosuite.Properties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Set;

public class TestAccessMethod {
    @After
    public void resetProperties() {
        Properties.CLASS_PREFIX = "";
        Properties.TARGET_CLASS = "";
    }

    protected Method getMethod(Class<?> clazz, String name) {
        Set<Method> methods = TestClusterUtils.getMethods(clazz);
        for (Method m : methods) {
            if (m.getName().equals(name))
                return m;
        }
        Assert.fail("No such method: " + name);
        return null;
    }

    @Test
    public void testPublicMethod() {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";
        Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
                "publicMethod");
        boolean result = TestUsageChecker.canUse(f);
        Assert.assertTrue(result);
    }

    @Test
    public void testDefaultMethod() {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";
        Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
                "defaultMethod");
        boolean result = TestUsageChecker.canUse(f);
        Assert.assertFalse(result);
    }

    @Test
    public void testProtectedMethod() {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";
        Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
                "protectedMethod");
        boolean result = TestUsageChecker.canUse(f);
        Assert.assertFalse(result);
    }

    @Test
    public void testPrivateMethod() {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";
        Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
                "privateMethod");
        boolean result = TestUsageChecker.canUse(f);
        Assert.assertFalse(result);
    }

    @Test
    public void testPublicMethodTargetPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
        Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
                "publicMethod");
        boolean result = TestUsageChecker.canUse(f);
        Assert.assertTrue(result);
    }

    @Test
    public void testDefaultMethodTargetPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
        Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
                "defaultMethod");
        boolean result = TestUsageChecker.canUse(f);
        Assert.assertTrue(result);
    }

    @Test
    public void testDefaultMethodInSuperClass() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
        Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
                "defaultMethodInSuperClass");
        boolean result = TestUsageChecker.canUse(f);
        Assert.assertFalse(result);
    }

    @Test
    public void testProtectedMethodTargetPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
        Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
                "protectedMethod");
        boolean result = TestUsageChecker.canUse(f);
        Assert.assertTrue(result);
    }

    @Test
    public void testPrivateMethodTargetPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
        Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
                "privateMethod");
        boolean result = TestUsageChecker.canUse(f);
        Assert.assertFalse(result);
    }

    @Test
    public void testPublicMethodTargetSubPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename.subpackage";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.subpackage.Foo";
        Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
                "publicMethod");
        boolean result = TestUsageChecker.canUse(f,
                com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
        Assert.assertTrue(result);
    }

    @Test
    public void testProtectedMethodTargetSubPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename.subpackage";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.subpackage.Foo";
        Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
                "protectedMethod");
        boolean result = TestUsageChecker.canUse(f,
                com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
        Assert.assertFalse(result);
    }

    @Test
    public void testDefaultMethodTargetSubPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename.subpackage";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.subpackage.Foo";
        Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
                "defaultMethod");
        boolean result = TestUsageChecker.canUse(f,
                com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
        Assert.assertFalse(result);
    }

    @Test
    public void testPrivateMethodTargetSubPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename.subpackage";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.subpackage.Foo";
        Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
                "privateMethod");
        boolean result = TestUsageChecker.canUse(f,
                com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
        Assert.assertFalse(result);
    }

    @Test
    public void testPublicMethodTargetFromSubPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
        Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
                "publicMethod");
        boolean result = TestUsageChecker.canUse(f,
                com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
        Assert.assertTrue(result);
    }

    @Test
    public void testProtectedMethodTargetFromSubPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
        Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
                "protectedMethod");
        boolean result = TestUsageChecker.canUse(f,
                com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
        Assert.assertFalse(result);
    }

    @Test
    public void testDefaultMethodTargetFromSubPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
        Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
                "defaultMethod");
        boolean result = TestUsageChecker.canUse(f,
                com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
        Assert.assertFalse(result);
    }

    @Test
    public void testPrivateMethodTargetFromSubPackage() {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
        Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
                "privateMethod");
        boolean result = TestUsageChecker.canUse(f,
                com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
        Assert.assertFalse(result);
    }

    @Test
    public void testArrayListBug() {
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.ArrayStack";
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        try {
            Method m = getMethod(java.util.ArrayList.class, "elementData");
            boolean result = TestUsageChecker.canUse(m,
                    com.examples.with.different.packagename.ArrayStack.class);
            Assert.assertFalse(result);
        } catch (Throwable e) {
            // Method elementData only exists in Java 7
        }
    }

    @Test
    public void testMethodReturnsUnaccessibleClass() {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";

        Method m = getMethod(com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass.class,
                "getFoo");
        boolean result = TestUsageChecker.canUse(m,
                com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass.class);
        Assert.assertFalse(result);
    }

    @Test
    public void testMethodReturnsUnaccessibleClass2() throws ClassNotFoundException {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.ClassWithPrivateInnerClass";

        Class<?> clazz = Class.forName("com.examples.with.different.packagename.ClassWithPrivateInnerClass");
        Method m = getMethod(clazz, "getProperty");
        boolean result = TestUsageChecker.canUse(m, clazz);
        Assert.assertFalse(result);
    }

    @Test
    public void testMethodReturnsUnaccessibleClass3() throws ClassNotFoundException {
        Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
        Properties.TARGET_CLASS = "com.examples.with.different.packagename.ClassWithPrivateInnerClass";

        Class<?> clazz = Class.forName("com.examples.with.different.packagename.ClassWithPrivateInnerClass");
        Method m = getMethod(clazz, "getPropertyList");
        boolean result = TestUsageChecker.canUse(m, clazz);
        Assert.assertFalse(result);
    }


    @Test
    public void testMethodUnaccessibleClassParameter() {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";

        Method m = getMethod(com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass.class,
                "setFoo");
        boolean result = TestUsageChecker.canUse(m,
                com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass.class);
        Assert.assertFalse(result);
    }

    @Test
    public void testAccessibleMethodInClassWithUnaccessibleClassParameterMethod() {
        Properties.CLASS_PREFIX = "some.package";
        Properties.TARGET_CLASS = "some.package.Foo";

        Method m = getMethod(com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass.class,
                "bar");
        boolean result = TestUsageChecker.canUse(m,
                com.examples.with.different.packagename.otherpackage.ExampleWithStaticPackagePrivateInnerClass.class);
        Assert.assertTrue(result);
    }
}
