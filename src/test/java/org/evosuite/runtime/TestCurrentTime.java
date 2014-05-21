package org.evosuite.runtime;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.CurrentTime;
import com.examples.with.different.packagename.CurrentTimeViaCalendar;
import com.examples.with.different.packagename.CurrentTimeViaCalendar1;
import com.examples.with.different.packagename.CurrentTimeViaCalendar2;
import com.examples.with.different.packagename.CurrentTimeViaCalendar3;
import com.examples.with.different.packagename.CurrentTimeViaCalendarParameter;
import com.examples.with.different.packagename.CurrentTimeViaDate;
import com.examples.with.different.packagename.CurrentTimeViaDateParameter;
import com.examples.with.different.packagename.CurrentTimeViaGregorianCalendar;
import com.examples.with.different.packagename.CurrentTimeViaGregorianCalendarParameter;
import com.examples.with.different.packagename.TimeOperation;

public class TestCurrentTime extends SystemTest {

	private boolean replaceCalls = Properties.REPLACE_CALLS;

	@Before
	public void storeValues() {
		replaceCalls = Properties.REPLACE_CALLS;
	}

	@After
	public void resetValues() {
		Properties.REPLACE_CALLS = replaceCalls;
	}

	@Test
	public void testCurrentTime1() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTime.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testTimeOperation() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TimeOperation.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass }; //, "-assertions"

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testCurrentTimeViaCalendar() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTimeViaCalendar.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testCurrentTimeViaCalendar1() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTimeViaCalendar1.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testCurrentTimeViaCalendar2() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTimeViaCalendar2.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testCurrentTimeViaCalendar3() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTimeViaCalendar3.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testCurrentTimeViaCalendarParameter() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTimeViaCalendarParameter.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		java.lang.System.out.println("Test Suite: " + best.toString());

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	
	@Test
	public void testCurrentTimeViaGregorianCalendar() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTimeViaGregorianCalendar.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testCurrentTimeViaGregorianCalendarParameter() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTimeViaGregorianCalendarParameter.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		java.lang.System.out.println("Test Suite: " + best.toString());

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testCurrentTimeViaDate() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTimeViaDate.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testCurrentTimeViaDateParameter() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTimeViaDateParameter.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		java.lang.System.out.println("Test Suite: " + best.toString());

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
