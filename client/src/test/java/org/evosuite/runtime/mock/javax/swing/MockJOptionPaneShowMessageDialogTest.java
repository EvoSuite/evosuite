package org.evosuite.runtime.mock.javax.swing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.Properties.Criterion;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.mock.javax.swing.ShowMessageDialogExample;

public class MockJOptionPaneShowMessageDialogTest {

	private static final boolean DEFAULT_MOCK_GUI = RuntimeSettings.mockGUI;
	private static final boolean DEFAULT_REPLACE_GUI = Properties.REPLACE_GUI;

	@BeforeClass
	public static void init() {
		String cp = System.getProperty("user.dir") + "/target/test-classes";
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
	}

	@Before
	public void setUp() {
		Properties.CRITERION = new Properties.Criterion[] { Criterion.BRANCH };
		Properties.TARGET_CLASS = ShowMessageDialogExample.class.getCanonicalName();
		Properties.REPLACE_GUI = true;
		RuntimeSettings.mockGUI = true;
		TestGenerationContext.getInstance().resetContext();
	}

	@After
	public void tearDown() {
		RuntimeSettings.mockGUI = DEFAULT_MOCK_GUI;
		Properties.REPLACE_GUI = DEFAULT_REPLACE_GUI;
		TestGenerationContext.getInstance().resetContext();
	}

	@Test
	public void testShowMessageDialog0() throws Exception {

		TestSuiteChromosome suite = new TestSuiteChromosome();
		InstrumentingClassLoader cl = new InstrumentingClassLoader();
		TestCase t0 = buildTestCase0TrueBranch(cl);
		TestCase t1 = buildTestCase0FalseBranch(cl);
		suite.addTest(t0);
		suite.addTest(t1);

		BranchCoverageSuiteFitness ff = new BranchCoverageSuiteFitness(cl);
		ff.getFitness(suite);

		Set<TestFitnessFunction> coveredGoals = suite.getCoveredGoals();
		Assert.assertEquals(3, coveredGoals.size());
	}
	
	@Test
	public void testShowMessageDialog1() throws Exception {

		TestSuiteChromosome suite = new TestSuiteChromosome();
		InstrumentingClassLoader cl = new InstrumentingClassLoader();
		TestCase t0 = buildTestCase1TrueBranch(cl);
		TestCase t1 = buildTestCase1FalseBranch(cl);
		suite.addTest(t0);
		suite.addTest(t1);

		BranchCoverageSuiteFitness ff = new BranchCoverageSuiteFitness(cl);
		ff.getFitness(suite);

		Set<TestFitnessFunction> coveredGoals = suite.getCoveredGoals();
		Assert.assertEquals(3, coveredGoals.size());
	}
	
	@Test
	public void testShowMessageDialog2() throws Exception {

		TestSuiteChromosome suite = new TestSuiteChromosome();
		InstrumentingClassLoader cl = new InstrumentingClassLoader();
		TestCase t0 = buildTestCase2TrueBranch(cl);
		TestCase t1 = buildTestCase2FalseBranch(cl);
		suite.addTest(t0);
		suite.addTest(t1);

		BranchCoverageSuiteFitness ff = new BranchCoverageSuiteFitness(cl);
		ff.getFitness(suite);

		Set<TestFitnessFunction> coveredGoals = suite.getCoveredGoals();
		Assert.assertEquals(3, coveredGoals.size());
	}

	private static TestCase buildTestCase0TrueBranch(InstrumentingClassLoader cl)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		TestCaseBuilder builder = new TestCaseBuilder();

		Class<?> clazz = cl.loadClass(ShowMessageDialogExample.class.getCanonicalName());
		Constructor<?> constructor = clazz.getConstructor();
		VariableReference showMessageDialogExample0 = builder.appendConstructor(constructor);

		VariableReference int0 = builder.appendIntPrimitive(0);
		Method showMessageDialogMethod = clazz.getMethod("showMessageDialog0", int.class);
		builder.appendMethod(showMessageDialogExample0, showMessageDialogMethod, int0);

		return builder.getDefaultTestCase();
	}

	private static TestCase buildTestCase0FalseBranch(InstrumentingClassLoader cl)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		TestCaseBuilder builder = new TestCaseBuilder();

		Class<?> clazz = cl.loadClass(ShowMessageDialogExample.class.getCanonicalName());
		Constructor<?> constructor = clazz.getConstructor();
		VariableReference showMessageDialogExample0 = builder.appendConstructor(constructor);

		VariableReference int0 = builder.appendIntPrimitive(1);
		Method showMessageDialogMethod = clazz.getMethod("showMessageDialog0", int.class);
		builder.appendMethod(showMessageDialogExample0, showMessageDialogMethod, int0);

		return builder.getDefaultTestCase();
	}
	
	private static TestCase buildTestCase1TrueBranch(InstrumentingClassLoader cl)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		TestCaseBuilder builder = new TestCaseBuilder();

		Class<?> clazz = cl.loadClass(ShowMessageDialogExample.class.getCanonicalName());
		Constructor<?> constructor = clazz.getConstructor();
		VariableReference showMessageDialogExample0 = builder.appendConstructor(constructor);

		VariableReference int0 = builder.appendIntPrimitive(0);
		Method showMessageDialogMethod = clazz.getMethod("showMessageDialog1", int.class);
		builder.appendMethod(showMessageDialogExample0, showMessageDialogMethod, int0);

		return builder.getDefaultTestCase();
	}

	private static TestCase buildTestCase1FalseBranch(InstrumentingClassLoader cl)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		TestCaseBuilder builder = new TestCaseBuilder();

		Class<?> clazz = cl.loadClass(ShowMessageDialogExample.class.getCanonicalName());
		Constructor<?> constructor = clazz.getConstructor();
		VariableReference showMessageDialogExample0 = builder.appendConstructor(constructor);

		VariableReference int0 = builder.appendIntPrimitive(1);
		Method showMessageDialogMethod = clazz.getMethod("showMessageDialog1", int.class);
		builder.appendMethod(showMessageDialogExample0, showMessageDialogMethod, int0);

		return builder.getDefaultTestCase();
	}
	
	
	private static TestCase buildTestCase2TrueBranch(InstrumentingClassLoader cl)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		TestCaseBuilder builder = new TestCaseBuilder();

		Class<?> clazz = cl.loadClass(ShowMessageDialogExample.class.getCanonicalName());
		Constructor<?> constructor = clazz.getConstructor();
		VariableReference showMessageDialogExample0 = builder.appendConstructor(constructor);

		VariableReference int0 = builder.appendIntPrimitive(0);
		Method showMessageDialogMethod = clazz.getMethod("showMessageDialog2", int.class);
		builder.appendMethod(showMessageDialogExample0, showMessageDialogMethod, int0);

		return builder.getDefaultTestCase();
	}

	private static TestCase buildTestCase2FalseBranch(InstrumentingClassLoader cl)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		TestCaseBuilder builder = new TestCaseBuilder();

		Class<?> clazz = cl.loadClass(ShowMessageDialogExample.class.getCanonicalName());
		Constructor<?> constructor = clazz.getConstructor();
		VariableReference showMessageDialogExample0 = builder.appendConstructor(constructor);

		VariableReference int0 = builder.appendIntPrimitive(1);
		Method showMessageDialogMethod = clazz.getMethod("showMessageDialog2", int.class);
		builder.appendMethod(showMessageDialogExample0, showMessageDialogMethod, int0);

		return builder.getDefaultTestCase();
	}
	

}
