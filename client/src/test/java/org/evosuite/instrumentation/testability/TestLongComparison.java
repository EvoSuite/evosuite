package org.evosuite.instrumentation.testability;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by gordon on 03/02/2016.
 */
public class TestLongComparison {

    @Test
    public void testLongComparison() {
        long l1 = 0L;
        long l2 = 10L;
        assertEquals(0, BooleanHelper.longSub(l1, l1));
        assertTrue(BooleanHelper.longSub(l2, l1) > 0);
        assertTrue(BooleanHelper.longSub(l1, l2) < 0);
    }

    @Test
    public void testLongExampleFromMath() {
        long l1 = -9223372036854775808L;
        long l2 = 1528L;
        assertEquals(0, BooleanHelper.longSub(l1, l1));
        assertTrue(BooleanHelper.longSub(l2, l1) > 0);
        assertTrue(BooleanHelper.longSub(l1, l2) < 0);
    }


}
