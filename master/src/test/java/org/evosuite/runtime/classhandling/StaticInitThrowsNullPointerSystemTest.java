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
package org.evosuite.runtime.classhandling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.Test;

import com.examples.with.different.packagename.reset.StaticInitThrowsNullPointer;

public class StaticInitThrowsNullPointerSystemTest extends SystemTestBase {

	/*
	 * These tests are based on issues found on project 44_summa, which is using the lucene API.
	 * those have issues when for example classes uses org.apache.lucene.util.Constants which has:
	 * 
	  try {
	    Collections.class.getMethod("emptySortedSet");
	  } catch (NoSuchMethodException nsme) {
	    v8 = false;
	  }
	  *
	  * in its static initializer
	 */

    @Test
    public void testWithNoReset() {
        Properties.RESET_STATIC_FIELDS = false;

        EvoSuite evosuite = new EvoSuite();

        String targetClass = StaticInitThrowsNullPointer.class
                .getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(0, list.size());
    }

    @Test
    public void testWithReset() {
        Properties.RESET_STATIC_FIELDS = true;

        EvoSuite evosuite = new EvoSuite();

        String targetClass = StaticInitThrowsNullPointer.class
                .getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(0, list.size());
    }
}
