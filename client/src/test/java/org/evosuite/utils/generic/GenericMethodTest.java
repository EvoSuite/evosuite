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
package org.evosuite.utils.generic;


import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

import javax.servlet.Servlet;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 02/07/15.
 */
public class GenericMethodTest {

    private static class A{
        public static <T> T bar(T obj){return obj;}
    }

    public static class B <T> {
        public T bar(T t){ return t;}
    }

    public static class C <T extends A> {
        public T bar(T t){ return t;}
    }

    public static class D <T extends A> {
        public <T extends B> T bar(T t){ return t;}
    }



    @Test
    public void testGetExactReturnType() throws Exception {

        Method m = B.class.getDeclaredMethod("bar", Object.class);

        GenericMethod gm = new GenericMethod(m,B.class);
        Type res =  gm.getExactReturnType(m, B.class);

        Assert.assertEquals(Object.class, res);
    }

    @Test
    public void testGetExactReturnType_extend() throws Exception {

        try {
            Method m = C.class.getDeclaredMethod("bar", Object.class);
            Assert.fail();
        } catch (Exception e){
            //expected
        }

        Method m = C.class.getDeclaredMethod("bar", A.class);

        GenericMethod gm = new GenericMethod(m,C.class);
        Type res =  gm.getExactReturnType(m, C.class);
        Assert.assertEquals(A.class, res);
    }

    @Test
    public void testGetExactReturnType_extend2() throws Exception {

        try {
            Method m = D.class.getDeclaredMethod("bar", A.class);
            Assert.fail();
        } catch (Exception e){
            //expected
        }

        Method m = D.class.getDeclaredMethod("bar", B.class);

        GenericMethod gm = new GenericMethod(m,D.class);
        Type res =  gm.getExactReturnType(m, D.class);
        Assert.assertEquals(B.class, res);
    }


    @Test
    public void testGetExactReturnType_staticMethod() throws Exception {

        Method m = A.class.getDeclaredMethod("bar", Object.class);

        GenericMethod gm = new GenericMethod(m,A.class);
        Type res =  gm.getExactReturnType(m, A.class);

        //Check if generic types were correctly analyzed/inferred
        Assert.assertNotNull(res);
        WildcardTypeImpl wt = (WildcardTypeImpl) res;        
        Assert.assertEquals(0,wt.getLowerBounds().length);
        Assert.assertEquals(1,wt.getUpperBounds().length);
        
        Class<?> upper = (Class<?>) wt.getUpperBounds()[0];
        Assert.assertEquals(Object.class,upper);
    }

    @Test
    public void testGetExactParameterTypes_staticMethod() throws Exception {
        Method m = A.class.getDeclaredMethod("bar", Object.class);

        GenericMethod gm = new GenericMethod(m,A.class);
        Type res =  gm.getExactParameterTypes(m, A.class)[0];

        //Check if generic types were correctly analyzed/inferred
        Assert.assertNotNull(res);
        WildcardTypeImpl wt = (WildcardTypeImpl) res;        
        Assert.assertEquals(0,wt.getLowerBounds().length);
        Assert.assertEquals(1,wt.getUpperBounds().length);
        
        Class<?> upper = (Class<?>) wt.getUpperBounds()[0];
        Assert.assertEquals(Object.class,upper);
    }
}