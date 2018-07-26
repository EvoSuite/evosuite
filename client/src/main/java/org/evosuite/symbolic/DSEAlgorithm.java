package org.evosuite.symbolic;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
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
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.localsearch.DSETestGenerator;
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

	private static class BranchId {
		private final String className;
		private final String methodName;
		private final int instructionIndex;

		public BranchId(String className, String methodName, int instructionIndex) {
			super();
			this.className = className;
			this.methodName = methodName;
			this.instructionIndex = instructionIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((className == null) ? 0 : className.hashCode());
			result = prime * result + instructionIndex;
			result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BranchId other = (BranchId) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			if (instructionIndex != other.instructionIndex)
				return false;
			if (methodName == null) {
				if (other.methodName != null)
					return false;
			} else if (!methodName.equals(other.methodName))
				return false;
			return true;
		}

	}

	private static List<TestCase> generateTestCases(Method staticEntryMethod) {

		List<TestCase> generatedTestCases = new ArrayList<TestCase>();

		TestCase initialTestCase = buildTestCaseWithDefaultValues(staticEntryMethod);
		generatedTestCases.add(initialTestCase);

		Map<Set<Constraint<?>>, SolverResult> queryCache = new HashMap<Set<Constraint<?>>, SolverResult>();
		HashSet<Set<Constraint<?>>> collectedPathConditions = new HashSet<Set<Constraint<?>>>();
		HashSet<Map<String, Object>> solutions = new HashSet<Map<String, Object>>();

		for (int currentTestIndex = 0; currentTestIndex < generatedTestCases.size(); currentTestIndex++) {

			TestCase currentTestCase = generatedTestCases.get(currentTestIndex);

			List<BranchCondition> collectedBranchConditions = ConcolicExecution
					.executeConcolic((DefaultTestCase) currentTestCase);

			final PathCondition collectedPathCondition = new PathCondition(collectedBranchConditions);
			List<Constraint<?>> constraintsList = collectedPathCondition.getConstraints();
			Set<Constraint<?>> constraintsSet = new HashSet<Constraint<?>>(constraintsList);
			collectedPathConditions.add(constraintsSet);

			for (int i = collectedPathCondition.size() - 1; i >= 0; i--) {
				List<Constraint<?>> queryList = DSETestGenerator.buildQuery(collectedPathCondition, i);
				Set<Constraint<?>> querySet = new HashSet<Constraint<?>>(queryList);
				if (!queryCache.containsKey(querySet) && !collectedPathConditions.contains(querySet)) {

					if (isSubSetOf(querySet, collectedPathConditions)) {
						continue;
					}

					SolverResult result = DSETestGenerator.solve(queryList);
					queryCache.put(querySet, result);
					if (result != null && result.isSAT()) {
						Map<String, Object> solution = result.getModel();
						solutions.add(solution);
						TestCase newTest = DSETestGenerator.updateTest(currentTestCase, solution);
						generatedTestCases.add(newTest);
					}
				}
			}
		}

		return generatedTestCases;
	}

	private static boolean isSubSetOf(Set<Constraint<?>> querySet,
			HashSet<Set<Constraint<?>>> collectedPathConditions) {
		for (Set<Constraint<?>> pathCondition : collectedPathConditions) {
			if (pathCondition.containsAll(querySet)) {
				return true;
			}
		}
		return false;
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
				((DefaultTestCase) testCase).changeClassLoader(classLoader);
			}
		}

		TestSuiteChromosome bestIndividual = new TestSuiteChromosome();
		for (TestCase testCase : generatedTestCases) {
			((DefaultTestCase) testCase).changeClassLoader(classLoader);
			bestIndividual.addTest(testCase);
		}

		// one fitness evaluation is needed to set up everything for post-process
		BranchCoverageSuiteFitness ff = new BranchCoverageSuiteFitness();
		double fitness = ff.getFitness(bestIndividual);

		population.clear();
		population.add(bestIndividual);

		this.notifySearchFinished();
	}

}
