package org.evosuite.runtime;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ViolatedAssumptionAnswerTest {

    private class Foo{
        public void doSomething(){}
        public int getX(){return -1;}
        public int getY(){return 2;}
        public int bar(int x, Foo foo){return 7;}
    }

    @Test
    public void test(){

        Foo foo = mock(Foo.class, new ViolatedAssumptionAnswer<>());

        int y = 5;
        int bar = 9;
        //when(foo.getY()).thenReturn(y); //this does not work, as it throws exception!
        doReturn(y).when(foo).getY();
        doReturn(bar).when(foo).bar(anyInt(),any());

        foo.doSomething(); //no exception, as it is void

        try{
            foo.getX();
            fail();
        } catch (FalsePositiveException e){
            //expected
        }

        int res = foo.getY();
        assertEquals(y, res);

        res = foo.bar(4,null);
        assertEquals(bar, res);
    }
}