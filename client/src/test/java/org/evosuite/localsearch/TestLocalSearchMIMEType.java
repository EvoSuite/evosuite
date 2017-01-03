package org.evosuite.localsearch;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.SolverType;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.ga.localsearch.DefaultLocalSearchObjective;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.reset.ClassReInitializer;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.MIMEType;

public class TestLocalSearchMIMEType {

	private final static boolean DEFAULT_IS_TRACE_ENABLED = ExecutionTracer.isTraceCallsEnabled();
	private java.util.Properties currentProperties;

	@Before
	public void setUp() {
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();

		Properties.getInstance().resetToDefaults();

		Randomness.setSeed(42);
		Properties.TARGET_CLASS = "";

		TestGenerationContext.getInstance().resetContext();
		ClassReInitializer.resetSingleton();

		Randomness.setSeed(42);

		currentProperties = (java.util.Properties) System.getProperties().clone();

		Properties.CRITERION = new Criterion[] { Criterion.LINE, Criterion.BRANCH, Criterion.EXCEPTION,
				Criterion.WEAKMUTATION, Criterion.OUTPUT, Criterion.METHOD, Criterion.METHODNOEXCEPTION,
				Criterion.CBRANCH };
		ExecutionTracer.enableTraceCalls();

	}

	@After
	public void tearDown() {
		if (DEFAULT_IS_TRACE_ENABLED) {
			ExecutionTracer.enableTraceCalls();
		} else {
			ExecutionTracer.disableTraceCalls();
		}
		TestGenerationContext.getInstance().resetContext();
		ClassReInitializer.resetSingleton();

		System.setProperties(currentProperties);
		Properties.getInstance().resetToDefaults();
	}

	private DefaultTestCase createTestCase0()
			throws NoSuchFieldException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		TestCaseBuilder builder = new TestCaseBuilder();

		final Class<?> mimeTypeClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MIMEType.class.getName());

		final Field memField = mimeTypeClass.getField("MEM");
		final Method toString = mimeTypeClass.getMethod("toString");
		VariableReference mIMEType0 = builder.appendStaticFieldStmt(memField);
		builder.appendMethod(mIMEType0, toString);
		
		System.out.println("Test Case #0=" + builder.toCode());

		return builder.getDefaultTestCase();
	}

	private DefaultTestCase createTestCase2()
			throws NoSuchFieldException, SecurityException, NoSuchMethodException, ClassNotFoundException {

		final Class<?> mimeTypeClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MIMEType.class.getName());
		final Field rdfField = mimeTypeClass.getDeclaredField("RDF");
		final Method hashCode = mimeTypeClass.getMethod("hashCode");
		final Field slashField = mimeTypeClass.getDeclaredField("slash");
		final Method getTypeMethod = mimeTypeClass.getMethod("getType");
		final Method getSubTypeMethod = mimeTypeClass.getMethod("getSubType");
		final Method equalsMethod = mimeTypeClass.getMethod("equals", Object.class);
		final Field mimeTypeField = mimeTypeClass.getDeclaredField("mimeType");
		final Constructor<?> constructorStringBoolean = mimeTypeClass.getConstructor(String.class, boolean.class);

		final TestCaseBuilder builder = new TestCaseBuilder();
		VariableReference mIMEType0 = builder.appendStaticFieldStmt(rdfField);
		VariableReference string0 = builder.appendStringPrimitive("");
		VariableReference int0 = builder.appendMethod(mIMEType0, hashCode);
		VariableReference string1 = builder.appendStringPrimitive("");
		VariableReference int1 = builder.appendIntPrimitive(0);
		VariableReference string2 = builder
				.appendStringPrimitive("com.examples.with.different.packagename.concolic.MalformedMIMETypeException");
		builder.appendAssignment(mIMEType0, slashField, int1);
		VariableReference int2 = builder.appendFieldStmt(mIMEType0, slashField);
		VariableReference string3 = builder.appendMethod(mIMEType0, getTypeMethod);
		VariableReference string4 = builder.appendMethod(mIMEType0, getSubTypeMethod);
		VariableReference boolean0 = builder.appendMethod(mIMEType0, equalsMethod, string3);
		VariableReference string5 = builder.appendFieldStmt(mIMEType0, mimeTypeField);
		VariableReference string6 = builder.appendMethod(mIMEType0, getSubTypeMethod);
		VariableReference int3 = builder.appendFieldStmt(mIMEType0, slashField);
		VariableReference string7 = builder.appendStringPrimitive("xT7vo\"<|[E{4");
		builder.appendConstructor(constructorStringBoolean, string7, boolean0);
		builder.addException(new Error());
		System.out.println("Test Case #2=" + builder.toCode());
		return builder.getDefaultTestCase();
	}

	private DefaultTestCase createTestCase1()
			throws NoSuchFieldException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		final TestCaseBuilder builder = new TestCaseBuilder();

		final Class<?> mimeTypeClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MIMEType.class.getName());
		final Class<?> objectClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(Object.class.getName());

		final Constructor<?> constructorStringBoolean = mimeTypeClass.getConstructor(String.class, boolean.class);
		final Constructor<?> constructorString = mimeTypeClass.getConstructor(String.class);
		final Field mimeTypeField = mimeTypeClass.getDeclaredField("mimeType");
		final Field slashField = mimeTypeClass.getDeclaredField("slash");
		final Method getSubTypeMethod = mimeTypeClass.getMethod("getSubType");
		final Method equalsMethod = mimeTypeClass.getMethod("equals", Object.class);
		final Method getTypeMethod = mimeTypeClass.getMethod("getType");

		VariableReference string0 = builder.appendStringPrimitive("Y.8p>:/]WybaL");
		VariableReference boolean0 = builder.appendBooleanPrimitive(false);
		VariableReference mIMEType0 = builder.appendConstructor(constructorStringBoolean, string0, boolean0);
		VariableReference int0 = builder.appendIntPrimitive(-1);
		VariableReference int1 = builder.appendIntPrimitive(-1);
		VariableReference int2 = builder.appendIntPrimitive(1);
		builder.appendAssignment(mIMEType0, slashField, int2);
		builder.appendAssignment(mIMEType0, slashField, int0);
		builder.appendAssignment(mIMEType0, slashField, int1);
		VariableReference int3 = builder.appendIntPrimitive(0);
		VariableReference string1 = builder.appendStringPrimitive("Jm");
		builder.appendAssignment(mIMEType0, mimeTypeField, string1);
		VariableReference int4 = builder.appendIntPrimitive(2556);
		VariableReference int5 = builder.appendIntPrimitive(2556);
		builder.appendAssignment(mIMEType0, slashField, int4);
		builder.appendAssignment(mIMEType0, slashField, int3);
		VariableReference string2 = builder.appendMethod(mIMEType0, getSubTypeMethod);
		VariableReference object0 = builder.appendNull(objectClass);
		VariableReference boolean1 = builder.appendMethod(mIMEType0, equalsMethod, object0);
		VariableReference object1 = builder.appendNull(objectClass);
		VariableReference boolean2 = builder.appendMethod(mIMEType0, equalsMethod, object1);
		VariableReference string3 = builder.appendStringPrimitive("");
		VariableReference mIMEType1 = builder.appendConstructor(constructorString, string3);
		builder.addException(new Exception());
		builder.appendAssignment(mIMEType1, slashField, int5);
		VariableReference string4 = builder.appendStringPrimitive("DI'XL>AQzq1");
		builder.appendAssignment(mIMEType1, mimeTypeField, string4);
		VariableReference string5 = builder.appendFieldStmt(mIMEType1, mimeTypeField);
		VariableReference string6 = builder.appendStringPrimitive("bjvXpt%");
		VariableReference boolean3 = builder.appendBooleanPrimitive(true);
		VariableReference mIMEType2 = builder.appendConstructor(constructorStringBoolean, string6, boolean3);
		builder.appendAssignment(mIMEType2, slashField, mIMEType0, slashField);

		VariableReference string7 = builder.appendMethod(mIMEType0, getTypeMethod);
		VariableReference mIMEType3 = builder.appendConstructor(constructorStringBoolean, string5, boolean1);
		builder.appendAssignment(mIMEType1, mimeTypeField, string7);
		VariableReference string8 = builder.appendStringPrimitive("g");
		VariableReference mIMEType4 = builder.appendConstructor(constructorString, string8);
		VariableReference string9 = builder.appendMethod(mIMEType1, getTypeMethod);

		System.out.println("Test Case #1=" + builder.toCode());
		return builder.getDefaultTestCase();

	}

	@Test
	public void testFitness()
			throws NoSuchFieldException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		Properties.RESET_STATIC_FINAL_FIELDS = false;
		
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_BUDGET = 100;
		Properties.DSE_SOLVER = SolverType.EVOSUITE_SOLVER;
		Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		Properties.SEARCH_BUDGET = 120;
		Properties.TARGET_CLASS = MIMEType.class.getName();

		String classPath = ClassPathHandler.getInstance().getTargetProjectClasspath();
		DependencyAnalysis.analyzeClass(MIMEType.class.getName(), Arrays.asList(classPath));

		TestSuiteChromosome suite = new TestSuiteChromosome();
		DefaultTestCase test0 = createTestCase0();
		DefaultTestCase test1 = createTestCase1();
		DefaultTestCase test2 = createTestCase2();
		DefaultTestCase test3 = createTestCase3();
		DefaultTestCase test4 = createTestCase4();
		DefaultTestCase test5 = createTestCase5();
		suite.addTest(test0);
		suite.addTest(test1);
		suite.addTest(test2);
		suite.addTest(test3);
		suite.addTest(test4);
		suite.addTest(test5);

		TestSuiteFitnessFunction lineCoverage = FitnessFunctions.getFitnessFunction(Criterion.LINE);
		TestSuiteFitnessFunction branchCoverage = FitnessFunctions.getFitnessFunction(Criterion.BRANCH);
		TestSuiteFitnessFunction exceptionCoverage = FitnessFunctions.getFitnessFunction(Criterion.EXCEPTION);
		TestSuiteFitnessFunction weakMutationCoverage = FitnessFunctions.getFitnessFunction(Criterion.WEAKMUTATION);
		TestSuiteFitnessFunction outputCoverage = FitnessFunctions.getFitnessFunction(Criterion.OUTPUT);
		TestSuiteFitnessFunction methodCoverage = FitnessFunctions.getFitnessFunction(Criterion.METHOD);
		TestSuiteFitnessFunction methodNoExceptionCoverage = FitnessFunctions
				.getFitnessFunction(Criterion.METHODNOEXCEPTION);
		TestSuiteFitnessFunction cbranchCoverage = FitnessFunctions.getFitnessFunction(Criterion.CBRANCH);

		List<TestSuiteFitnessFunction> fitnessFunctions = new ArrayList<TestSuiteFitnessFunction>();
		fitnessFunctions.add(lineCoverage);
		fitnessFunctions.add(branchCoverage);
		fitnessFunctions.add(exceptionCoverage);
		fitnessFunctions.add(weakMutationCoverage);
		fitnessFunctions.add(outputCoverage);
		fitnessFunctions.add(methodCoverage);
		fitnessFunctions.add(methodNoExceptionCoverage);
		fitnessFunctions.add(cbranchCoverage);

		for (TestSuiteFitnessFunction ff : fitnessFunctions) {
			suite.addFitness(ff);
		}

		for (TestSuiteFitnessFunction ff : fitnessFunctions) {
			double oldFitness = ff.getFitness(suite);
			System.out.println(ff.toString() + "->" + oldFitness);
		}
		double oldFitness = suite.getFitness();
		System.out.println("oldFitness->" + oldFitness);
		System.out.println("oldSize->" + suite.getTests().size());

		DefaultLocalSearchObjective objective = new DefaultLocalSearchObjective<>();
		for (TestSuiteFitnessFunction ff : fitnessFunctions) {
			objective.addFitnessFunction(ff);
		}
		boolean hasImproved = suite.localSearch(objective);

		System.out.println("hasImproved=" + hasImproved);


		for (TestSuiteFitnessFunction ff : fitnessFunctions) {
			double newFitness = ff.getFitness(suite);
			System.out.println(ff.toString() + "->" + newFitness);

		}
		double newFitness = suite.getFitness();
		System.out.println("newFitness->" + newFitness);
		System.out.println("newSize->" + suite.getTests().size());

		assertTrue(newFitness<=oldFitness);
	}

	private DefaultTestCase createTestCase3()
			throws NoSuchFieldException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		final Class<?> mimeTypeClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MIMEType.class.getName());
		final Field xmlField = mimeTypeClass.getDeclaredField("XML");
		final Method toString = mimeTypeClass.getMethod("toString");

		final TestCaseBuilder builder = new TestCaseBuilder();
		VariableReference mIMEType0 = builder.appendStaticFieldStmt(xmlField);
		VariableReference string0 = builder.appendMethod(mIMEType0, toString);

		System.out.println("Test Case #3=" + builder.toCode());
		return builder.getDefaultTestCase();
	}

	private DefaultTestCase createTestCase4()
			throws NoSuchFieldException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		final Class<?> mimeTypeClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MIMEType.class.getName());
		final Field rdfField = mimeTypeClass.getDeclaredField("RDF");
		final Field slashField = mimeTypeClass.getDeclaredField("slash");
		final Method equalsMethod = mimeTypeClass.getMethod("equals", Object.class);
		final Constructor<?> constructorString = mimeTypeClass.getConstructor(String.class);
		final Field mimeTypeField = mimeTypeClass.getDeclaredField("mimeType");
		final Method getTypeMethod = mimeTypeClass.getMethod("getType");
		final Method toString = mimeTypeClass.getMethod("toString");

		final TestCaseBuilder builder = new TestCaseBuilder();
		VariableReference mIMEType0 = builder.appendStaticFieldStmt(rdfField);
		VariableReference int0 = builder.appendIntPrimitive(2415);
		VariableReference int1 = builder.appendIntPrimitive(2415);
		VariableReference int2 = builder.appendIntPrimitive(-196);
		builder.appendAssignment(mIMEType0, slashField, int2);
		builder.appendAssignment(mIMEType0, slashField, int0);
		VariableReference int3 = builder.appendIntPrimitive(0);
		builder.appendAssignment(mIMEType0, slashField, int1);
		builder.appendAssignment(mIMEType0, slashField, int3);
		VariableReference boolean0 = builder.appendMethod(mIMEType0, equalsMethod, mIMEType0);
		VariableReference string0 = builder.appendStringPrimitive("/");
		VariableReference string1 = builder.appendStringPrimitive("\"cC3$]nc.<p) u:");
		VariableReference mIMEType1 = builder.appendConstructor(constructorString, string0);
		builder.appendAssignment(mIMEType1, slashField, mIMEType0, slashField);
		VariableReference string2 = builder.appendNull(String.class);
		builder.appendAssignment(mIMEType1, mimeTypeField, string2);
		VariableReference int4 = builder.appendFieldStmt(mIMEType0, slashField);
		builder.appendMethod(mIMEType1, getTypeMethod);
		builder.addException(new NullPointerException());
		VariableReference string3 = builder.appendMethod(mIMEType1, toString);
		
		System.out.println("Test Case #4=" + builder.toCode());
		return builder.getDefaultTestCase();
	}

	private DefaultTestCase createTestCase5()
			throws NoSuchFieldException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		final Class<?> mimeTypeClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MIMEType.class.getName());
		final Field memField = mimeTypeClass.getDeclaredField("MEM");
		final Method toString = mimeTypeClass.getMethod("toString");
	
		final TestCaseBuilder builder = new TestCaseBuilder();
		VariableReference mIMEType0 = builder.appendStaticFieldStmt(memField);
		VariableReference string0 = builder.appendMethod(mIMEType0, toString);
		
		System.out.println("Test Case #5=" + builder.toCode());
		return builder.getDefaultTestCase();
	}

}
