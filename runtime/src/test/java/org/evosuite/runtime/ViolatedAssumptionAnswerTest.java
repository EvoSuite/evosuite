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

        Foo foo = mock(Foo.class, new ViolatedAssumptionAnswer());

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