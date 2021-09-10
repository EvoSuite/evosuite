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
package org.evosuite.runtime;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.CallExit;

import java.lang.*;
import java.security.Permission;

import static org.junit.Assert.assertFalse;

public class SUTWithSystemExitSystemTest extends SystemTestBase {

    @Before
    public void setFlag() {
        SafeExit.calledExit = false;
    }

    @Test
    public void testSystemExit_noAssertions() {
        Properties.ASSERTIONS = false;
        testSystemExit();
    }

    @Test
    public void testSystemExit() {

        java.lang.System.setSecurityManager(new SafeExit());

        EvoSuite evosuite = new EvoSuite();

        String targetClass = CallExit.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.REPLACE_CALLS = true;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        evosuite.parseCommandLine(command);

        assertFalse(SafeExit.calledExit);
    }

    @After
    public void removeSecurity() {
        java.lang.System.setSecurityManager(null);
    }

    private static class SafeExit extends SecurityManager {

        public static boolean calledExit = false;

        public void checkPermission(Permission perm) throws SecurityException {

            final String name = perm.getName().trim();
            if (name.startsWith("exitVM")) {
                calledExit = true;
                throw new RuntimeException("CALLED EXIT");
            }
        }
    }

}
