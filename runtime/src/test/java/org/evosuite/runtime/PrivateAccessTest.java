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
package org.evosuite.runtime;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.assertTrue;

/**
 * Created by Andrea on 20/02/15.
 */
public class PrivateAccessTest {

    @Before
    public void init() {
        PrivateAccess.setShouldNotFailTest(false);
    }

    @After
    public void tearDown() {
        PrivateAccess.setShouldNotFailTest(true);
    }


    @Test
    public void testPrivateConstructor() throws Throwable {

        FooConstructor.counter = 0;

        PrivateAccess.callDefaultConstructor(FooConstructor.class);

        assertTrue(FooConstructor.counter > 0);
    }


    @Test
    public void testSetField_serialVersionUID() {
        try {
            //it should fail
            PrivateAccess.setVariable(FooFields.class, null, "serialVersionUID", 42L);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            //expected
        }
    }


    @Test
    public void testSetField_static() {
        PrivateAccess.setVariable(FooFields.class, null, "n", 42);
        Assert.assertEquals(42, FooFields.getN());
    }

    @Test
    public void testSetField_instance() {
        FooFields foo = new FooFields();
        PrivateAccess.setVariable(FooFields.class, foo, "s", "bar");
        Assert.assertEquals("bar", foo.getS());
    }

    @Test
    public void testSetField_error() {
        PrivateAccess.setShouldNotFailTest(true);
        PrivateAccess.setVariable(FooFields.class, null, "a non-existing field", 42);
        Assert.fail(); // this should never be reached, as failed "Assumption" inside setVariable
    }

    @Test
    public void testSetField_fail_on_error() {
        PrivateAccess.setShouldNotFailTest(false);
        try {
            PrivateAccess.setVariable(FooFields.class, null, "a non-existing field", 42);
            Assert.fail();
        } catch (FalsePositiveException e) {
            Assert.fail();
        } catch (RuntimeException e) {
            //Ok, expected
        } finally {
            PrivateAccess.setShouldNotFailTest(true);
        }
    }

    @Test
    public void testMethod_static() throws Throwable {
        FooMethods.n = 42;
        Integer res = (Integer) PrivateAccess.callMethod(FooMethods.class, null, "getN", new Object[0], new Class<?>[0]);
        Assert.assertEquals(42, res.intValue());
    }

    @Test
    public void testMethod_error() throws Throwable {
        PrivateAccess.setShouldNotFailTest(true);
        PrivateAccess.callMethod(FooMethods.class, null, "a non-existing method", new Object[0], new Class<?>[0]);
        Assert.fail();
    }

    @Test
    public void testMethod_fail_on_error() {
        PrivateAccess.setShouldNotFailTest(false);
        try {
            PrivateAccess.callMethod(FooMethods.class, null, "a non-existing method", new Object[0], new Class<?>[0]);
            Assert.fail();
        } catch (FalsePositiveException e) {
            Assert.fail();
        } catch (Throwable e) {
            //Ok, expected
        } finally {
            PrivateAccess.setShouldNotFailTest(true);
        }
    }

    @Test
    public void testMethod_zero() throws Throwable {
        FooMethods foo = new FooMethods();
        foo.s = "bar";
        String s = (String) PrivateAccess.callMethod(FooMethods.class, foo, "getS", new Object[0], new Class<?>[0]);
        Assert.assertEquals("bar", s);
    }

    @Test
    public void testMethod_one() throws Throwable {
        FooMethods foo = new FooMethods();
        PrivateAccess.callMethod(FooMethods.class, foo, "set",
                new Object[]{"bar"}, new Class<?>[]{String.class});
        Assert.assertEquals("bar", foo.s);
    }

    @Test
    public void testMethod_sameMethod_but2parameters() throws Throwable {
        FooMethods foo = new FooMethods();
        FooMethods.n = 0;
        PrivateAccess.callMethod(FooMethods.class, foo, "set",
                new Object[]{"bar", 666}, new Class<?>[]{String.class, int.class});
        Assert.assertEquals("bar", foo.s);
        Assert.assertEquals(666, FooMethods.n);
    }

    @Test
    public void testMethod_two_andReturn() throws Throwable {
        FooMethods foo = new FooMethods();
        String res = (String) PrivateAccess.callMethod(FooMethods.class, foo, "compute",
                new Object[]{"bar", 666}, new Class<?>[]{String.class, int.class});
        Assert.assertEquals("bar666", res);
    }

    @Test
    public void testMethod_throwNPE() throws Throwable {
        try {
            PrivateAccess.callMethod(FooMethods.class, null, "throwNPE", new Object[0], new Class<?>[0]);
            Assert.fail();
        } catch (NullPointerException e) {
            //OK
        }
    }

}


class FooConstructor {

    public static int counter = 0;

    private FooConstructor() {
        counter++;
    }

}

class FooFields implements Serializable {
    private String s;
    private static int n;

    public static final long serialVersionUID = 1L;

    public String getS() {
        return s;
    }

    public static int getN() {
        return n;
    }
}

class FooMethods {
    public String s;
    public static int n;

    private static int getN() {
        return n;
    }

    private String getS() {
        return s;
    }

    private void set(String s) {
        this.s = s;
    }

    private void set(String s, int n) {
        set(s);
        FooMethods.n = n;
    }

    private String compute(String s, int x) {
        return s + x;
    }

    private static void throwNPE() {
        throw new NullPointerException("NPE");
    }
}