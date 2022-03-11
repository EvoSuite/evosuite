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
package org.evosuite.runtime.jvm;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ShutdownHookHandlerTest {

    @Before
    public void init() {
        ShutdownHookHandler.getInstance().initHandler();
    }

    @After
    public void tearDown() {
        //be sure no hook is left
        ShutdownHookHandler.getInstance().processWasHalted();
    }

    @Test
    public void testAddHook() {

        int n = ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks();

        Runtime.getRuntime().addShutdownHook(new Thread() {
        });

        Assert.assertEquals(n + 1, ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks());
    }

    @Test
    public void testDoubleInit() {
        int n = ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks();

        Runtime.getRuntime().addShutdownHook(new Thread() {
        });

        //this should remove the above hook thread
        ShutdownHookHandler.getInstance().initHandler();

        Assert.assertEquals(n, ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks());
    }

    @Test
    public void testNormalExecution() {

        final int[] array = new int[1];
        final int value = 42;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                array[0] = value;
            }
        });

        //value not modified yet
        Assert.assertNotEquals(value, array[0]);

        ShutdownHookHandler.getInstance().executeAddedHooks();

        //hook should had modified the value by now
        Assert.assertEquals(value, array[0]);
    }

    @Test
    public void testExecutionWithException() {
        int n = ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                throw new IllegalStateException();
            }
        });

        try {
            ShutdownHookHandler.getInstance().executeAddedHooks();
            Assert.fail();
        } catch (IllegalStateException e) {
            //expected
        }

        //even if failed, hook should had been removed
        Assert.assertEquals(n, ShutdownHookHandler.getInstance().getNumberOfAllExistingHooks());
    }
}
