package com.testbuild.support;

import org.evosuite.runtime.mock.MockFramework;
import org.junit.Test;

import static org.junit.Assert.*;

public class FooTest {


    @Test
    public void testDoesFileExist_deactivatedInstrumentation() throws Exception {
        //the file does actually exists
        assertTrue(Foo.doesFileExist());
    }

    @Test
    public void testDoesFileExist_withInstrumentation() throws Exception {
        MockFramework.enable();
        try {
            assertFalse(Foo.doesFileExist());
        } finally {
            MockFramework.disable();
        }
    }

}