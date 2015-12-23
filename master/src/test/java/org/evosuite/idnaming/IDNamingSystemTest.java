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

import com.examples.with.different.packagename.Calculator;
import com.examples.with.different.packagename.coverage.*;
import com.examples.with.different.packagename.coverage.MethodWithSeveralInputArguments;
import com.examples.with.different.packagename.coverage.TestMethodWithSeveralInputArguments;
import com.examples.with.different.packagename.idnaming.BOMInputStream;
import com.examples.with.different.packagename.sette.L4_Collections;
import com.examples.with.different.packagename.sette.SnippetInputContainer;
import com.examples.with.different.packagename.strings.Calc;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageGoal;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.statistics.SearchStatistics;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcarver.testcase.CarvedTestCase;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;

public class IDNamingSystemTest extends SystemTestBase {

	@Test
	public void testIDNamingOn() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Calc.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.TEST_NAMING = true;
		Properties.JUNIT_TESTS = true;
		Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DUMMY;

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

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.145834);
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
		Properties.TEST_NAMING = true;
		Properties.JUNIT_TESTS = true;
		StringBuilder analysisCriteria = new StringBuilder();
		analysisCriteria.append(Properties.Criterion.METHOD); analysisCriteria.append(",");
		analysisCriteria.append(Properties.Criterion.OUTPUT); analysisCriteria.append(",");
		analysisCriteria.append(Properties.Criterion.INPUT); analysisCriteria.append(",");
		analysisCriteria.append(Properties.Criterion.BRANCH);
		Properties.ANALYSIS_CRITERIA = analysisCriteria.toString();

		Properties.CRITERION = new Properties.Criterion[4];
		Properties.CRITERION[0] = Properties.Criterion.METHOD;
		Properties.CRITERION[1] = Properties.Criterion.OUTPUT;
		Properties.CRITERION[2] = Properties.Criterion.BRANCH;
		Properties.CRITERION[3] = Properties.Criterion.INPUT;

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
		Properties.TEST_NAMING = true;
		Properties.JUNIT_TESTS = true;
		StringBuilder analysisCriteria = new StringBuilder();
		analysisCriteria.append(Properties.Criterion.METHOD); analysisCriteria.append(",");
		analysisCriteria.append(Properties.Criterion.OUTPUT); analysisCriteria.append(",");
		analysisCriteria.append(Properties.Criterion.BRANCH);
		Properties.ANALYSIS_CRITERIA = analysisCriteria.toString();

		Properties.CRITERION = new Properties.Criterion[3];
		Properties.CRITERION[0] = Properties.Criterion.METHOD;
		Properties.CRITERION[1] = Properties.Criterion.OUTPUT;
		Properties.CRITERION[2] = Properties.Criterion.BRANCH;

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
		Properties.TEST_NAMING = true;
		Properties.JUNIT_TESTS = true;
		StringBuilder analysisCriteria = new StringBuilder();
		analysisCriteria.append(Properties.Criterion.METHODTRACE); analysisCriteria.append(",");
		//  analysisCriteria.append(Properties.Criterion.METHOD); analysisCriteria.append(",");
		//    analysisCriteria.append(Properties.Criterion.OUTPUT); analysisCriteria.append(",");
		Properties.ANALYSIS_CRITERIA = analysisCriteria.toString();

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
