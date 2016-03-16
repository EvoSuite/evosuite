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
package org.evosuite.testcase.fm;

import org.evosuite.utils.ParameterizedTypeImpl;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Created by Andrea Arcuri on 27/07/15.
 */
public class EvoInvocationListenerTest {

    public interface Foo{
        public int parseString(String s);
        public int parseString(String s, boolean flag);
        public int parseString(String s, Object obj);
        public int parseString(String s, Foo foo);
    }

    public class AClassWithFinal{
        public final boolean getFoo(){
            return true;
        }
    }

    public interface AGenericClass<T>{
        boolean genericAsInput(T t);
    }

    @Test
    public void testGenerics(){

        ParameterizedTypeImpl type = new ParameterizedTypeImpl(AGenericClass.class, new Type[]{String.class}, null);
        EvoInvocationListener listener = new EvoInvocationListener(type);
        AGenericClass<String> aGenericClass = (AGenericClass<String>) mock(AGenericClass.class, withSettings().invocationListeners(listener));
        when(aGenericClass.genericAsInput(any((Class<String>)type.getActualTypeArguments()[0]))).thenReturn(true);
        listener.activate();

        boolean b = aGenericClass.genericAsInput("foo");
        assertTrue(b);

        List<MethodDescriptor> list = listener.getCopyOfMethodDescriptors();
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testCheckGenericsProperties() throws Exception{
        AGenericClass<String> aGenericClass = (AGenericClass<String>) mock(AGenericClass.class);

        Method m = aGenericClass.getClass().getDeclaredMethod("genericAsInput", Object.class);
        assertEquals(""+Object.class.toString(), m.getParameterTypes()[0].toString());
    }

    @Test
    public void testFinal(){
        /*
            If no special instrumentation is done, we cannot handle final methods
         */
        EvoInvocationListener listener = new EvoInvocationListener(AClassWithFinal.class);
        AClassWithFinal foo = mock(AClassWithFinal.class, withSettings().invocationListeners(listener));
        listener.activate();

        foo.getFoo(); // this is not mocked

        List<MethodDescriptor> list = listener.getCopyOfMethodDescriptors();
        Assert.assertEquals(0, list.size());
    }


    @Test
    public void testBase(){

        EvoInvocationListener listener = new EvoInvocationListener(Foo.class);
        Foo foo = mock(Foo.class, withSettings().invocationListeners(listener));

        when(foo.parseString(any())).thenReturn(1);
        when(foo.parseString(any(), anyBoolean())).thenReturn(2);
        when(foo.parseString(any(), any(Object.class))).thenReturn(3);
        when(foo.parseString(any(), any(Foo.class))).thenReturn(4);

        List<MethodDescriptor> list = listener.getCopyOfMethodDescriptors();
        Assert.assertEquals(0, list.size()); //not active yet
        listener.activate();

        int res = foo.parseString("foo");
        Assert.assertEquals(1, res);

        res = foo.parseString("bar",true);
        Assert.assertEquals(2, res);

        res = foo.parseString("foo");
        Assert.assertEquals(1, res);

        res = foo.parseString("bar",new Object());
        Assert.assertEquals(3, res);

        res = foo.parseString("bar", (Foo) null);
        Assert.assertEquals(4, res);

        list = listener.getCopyOfMethodDescriptors();
        Assert.assertEquals(4, list.size());
    }
}