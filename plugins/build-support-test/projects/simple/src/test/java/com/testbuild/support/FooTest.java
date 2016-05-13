package com.testbuild.support;

import org.evosuite.runtime.instrumentation.InstrumentedClass;
import org.evosuite.runtime.mock.MockFramework;
import org.junit.Test;

import static org.junit.Assert.*;

public class FooTest {

    /*
        These tests have to fail when run from IDE.
        But should pass from build (Maven, Gradle and Ant) if JUnit listener
        is properly initialized
     */

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

    @Test
    public void testInstrumentation(){
        assertTrue(new Foo() instanceof InstrumentedClass);
    }
}