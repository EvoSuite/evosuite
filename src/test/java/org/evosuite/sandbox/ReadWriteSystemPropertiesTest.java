package org.evosuite.sandbox;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.assertion.stable.TestStabilityChecker;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

import com.examples.with.different.packagename.DeleteFileCommonsIO;
import com.examples.with.different.packagename.sandbox.ReadLineSeparator;
import com.examples.with.different.packagename.sandbox.ReadWriteSystemProperties;

public class ReadWriteSystemPropertiesTest extends SystemTest {

	private static final String userDir = System
			.getProperty(ReadWriteSystemProperties.USER_DIR);
	private static final String aProperty = System
			.getProperty(ReadWriteSystemProperties.A_PROPERTY);

	private final boolean DEFAULT_REPLACE_CALLS = Properties.REPLACE_CALLS;

	@After
	public void reset() {
		Properties.REPLACE_CALLS = DEFAULT_REPLACE_CALLS;
	}

	@BeforeClass
	public static void checkStatus() {
		//such property shouldn't exist
		Assert.assertNull(aProperty);
	}

	@Test
	public void testReadLineSeparator() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ReadLineSeparator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double cov = best.getCoverage();
		Assert.assertEquals("Non-optimal coverage: ", 1d, cov, 0.001);

		//now check the JUnit generation
		List<TestCase> list = best.getTests();
		int n = list.size();
		Assert.assertTrue(n > 0);

		TestCaseExecutor.initExecutor(); //needed because it gets pulled down after the search

		try {
			Sandbox.initializeSecurityManagerForSUT();
			JUnitAnalyzer.removeTestsThatDoNotCompile(list);
		} finally {
			Sandbox.resetDefaultSecurityManager();
		}
		Assert.assertEquals(n, list.size());

		TestGenerationResult tgr = TestGenerationResultBuilder
				.buildSuccessResult();
		String code = tgr.getTestSuiteCode();
		Assert.assertTrue("Test code:\n" + code,
				code.contains("line.separator"));
		Assert.assertTrue("Test code:\n" + code, code.contains("debug"));
	}

	@Test
	public void testNoReplace() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ReadWriteSystemProperties.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.REPLACE_CALLS = false;

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double cov = best.getCoverage();
		//without replace calls, we shouldn't be able to achieve full coverage
		Assert.assertTrue(cov < 1d);
	}

	@Test
	public void testWithReplace() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ReadWriteSystemProperties.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double cov = best.getCoverage();
		Assert.assertEquals("Non-optimal coverage: ", 1d, cov, 0.001);

		//now check if properties have been reset to their initial state
		String currentUserDir = System
				.getProperty(ReadWriteSystemProperties.USER_DIR);
		String currentAProperty = System
				.getProperty(ReadWriteSystemProperties.A_PROPERTY);

		Assert.assertEquals(userDir, currentUserDir);
		Assert.assertEquals(aProperty, currentAProperty);

		//now check the JUnit generation
		List<TestCase> list = best.getTests();
		int n = list.size();
		Assert.assertTrue(n > 0);

		TestCaseExecutor.initExecutor(); //needed because it gets pulled down after the search

		try {
			Sandbox.initializeSecurityManagerForSUT();
			for (TestCase tc : list) {
				Assert.assertFalse(tc.isUnstable());
			}

			JUnitAnalyzer.removeTestsThatDoNotCompile(list);
			Assert.assertEquals(n, list.size());
			JUnitAnalyzer.handleTestsThatAreUnstable(list);
			Assert.assertEquals(n, list.size());

			for (TestCase tc : list) {
				Assert.assertFalse(tc.isUnstable());
			}

			Assert.assertEquals(userDir, currentUserDir);
			Assert.assertEquals(aProperty, currentAProperty);
		} catch (Throwable ex) {
			fail();
		} finally {
			Sandbox.resetDefaultSecurityManager();
		}
	}
}
