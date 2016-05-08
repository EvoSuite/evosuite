package org.maven_test_project.cm;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SimpleClassPartialTest {

    @Test
    public void testIsPositiveTrue() throws Exception {
        SimpleClass cut = new SimpleClass();
        assertTrue(cut.isPositive(50));
    }
}
