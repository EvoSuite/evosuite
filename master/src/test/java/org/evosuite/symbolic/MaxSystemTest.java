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
package org.evosuite.symbolic;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Strategy;
import org.evosuite.SystemTestBase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.symbolic.Max;

public class MaxSystemTest extends SystemTestBase {


    @Before
    public void setUpProperties() {
        Properties.RESET_STATIC_FIELDS = true;
        Properties.RESET_STATIC_FIELD_GETS = true;
        Properties.SANDBOX = true;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.TRUE;
        Properties.JUNIT_TESTS = true;
        Properties.PURE_INSPECTORS = true;
        Properties.CLIENT_ON_THREAD = true;
    }

    @Test
    public void testGenerateUsingDSE() {

        Properties.STRATEGY = Strategy.DSE;

        Assume.assumeTrue(System.getenv("z3_path") != null);
        Properties.Z3_PATH = System.getenv("z3_path");

        EvoSuite evosuite = new EvoSuite();

        String targetClass = Max.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuiteUsingDSE", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        Assert.assertNotNull(result);
    }

}
