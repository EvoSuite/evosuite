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