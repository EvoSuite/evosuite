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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrea Arcuri on 29/03/15.
 */
public class LoopCounterTest {

    @Before
    public void init() {
        LoopCounter.getInstance().reset();
    }

    @After
    public void tearDown() {
        LoopCounter.getInstance().reset();
    }

    @Test(timeout = 10000)
    public void testInfiniteLoop() {

        Assert.assertTrue(RuntimeSettings.maxNumberOfIterationsPerLoop > 0);//should be on by default

        int first = LoopCounter.getInstance().getNewIndex();
        int second = LoopCounter.getInstance().getNewIndex();

        while (true) {
            LoopCounter.getInstance().checkLoop(first);

            for (int i = 0; i < 100; i++) {
                try {
                    LoopCounter.getInstance().checkLoop(second); //this should fail first
                } catch (TooManyResourcesException e) {
                    //expected
                    return;
                }
            }
        }
    }

    @Test
    public void testNoNewIndex(){
		LoopCounter.getInstance().checkLoop(0);
		LoopCounter.getInstance().checkLoop(1);
		LoopCounter.getInstance().checkLoop(2);
		LoopCounter.getInstance().checkLoop(5);
		LoopCounter.getInstance().checkLoop(6);
    }
}
