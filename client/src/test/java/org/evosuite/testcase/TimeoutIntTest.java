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
package org.evosuite.testcase;

import org.evosuite.testcase.execution.TimeoutHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;

public class TimeoutIntTest {
    protected static int RESULT = -1;

    @Test
    public void testNormalTimeout() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Integer> call = new FakeTestCase();

        TimeoutHandler<Integer> handler = new TimeoutHandler<>();

        RESULT = -1;

        try {
            handler.execute(call, executor, 2000, false);
        } catch (TimeoutException e) {
            executor.shutdownNow();
            try {
                boolean terminated = executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
                Assert.assertTrue(terminated);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            Assert.assertEquals(1, RESULT);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Test
    public void testCPUTimeout() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Integer> call = new FakeTestCase();

        TimeoutHandler<Integer> handler = new TimeoutHandler<>();

        RESULT = -1;

        try {
            handler.execute(call, executor, 2000, true);
        } catch (TimeoutException e) {
            executor.shutdownNow();
            try {
                boolean terminated = executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
                Assert.assertTrue(terminated);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            Assert.assertEquals(2, RESULT);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    protected class FakeTestCase implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                RESULT = 1;
                return 1;
            }

            while (true) {
                if (Thread.currentThread().isInterrupted())
                    break;
                else
                    Thread.yield();
            }

            RESULT = 2;
            return 2;
        }
    }
}
