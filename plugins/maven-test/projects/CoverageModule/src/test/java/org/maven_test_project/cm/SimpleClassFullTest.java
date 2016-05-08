package org.maven_test_project.cm;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleClassFullTest {

    @Test
    public void testIsPositiveTrue() throws Exception {
        SimpleClass cut = new SimpleClass();
        assertTrue(cut.isPositive(50));
    }

    @Test
    public void testIsPositiveFalse() throws Exception {
        SimpleClass cut = new SimpleClass();
        assertFalse(cut.isPositive(-50));
    }
}