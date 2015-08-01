package org.evosuite.runtime.fm;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
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

    @Test
    public void testBase(){

        EvoInvocationListener listener = new EvoInvocationListener();
        Foo foo = mock(Foo.class, withSettings().invocationListeners(listener));

        when(foo.parseString(any())).thenReturn(1);
        when(foo.parseString(any(), anyBoolean())).thenReturn(2);
        when(foo.parseString(any(), any(Object.class))).thenReturn(3);
        when(foo.parseString(any(), any(Foo.class))).thenReturn(4);

        List<MethodDescriptor> list = listener.getViewOfMethodDescriptors();
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

        list = listener.getViewOfMethodDescriptors();
        //TODO: as it is now, in Mockito we cannot distinguish between overloaded methods with same cardinality
        Assert.assertEquals(2, list.size());
    }
}