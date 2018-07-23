package org.evosuite.symbolic;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.dse.TestCaseBuilder;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.junit.rules.StaticStateResetter;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.objectweb.asm.Type;

/**
 * This class implements a DSE algorithm *as* a subclass of genetic algorithm.
 * 
 * @author jgaleotti
 *
 * @param <T>
 */
public class DSEAlgorithm extends GeneticAlgorithm<TestSuiteChromosome> {

	private static List<TestCase> generateTestCases(Method staticEntryMethod) {
		List<TestCase> generatedTestCases = new ArrayList<TestCase>();
		TestCase initialTestCase = buildTestCaseWithDefaultValues(staticEntryMethod);
		generatedTestCases.add(initialTestCase);

		List<BranchCondition> pathCondition = ConcolicExecution.executeConcolic((DefaultTestCase) initialTestCase);

		return generatedTestCases;
	}

	/**
	 * Builds a default test case for a static target method
	 * 
	 * @param targetStaticMethod
	 * @return
	 */
	private static DefaultTestCase buildTestCaseWithDefaultValues(Method targetStaticMethod) {
		TestCaseBuilder testCaseBuilder = new TestCaseBuilder();

		Type[] argumentTypes = Type.getArgumentTypes(targetStaticMethod);

		ArrayList<VariableReference> arguments = new ArrayList<VariableReference>();
		for (Type argumentType : argumentTypes) {
			switch (argumentType.getSort()) {
			case Type.INT: {
				VariableReference variableReference = testCaseBuilder.appendIntPrimitive(0);
				arguments.add(variableReference);
				break;
			}
			default: {
				throw new UnsupportedOperationException();
			}
			}
		}
		testCaseBuilder.appendMethod(targetStaticMethod, arguments.toArray(new VariableReference[] {}));
		DefaultTestCase testCase = testCaseBuilder.getDefaultTestCase();
		return testCase;
	}

	public DSEAlgorithm() {
		super(null);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 964984026539409121L;

	/**
	 * This algorithm does not evolve populations
	 */
	@Override
	protected void evolve() {
		// skip
	}

	/**
	 * This algorithm does not initialize population
	 */
	@Override
	public void initializePopulation() {
		// skip
	}

	/**
	 * Returns a set with the static methods of a class
	 * 
	 * @param targetClass
	 *            a class instance
	 * @return
	 */
	private static Set<Method> getTargetStaticMethods(Class<?> targetClass) {
		Method[] declaredMethods = targetClass.getDeclaredMethods();
		Set<Method> targetStaticMethods = new HashSet<Method>();
		for (Method m : declaredMethods) {

			if (!Modifier.isStatic(m.getModifiers())) {
				continue;
			}

			if (Modifier.isPrivate(m.getModifiers())) {
				continue;
			}

			if (m.getName().equals(ClassResetter.STATIC_RESET)) {
				continue;
			}

			targetStaticMethods.add(m);
		}
		return targetStaticMethods;
	}

	/**
	 * Applies the DSE test generation using the initial population as the initial
	 * test cases
	 */
	@Override
	public void generateSolution() {
		this.notifySearchStarted();

		final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
		TreeSet<Method> sortedEntryMethods = new TreeSet<Method>(new MethodComparator());

		sortedEntryMethods.addAll(getTargetStaticMethods(targetClass));

		InstrumentingClassLoader classLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();

		List<TestCase> generatedTestCases = new ArrayList<TestCase>();
		for (Method entryMethod : sortedEntryMethods) {
			List<TestCase> testCases = generateTestCases(entryMethod);
			generatedTestCases.addAll(testCases);
			for (TestCase testCase : testCases) {
				((DefaultTestCase)testCase).changeClassLoader(classLoader);
			}
		}

//		TestGenerationContext.getInstance().resetContext();
//		TestGenerationContext.getInstance().goingToExecuteSUTCode();
//
//		// We need to reset the target Class since it requires a different
//		// instrumentation
//		// for handling assertion generation.
//		Properties.resetTargetClass();
//		Properties.getInitializedTargetClass();
//

		TestSuiteChromosome bestIndividual = new TestSuiteChromosome();
		for (TestCase testCase : generatedTestCases) {
			((DefaultTestCase) testCase).changeClassLoader(classLoader);
			bestIndividual.addTest(testCase);
		}

		BranchCoverageSuiteFitness ff = new BranchCoverageSuiteFitness();
		double fitness = ff.getFitness(bestIndividual);

//		TestGenerationContext.getInstance().doneWithExecutingSUTCode();

		
		population.clear();
		population.add(bestIndividual);

		this.notifySearchFinished();
	}

}
