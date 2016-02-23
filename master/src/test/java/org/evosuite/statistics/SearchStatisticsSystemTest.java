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
package org.evosuite.statistics;

import java.util.Map;

import com.examples.with.different.packagename.Calculator;
import com.examples.with.different.packagename.ExampleGradientBranches;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.statistics.MultiThreads;
import com.examples.with.different.packagename.statistics.NoThreads;

public class SearchStatisticsSystemTest extends SystemTestBase {

	@Test
	public void testHandlingOfNoThreads(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = NoThreads.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.OUTPUT_VARIABLES=""+RuntimeVariable.Threads;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
		Assert.assertNotNull(map);
		OutputVariable<?> threads = map.get(RuntimeVariable.Threads.toString());
		Assert.assertNotNull(threads);
		Assert.assertEquals(1, threads.getValue());
	}

    @Ignore //ignored due to problems of JVM8 crashing on MacOS, and anyway we do not really need to check for threads any more
	@Test
	public void testHandlingOfMultiThreads(){
		EvoSuite evosuite = new EvoSuite();

		String targetClass = MultiThreads.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.OUTPUT_VARIABLES=""+RuntimeVariable.Threads;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);

		Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
		Assert.assertNotNull(map);
		OutputVariable<?> threads = map.get(RuntimeVariable.Threads.toString());
		Assert.assertNotNull(threads);
		Assert.assertEquals(3, threads.getValue());
		// TODO: This test currently fails because MSecurityManager does not set the number
		//       of threads correctly to avoid a JVM8 crash on MacOS: 
		// PermissionStatistics.getInstance().countThreads(Thread.currentThread().getThreadGroup().activeCount());
	}

    @Test
    public void testBranchlessMethodsOutputVariables(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = Calculator.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.DYNAMIC_SEEDING = true;
        Properties.SEARCH_BUDGET = 30;
        Properties.OUTPUT_VARIABLES = ""+RuntimeVariable.Branchless_Methods + "," + RuntimeVariable.Covered_Branchless_Methods;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        evosuite.parseCommandLine(command);

        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable<?> branchlessMethods = map.get(RuntimeVariable.Branchless_Methods.toString());
        OutputVariable<?> coveredBranchlessMethods = map.get(RuntimeVariable.Covered_Branchless_Methods.toString());
        Assert.assertNotNull(branchlessMethods);
        Assert.assertNotNull(coveredBranchlessMethods);
        Assert.assertEquals(5, branchlessMethods.getValue());
        Assert.assertEquals(5, coveredBranchlessMethods.getValue());
    }

    @Test
    public void testGradientBranchesOutputVariable(){
        EvoSuite evosuite = new EvoSuite();

        String targetClass = ExampleGradientBranches.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.DYNAMIC_SEEDING = true;
        Properties.SEARCH_BUDGET = 2500;
        Properties.TRACK_BOOLEAN_BRANCHES = true;
        Properties.OUTPUT_VARIABLES = ""+RuntimeVariable.Coverage+","+RuntimeVariable.Gradient_Branches;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };
        evosuite.parseCommandLine(command);

        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable<?> coverage = map.get(RuntimeVariable.Coverage.toString());
        Assert.assertNotNull(coverage);
        Assert.assertEquals(1.0, coverage.getValue());
        OutputVariable<?> gradientBranches = map.get(RuntimeVariable.Gradient_Branches.toString());
        Assert.assertNotNull(gradientBranches);
        Assert.assertEquals(4, gradientBranches.getValue());
    }

    @SuppressWarnings("unused")
	private OutputVariable<?> getLastTimelineVariable(Map<String, OutputVariable<?>> map, String name) {
        OutputVariable<?> timelineVar = null;
        int max = -1;
        for (Map.Entry<String, OutputVariable<?>> e : map.entrySet()) {
            if (e.getKey().startsWith(name)) {
                int index = Integer.parseInt( (e.getKey().split("_T"))[1] );
                if (index > max) {
                    max = index;
                    timelineVar = e.getValue();
                }
            }
        }
        return timelineVar;
    }
}
