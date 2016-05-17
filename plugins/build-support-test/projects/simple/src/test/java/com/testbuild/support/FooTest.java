package com.testbuild.support;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;
import org.evosuite.runtime.instrumentation.InstrumentedClass;
import org.evosuite.runtime.mock.MockFramework;
import org.junit.Test;

import static org.junit.Assert.*;

public class FooTest extends TestCase {

    //make it JUnit 3 compatible. Only needed when Ant called from Java,
    //as from standalone command line it was fine
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(FooTest.class);
    }

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