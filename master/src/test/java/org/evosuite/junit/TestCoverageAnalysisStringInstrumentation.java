/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.junit;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.statistics.RuntimeVariable;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ClassNumberUtils;
import com.examples.with.different.packagename.ClassNumberUtilsTest;

public class TestCoverageAnalysisStringInstrumentation {

	@Test
	public void testCreateBigInteger() throws IOException {

		EvoSuite evosuite = new EvoSuite();

        String targetClass = ClassNumberUtils.class.getCanonicalName();
        String testClass = ClassNumberUtilsTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Properties.Criterion[] {
        	Properties.Criterion.LINE
        };

        Properties.OUTPUT_VARIABLES = RuntimeVariable.Total_Goals + "," + RuntimeVariable.LineCoverage;
        Properties.STATISTICS_BACKEND = StatisticsBackend.CSV;
        Properties.COVERAGE_MATRIX = true;

        String[] command = new String[] {
            "-class", targetClass,
            "-Djunit=" + testClass,
            "-measureCoverage"
        };

        Object statistics = evosuite.parseCommandLine(command);
        Assert.assertNotNull(statistics);

        fail("* Failure: String index out of range: 3"
        		+ "   java.lang.String.substring(String.java:1963)"
        		+ "   org.evosuite.instrumentation.testability.StringHelper.StringStartsWith(StringHelper.java:324)");
	}
}
