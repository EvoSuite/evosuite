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
package org.evosuite.testcarver;

import com.examples.with.different.packagename.testcarver.joda.*;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.SearchStatistics;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by jmr on 10/11/2015.
 */
public class CarverSystemTest extends SystemTestBase {

	// There doesn't seem to be an easy solution to this so let's ignore it for now
	@Ignore
	@Test
	public void testPrivateStaticField() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Days.class.getCanonicalName();
		// Test suite with private static field.
		String testClass = TestDays.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT_TESTS = true;

		Properties.CRITERION = new Properties.Criterion[] {
				Properties.Criterion.BRANCH,
				Properties.Criterion.METHOD
		};

		String[] command = new String[] {
				"-class", targetClass,
				"-Djunit=" + testClass,
				"-Dselected_junit=" + testClass,
				"-measureCoverage"
		};

		SearchStatistics result = (SearchStatistics)evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);
		OutputVariable coverage = (OutputVariable)result.getOutputVariables().get("Coverage");
		Assert.assertEquals("Non-optimal coverage", 1d, (double)coverage.getValue(), 0.01);
	}

	@Test
	public void testPublicStaticField() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Days.class.getCanonicalName();
		// Test suite with private static field.
		String testClass = TestDaysWithPublicField.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT_TESTS = true;

		Properties.CRITERION = new Properties.Criterion[] {
				Properties.Criterion.BRANCH,
				Properties.Criterion.METHOD
		};

		String[] command = new String[] {
				"-class", targetClass,
				"-Djunit=" + testClass,
				"-Dselected_junit=" + testClass,
				"-measureCoverage"
		};

		SearchStatistics result = (SearchStatistics)evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);
		OutputVariable coverage = (OutputVariable)result.getOutputVariables().get("Coverage");
		Assert.assertEquals("Non-optimal coverage", 1d, (double)coverage.getValue(), 0.01);
	}

	@Test
	public void testJodaTestScaledDurationField() {
		EvoSuite evosuite = new EvoSuite();

		// TestScaledDurationField.test_constructor causes NoSuchMethodException,
		// possibly related to a super() call in ScaledDurationField's constructor
		String targetClass = ScaledDurationField.class.getCanonicalName();
		String testClass = TestScaledDurationField.class.getCanonicalName();


		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT_TESTS = true;
		Properties.PRINT_GOALS = true;

		Properties.CRITERION = new Properties.Criterion[] {
				Properties.Criterion.BRANCH,
				Properties.Criterion.METHOD
		};
		Properties.GLOBAL_TIMEOUT = 600;
		String[] command = new String[] {
				"-class", targetClass,
				"-Djunit=" + testClass,
				"-Dselected_junit=" + testClass,
				"-measureCoverage"
		};

		SearchStatistics result = (SearchStatistics)evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);
		OutputVariable coverage = (OutputVariable)result.getOutputVariables().get("MethodCoverage");
		Assert.assertEquals("Non-optimal method coverage value", 1d, (double)coverage.getValue(), 0.01);
	}

	// An exception is thrown in the constructor. There is nothing that can be carved...
	@Ignore
	@Test
	public void testJodaTestScaledDurationFieldWithException() {
		EvoSuite evosuite = new EvoSuite();

		// TestScaledDurationField.test_constructor causes NoSuchMethodException,
		// possibly related to a super() call in ScaledDurationField's constructor
		String targetClass = ScaledDurationField.class.getCanonicalName();
		String testClass = TestScaledDurationFieldWithException.class.getCanonicalName();


		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT_TESTS = true;
		Properties.PRINT_GOALS = true;

		Properties.CRITERION = new Properties.Criterion[] {
				Properties.Criterion.BRANCH,
				Properties.Criterion.METHOD
		};
		Properties.GLOBAL_TIMEOUT = 600;
		String[] command = new String[] {
				"-class", targetClass,
				"-Djunit=" + testClass,
				"-Dselected_junit=" + testClass,
				"-measureCoverage"
		};

		SearchStatistics result = (SearchStatistics)evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);
		OutputVariable coverage = (OutputVariable)result.getOutputVariables().get("MethodCoverage");
		Assert.assertEquals("Non-optimal method coverage value", 1d, (double)coverage.getValue(), 0.01);
	}

}
