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
package org.evosuite.idnaming;

import com.examples.with.different.packagename.coverage.*;
import com.examples.with.different.packagename.coverage.MethodWithSeveralInputArguments;
import com.examples.with.different.packagename.coverage.TestMethodWithSeveralInputArguments;
import com.examples.with.different.packagename.idnaming.BOMInputStream;
import com.examples.with.different.packagename.strings.Calc;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.idNaming.TestNameGenerator;
import org.evosuite.statistics.SearchStatistics;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcarver.testcase.CarvedTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class TestIDNaming extends SystemTest {

/*	@Test
	public void testIDNamingOn() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Calc.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.TEST_NAMING = true;
		Properties.JUNIT_TESTS = true;

        Properties.CRITERION = new Properties.Criterion[5];
        Properties.CRITERION[0] = Properties.Criterion.METHOD;
        Properties.CRITERION[1] = Properties.Criterion.OUTPUT;
        Properties.CRITERION[2] = Properties.Criterion.INPUT;
        Properties.CRITERION[3] = Properties.Criterion.BRANCH;
        Properties.CRITERION[4] = Properties.Criterion.EXCEPTION;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		List<TestCase> tests = best.getTests();
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.145834);
	}*/

	@Test
	public void testCarvedTestNames() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = BOMInputStream.class.getCanonicalName();
		String testClass = BOMInputStreamTest.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT = testClass;
		Properties.SELECTED_JUNIT = testClass;

		Properties.TEST_NAMING = true;
		Properties.JUNIT_TESTS = true;
		Properties.WRITE_COVERED_GOALS_FILE = true;
		Properties.WRITE_TEST_NAMES_FILE = true;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.INPUT,
				Properties.Criterion.OUTPUT, Properties.Criterion.METHOD, Properties.Criterion.BRANCH, Properties.Criterion.EXCEPTION};

		String[] command = new String[] {
				"-class", targetClass,
				"-Djunit=" + testClass,
				"-Dselected_junit=" + testClass,
				"-measureCoverage"
		};

		SearchStatistics result = (SearchStatistics)evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);

		Map<TestCase,String> testNamesMap = TestNameGenerator.getResults();
		Assert.assertEquals("Incorrect number of carved tests", 2, testNamesMap.size());

		Object[] tests = testNamesMap.keySet().toArray();
		TestCase tc0 = (TestCase)tests[0];
		TestCase tc1 = (TestCase)tests[1];

		Assert.assertEquals("Unexpected number of covered goals", 7, tc0.getCoveredGoals().size());
		Assert.assertEquals("Unexpected number of covered goals", 7, tc1.getCoveredGoals().size());

		String carvedName0 = ((CarvedTestCase)tc0).getName();
		String generatedName0 = testNamesMap.get(tc0);

		String carvedName1 = ((CarvedTestCase)tc1).getName();
		String generatedName1 = testNamesMap.get(tc1);

		String[] carved = { carvedName0, carvedName1 };
		Arrays.sort(carved);
		String[] carvedExpected = {"testWithNull", "testWithArray"};
		Arrays.sort(carvedExpected);
		String[] generated = { generatedName0, generatedName1 };
		Arrays.sort(generated);
		String[] generatedExpected = {"testTestFooWithNonemptyInput", "testTestFooWithNullInput"};
		Arrays.sort(generatedExpected);

		Assert.assertArrayEquals("Unexpected carved test names", carvedExpected, carved);
		Assert.assertArrayEquals("Unexpected generated test names", generatedExpected, generated);
	}
}
