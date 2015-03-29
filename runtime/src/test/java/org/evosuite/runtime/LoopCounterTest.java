package org.evosuite.runtime;

import org.junit.After;
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
}
