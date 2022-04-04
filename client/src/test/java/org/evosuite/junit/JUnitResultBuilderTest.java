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
package org.evosuite.junit;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.junit.Assert.*;

public class JUnitResultBuilderTest {


    @Test
    public void testTranslationFromJUnitRunner() {
        JUnitCore core = new JUnitCore();

        Class<?> fooTestClass = new FooTestClassLoader().loadFooTestClass();
        Result result = core.run(fooTestClass);
        JUnitResultBuilder builder = new JUnitResultBuilder();
        JUnitResult junitResult = builder.build(result);

        assertFalse(junitResult.wasSuccessful());
        assertEquals(1, junitResult.getFailureCount());
        assertEquals(1, junitResult.getFailures().size());

        JUnitFailure junitFailure = junitResult.getFailures().get(0);
        assertTrue(junitFailure.isAssertionError());
    }
}
