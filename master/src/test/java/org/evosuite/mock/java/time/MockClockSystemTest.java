/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.mock.java.time;

import com.examples.with.different.packagename.mock.java.lang.MemorySum;
import com.examples.with.different.packagename.mock.java.net.ReadFromURL;
import com.examples.with.different.packagename.mock.java.time.ClockExample;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.EvoClassLoader;
import org.evosuite.runtime.instrumentation.MethodCallReplacementCache;
import org.evosuite.runtime.testdata.EvoSuiteURL;
import org.evosuite.runtime.testdata.NetworkHandling;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created by gordon on 25/01/2016.
 */
public class MockClockSystemTest extends SystemTestBase {

    @Test
    public void testClock() throws Exception{
        String targetClass = ClockExample.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.JUNIT_TESTS = true;
        Properties.JUNIT_CHECK = true;
        Properties.REPLACE_CALLS = true;
        Properties.OUTPUT_VARIABLES=""+ RuntimeVariable.HadUnstableTests;

        EvoSuite evosuite = new EvoSuite();
        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

        Assert.assertNotNull(best);
        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);

        checkUnstable();
    }

}
