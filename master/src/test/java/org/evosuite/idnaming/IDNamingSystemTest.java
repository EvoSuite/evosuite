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
package org.evosuite.idnaming;

import com.examples.with.different.packagename.Calculator;
import com.examples.with.different.packagename.sette.L4_Collections;
import com.examples.with.different.packagename.sette.SnippetInputContainer;
import com.examples.with.different.packagename.idnaming.naturalize.StringExample;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class IDNamingSystemTest extends SystemTestBase {

	@Test
	public void testStringExampleDummyStrategy() throws IOException {

		Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DUMMY;

		testStringExample("var0", "var1");

	}

	@Test
	public void testStringExampleDefaultStrategy() throws IOException {

		Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DEFAULT;

		testStringExample("stringExample", "boolean0");

	}

	@Test
	public void testStringExampleDeclarationsStrategy() throws IOException {

		Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DECLARATIONS;

		testStringExample("stringExample", "boolean0");

	}

	@Test
	public void testStringExampleExplanatoryStrategy() throws IOException {

		Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.EXPLANATORY;

		testStringExample("invokesFoo", "resultFromFoo");

	}

	@Test
	public void testStringExampleNaturalizeStrategy() throws IOException {

		Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.NATURALIZE;
		Properties.VARIABLE_NAMING_TRAINING_DATA_DIR = "src/test/java/com/examples/with/different/packagename/idnaming/naturalize";

		testStringExample("sex", "foo");

	}

	public void testStringExample(String varName0, String varName1) throws IOException {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = StringExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		String name = targetClass.substring(targetClass.lastIndexOf(".") + 1) + Properties.JUNIT_SUFFIX;

		// run EvoSuite
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		// write test suite
		TestCaseExecutor.initExecutor(); // TODO: why is this needed?
		TestSuiteWriter writer = new TestSuiteWriter();
		writer.insertAllTests(best.getTests());
		writer.writeTestSuite(name, Properties.TEST_DIR, Collections.EMPTY_LIST);

		// check that the test suite was created
		String junitFile = Properties.TEST_DIR + File.separatorChar +
				Properties.CLASS_PREFIX.replace('.', File.separatorChar) + File.separatorChar +
				name + ".java";
		Path path = Paths.get(junitFile);
		Assert.assertTrue("Test Suite does not exist", Files.exists(path));

		String testCode = new String(Files.readAllBytes(path));

		// check variable names, test suite should contain two tests
		Assert.assertEquals("Unexpected variable names in:\n" + testCode, 2, StringUtils.countMatches(testCode, "StringExample " + varName0 + " = new StringExample();"));
		Assert.assertEquals("Unexpected variable names in:\n" + testCode, 2, StringUtils.countMatches(testCode, "boolean " + varName1 + " = " + varName0 + ".foo"));

	}


//	@Test
//	public void testCarvedTestNames() {
//
//		EvoSuite evosuite = new EvoSuite();
//
//		String targetClass = MethodWithSeveralInputArguments.class.getCanonicalName();
//		String testClass = TestMethodWithSeveralInputArguments.class.getCanonicalName();
//
//		Properties.TARGET_CLASS = targetClass;
//		Properties.JUNIT = testClass;
//		Properties.SELECTED_JUNIT = testClass;
//
//		Properties.TEST_NAMING = true;
//		Properties.JUNIT_TESTS = true;
//		Properties.WRITE_COVERED_GOALS_FILE = true;
//		Properties.WRITE_TEST_NAMES_FILE = true;
//
//		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.INPUT,
//				Properties.Criterion.OUTPUT, Properties.Criterion.METHOD};
//
//		String[] command = new String[] {
//				"-class", targetClass,
//				"-Djunit=" + testClass,
//				"-Dselected_junit=" + testClass,
//				"-measureCoverage"
//		};
//
//		SearchStatistics result = (SearchStatistics)evosuite.parseCommandLine(command);
//		Assert.assertNotNull(result);
//
//		Map<TestCase,String> testNamesMap = TestNameGenerator.getResults();
//		Assert.assertEquals("Incorrect number of carved tests", 2, testNamesMap.size());
//
//		Object[] tests = testNamesMap.keySet().toArray();
//		TestCase tc0 = (TestCase)tests[0];
//		TestCase tc1 = (TestCase)tests[1];
//
//		Assert.assertEquals("Unexpected number of covered goals", 7, tc0.getCoveredGoals().size());
//		Assert.assertEquals("Unexpected number of covered goals", 7, tc1.getCoveredGoals().size());
//
//		String carvedName0 = ((CarvedTestCase)tc0).getName();
//		String generatedName0 = testNamesMap.get(tc0);
//
//		String carvedName1 = ((CarvedTestCase)tc1).getName();
//		String generatedName1 = testNamesMap.get(tc1);
//
//		String[] carved = { carvedName0, carvedName1 };
//		Arrays.sort(carved);
//		String[] carvedExpected = {"testWithNull", "testWithArray"};
//		Arrays.sort(carvedExpected);
//		String[] generated = { generatedName0, generatedName1 };
//		Arrays.sort(generated);
//		String[] generatedExpected = {"testTestFooWithNonemptyInput", "testTestFooWithNullInput"};
//		Arrays.sort(generatedExpected);
//
//		Assert.assertArrayEquals("Unexpected carved test names", carvedExpected, carved);
//		Assert.assertArrayEquals("Unexpected generated test names", generatedExpected, generated);
//	}

//	@Test
//	public void testCarvedTestNamesBOMInputStream() {
//
//		EvoSuite evosuite = new EvoSuite();
//
//		String targetClass = BOMInputStream.class.getCanonicalName();
//		String testClass = BOMInputStreamTest.class.getCanonicalName();
//
//		Properties.TARGET_CLASS = targetClass;
//		Properties.JUNIT = testClass;
//		Properties.SELECTED_JUNIT = testClass;
//
//		Properties.TEST_NAMING = true;
//		Properties.JUNIT_TESTS = true;
//		Properties.WRITE_COVERED_GOALS_FILE = true;
//		Properties.WRITE_TEST_NAMES_FILE = true;
//
//		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.INPUT,
//				Properties.Criterion.OUTPUT, Properties.Criterion.METHOD, Properties.Criterion.BRANCH, Properties.Criterion.EXCEPTION};
//
//		String[] command = new String[] {
//				"-class", targetClass,
//				"-Djunit=" + testClass,
//				"-Dselected_junit=" + testClass,
//				"-measureCoverage"
//		};
//
//		SearchStatistics result = (SearchStatistics)evosuite.parseCommandLine(command);
//		Assert.assertNotNull(result);
//
//		Map<TestCase,String> testNamesMap = TestNameGenerator.getResults();
//		Assert.assertEquals("Incorrect number of carved tests", 2, testNamesMap.size());
//
//		Object[] tests = testNamesMap.keySet().toArray();
//		TestCase tc0 = (TestCase)tests[0];
//		TestCase tc1 = (TestCase)tests[1];
//
//		Assert.assertEquals("Unexpected number of covered goals", 7, tc0.getCoveredGoals().size());
//		Assert.assertEquals("Unexpected number of covered goals", 7, tc1.getCoveredGoals().size());
//
//		String carvedName0 = ((CarvedTestCase)tc0).getName();
//		String generatedName0 = testNamesMap.get(tc0);
//
//		String carvedName1 = ((CarvedTestCase)tc1).getName();
//		String generatedName1 = testNamesMap.get(tc1);
//
//		String[] carved = { carvedName0, carvedName1 };
//		Arrays.sort(carved);
//		String[] carvedExpected = {"testWithNull", "testWithArray"};
//		Arrays.sort(carvedExpected);
//		String[] generated = { generatedName0, generatedName1 };
//		Arrays.sort(generated);
//		String[] generatedExpected = {"testTestFooWithNonemptyInput", "testTestFooWithNullInput"};
//		Arrays.sort(generatedExpected);
//
//		Assert.assertArrayEquals("Unexpected carved test names", carvedExpected, carved);
//		Assert.assertArrayEquals("Unexpected generated test names", generatedExpected, generated);
//	}

	@Test
	public void testIdWithSameMethod() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Calculator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT_TESTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function

		Assert.assertEquals("Wrong number of goals: ", 5, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testIdWithSameOutput() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = L4_Collections.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT_TESTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		//	System.out.println(best.getCoverage()+"-"+1d);
		Assert.assertEquals("Wrong number of goals: ", 11, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.443);
	}

	@Test
	public void testMethodTrace() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = SnippetInputContainer.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT_TESTS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 15, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}


}
