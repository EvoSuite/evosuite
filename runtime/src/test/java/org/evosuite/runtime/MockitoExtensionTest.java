package org.evosuite.runtime;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class MockitoExtensionTest {

    public static class OverrideToString{
        @Override
        public String toString(){
            return "foo";
        }
    }

    @Test
    public void testConfirmDoReturnChain(){
        String a = "a";
        String b = "b";

        OverrideToString obj = mock(OverrideToString.class);
        doReturn(a).doReturn(b).when(obj).toString();

        assertEquals(a, obj.toString());
        assertEquals(b, obj.toString());
        assertEquals(b, obj.toString());
        assertEquals(b, obj.toString());
    }

    @Test
    public void testDoReturnMultiple(){
        String a = "a";
        String b = "b";

        OverrideToString obj = mock(OverrideToString.class);
        MockitoExtension.doReturn(a,b).when(obj).toString();

        assertEquals(a, obj.toString());
        assertEquals(b, obj.toString());
        assertEquals(b, obj.toString());
        assertEquals(b, obj.toString());
    }

}