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
import com.examples.with.different.packagename.coverage.BOMInputStreamTest;
import com.examples.with.different.packagename.coverage.MethodWithSeveralInputArguments;
import com.examples.with.different.packagename.coverage.TestMethodWithSeveralInputArguments;
import com.examples.with.different.packagename.idnaming.BOMInputStream;
import com.examples.with.different.packagename.idnaming.naturalize.StringExample;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.junit.naming.methods.CoverageGoalTestNameGenerationStrategy;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.statistics.SearchStatistics;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcarver.testcase.CarvedTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.xsd.TestSuite;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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


	@Test
	public void testCarvedTestNames() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = MethodWithSeveralInputArguments.class.getCanonicalName();
		String testClass = TestMethodWithSeveralInputArguments.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.JUNIT = testClass;
		Properties.SELECTED_JUNIT = testClass;

		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.INPUT,
				Properties.Criterion.OUTPUT, Properties.Criterion.METHOD};

		String[] command = new String[] {
				"-class", targetClass,
				"-Djunit=" + testClass,
				"-Dselected_junit=" + testClass,
				"-measureCoverage"
		};

		SearchStatistics result = (SearchStatistics)evosuite.parseCommandLine(command);
		Assert.assertNotNull(result);

		JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(null);
		Assert.assertTrue(factory.hasCarvedTestCases());
		Assert.assertEquals("Incorrect number of carved tests", 2, factory.getNumCarvedTestCases());

		List<TestCase> tests = factory.getCarvedTestCases();

		Assert.assertEquals("Unexpected number of covered goals", 7, tests.get(0).getCoveredGoals().size());
		Assert.assertEquals("Unexpected number of covered goals", 7, tests.get(1).getCoveredGoals().size());

		String carvedName0 = TestSuiteWriterUtils.getNameOfTest(tests, 0);
		String carvedName1 = TestSuiteWriterUtils.getNameOfTest(tests, 1);
		Assert.assertEquals("Unexpected carved test name", "testWithNull", carvedName0);
		Assert.assertEquals("Unexpected carved test name", "testWithArray", carvedName1);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(tests);
		String generatedName0 = naming.getName(tests.get(0));
		String generatedName1 = naming.getName(tests.get(1));
		Assert.assertEquals("Unexpected generated test name", "testTestFooWithNull", generatedName0);
		Assert.assertEquals("Unexpected generated test name", "testTestFooWithNonEmptyArray", generatedName1);
	}

}
