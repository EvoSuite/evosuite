package org.evosuite.symbolic;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.dse.TestCaseBuilder;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.localsearch.DSETestGenerator;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a DSE algorithm *as* a subclass of genetic algorithm.
 * 
 * @author jgaleotti
 *
 * @param <T>
 */
public class DSEAlgorithm extends GeneticAlgorithm<TestSuiteChromosome> {

	private static final Logger logger = LoggerFactory.getLogger(DSEAlgorithm.class);

	/**
	 * Applies DSE test generation on a static non-private method until a stopping
	 * condition is met or all queries have been explored.
	 * 
	 * @param staticEntryMethod
	 * @return
	 */
	private List<TestCase> generateTestCases(Method staticEntryMethod) {

		List<TestCase> generatedTestCases = new ArrayList<TestCase>();

		TestCase testCaseWithDefaultValues = buildTestCaseWithDefaultValues(staticEntryMethod);
		generatedTestCases.add(testCaseWithDefaultValues);
		logger.debug("Created new default test case with default values:" + testCaseWithDefaultValues.toCode());

		Map<Set<Constraint<?>>, SolverResult> queryCache = new HashMap<Set<Constraint<?>>, SolverResult>();
		HashSet<Set<Constraint<?>>> collectedPathConditions = new HashSet<Set<Constraint<?>>>();

		for (int currentTestIndex = 0; currentTestIndex < generatedTestCases.size(); currentTestIndex++) {

			TestCase currentTestCase = generatedTestCases.get(currentTestIndex);

			if (this.isFinished()) {
				logger.debug("DSE test generation met a stopping condition. Exiting with " + generatedTestCases.size()
						+ " generated test cases for method " + staticEntryMethod.getName());
				return generatedTestCases;
			}

			logger.debug("Starting concolic execution of test case: " + currentTestCase.toCode());
			List<BranchCondition> collectedBranchConditions = ConcolicExecution
					.executeConcolic((DefaultTestCase) currentTestCase);
			MaxTestsStoppingCondition.testExecuted();

			final PathCondition collectedPathCondition = new PathCondition(collectedBranchConditions);
			logger.debug("Path condition collected with : " + collectedPathCondition.size() + " branches");

			Set<Constraint<?>> constraintsSet = new HashSet<Constraint<?>>(collectedPathCondition.getConstraints());
			collectedPathConditions.add(constraintsSet);
			logger.debug("Number of stored path condition: " + collectedPathConditions.size());

			for (int i = collectedPathCondition.size() - 1; i >= 0; i--) {
				logger.debug("negating index " + i + " of path condition");

				List<Constraint<?>> query = DSETestGenerator.buildQuery(collectedPathCondition, i);

				Set<Constraint<?>> constraintSet = new HashSet<Constraint<?>>(query);

				if (queryCache.containsKey(constraintSet)) {
					logger.debug("skipping solving of current query since it is in the query cache");
					continue;
				}

				if (collectedPathConditions.contains(constraintSet)) {
					logger.debug("skipping solving of current query because of existing path condition");
					continue;

				}

				if (isSubSetOf(constraintSet, collectedPathConditions)) {
					logger.debug(
							"skipping solving of current query because it is satisfiable and solved by previous path condition");
					continue;
				}

				if (this.isFinished()) {
					logger.debug(
							"DSE test generation met a stopping condition. Exiting with " + generatedTestCases.size()
									+ " generated test cases for method " + staticEntryMethod.getName());
					return generatedTestCases;
				}

				logger.debug("Solving query with  " + query.size() + " constraints");
				SolverResult result = DSETestGenerator.solve(query);

				queryCache.put(constraintSet, result);
				logger.debug("Number of stored entries in query cache : " + queryCache.keySet().size());

				if (result == null) {
					logger.debug("Solver outcome is null (probably failure/unknown");
				} else if (result.isSAT()) {
					logger.debug("query is SAT (solution found)");
					Map<String, Object> solution = result.getModel();
					logger.debug("solver found solution " + solution.toString());
					TestCase newTest = DSETestGenerator.updateTest(currentTestCase, solution);
					logger.debug("Created new test case from SAT solution:" + newTest.toCode());
					generatedTestCases.add(newTest);
				} else {
					assert (result.isUNSAT());
					logger.debug("query is UNSAT (no solution found)");
				}
			}
		}

		logger.debug("DSE test generation finished for method " + staticEntryMethod.getName() + ". Exiting with "
				+ generatedTestCases.size() + " generated test cases");
		return generatedTestCases;
	}

	/**
	 * Returns true if the constraints in the query are a subset of any of the
	 * constraints in the set of queries
	 * 
	 * @param query
	 * @param queries
	 * @return
	 */
	private static boolean isSubSetOf(Set<Constraint<?>> query, HashSet<Set<Constraint<?>>> queries) {
		for (Set<Constraint<?>> pathCondition : queries) {
			if (pathCondition.containsAll(query)) {
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

	/**
	 * Creates a DSE algorithm for test generation.
	 */
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
	private static List<Method> getTargetStaticMethods(Class<?> targetClass) {
		Method[] declaredMethods = targetClass.getDeclaredMethods();
		List<Method> targetStaticMethods = new LinkedList<Method>();
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

		List<Method> targetStaticMethods = getTargetStaticMethods(targetClass);
		Collections.sort(targetStaticMethods, new MethodComparator());
		logger.debug("Found " + targetStaticMethods.size() + " as entry points for DSE");

		final InstrumentingClassLoader classLoader = TestGenerationContext.getInstance().getClassLoaderForSUT();

		List<TestCase> generatedTestCases = new ArrayList<TestCase>();
		for (Method entryMethod : targetStaticMethods) {

			if (this.isFinished()) {
				logger.debug("A stoping condition was met. No more tests can be generated using DSE.");
				break;
			}

			logger.debug("Generating tests for entry method" + entryMethod.getName());
			List<TestCase> testCases = generateTestCases(entryMethod);
			logger.debug(testCases.size() + " tests were generated for entry method " + entryMethod.getName());

			generatedTestCases.addAll(testCases);
		}

		TestSuiteChromosome bestIndividual = createTestSuite(generatedTestCases);

		logger.debug("Replacing concolic class loader with instrumenting class loader");
		changeClassLoader(bestIndividual, classLoader);

		logger.debug("Computing fitness evaluation of generated test cases with DSE");
		double branchCoverageFitness = computeBranchCoverageFitness(bestIndividual);
		logger.debug("Branch coverage fitness of test suite is " + branchCoverageFitness);

		population.clear();
		population.add(bestIndividual);

		this.notifySearchFinished();
	}

	/**
	 * Changes the class loader of each test case in the test suite
	 * 
	 * @param testSuite
	 * @param classLoader
	 */
	private void changeClassLoader(TestSuiteChromosome testSuite, ClassLoader classLoader) {
		for (TestCase testCase : testSuite.getTests()) {
			DefaultTestCase defaultTestCase = (DefaultTestCase) testCase;
			defaultTestCase.changeClassLoader(classLoader);
		}

	}

	/**
	 * Creates a test suite from a list of generated test cases
	 * 
	 * @param generatedTestCases
	 * @return
	 */
	private TestSuiteChromosome createTestSuite(List<TestCase> generatedTestCases) {
		TestSuiteChromosome bestIndividual = new TestSuiteChromosome();
		for (TestCase testCase : generatedTestCases) {
			bestIndividual.addTest(testCase);
		}
		return bestIndividual;
	}

	/**
	 * Returns the fitness value for the criteria of branch coverage
	 * 
	 * @param bestIndividual
	 * @return
	 */
	private double computeBranchCoverageFitness(TestSuiteChromosome bestIndividual) {
		BranchCoverageSuiteFitness ff = new BranchCoverageSuiteFitness();
		double branchCoverageFitness = ff.getFitness(bestIndividual);
		return branchCoverageFitness;
	}

}
