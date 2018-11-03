package org.evosuite.setup;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestClusterUtilTest {

    @Test
    public void testIsAnonymousWithValidName() {
        assertFalse(TestClusterUtils.isAnonymousClass(TestClusterUtilTest.class.getName()));
    }

    @Test
    public void testIsAnonymousWithVAnonymousClassName() {
        Object o = new Object() {
            // ...
        };
        assertTrue(TestClusterUtils.isAnonymousClass(o.getClass().getName()));
    }

    @Test
    public void testIsAnonymousWithNameEndingWithDollar() {
        assertFalse(TestClusterUtils.isAnonymousClass("Option$None$"));
    }
}
