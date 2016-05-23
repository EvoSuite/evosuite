package org.evosuite.symbolic;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.strategy.PropertiesSuiteGAFactory;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.testsuite.similarity.DiversityObserver;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;

public class DSEStrategy extends TestGenerationStrategy {

	@Override
	public TestSuiteChromosome generateTests() {
		LoggingUtils.getEvoLogger().info("* Setting up DSE test suite generation");

		long startTime = System.currentTimeMillis() / 1000;

		Properties.CRITERION = new Criterion[] { Properties.Criterion.BRANCH };

		// What's the search target
		List<TestSuiteFitnessFunction> fitnessFunctions = getFitnessFunctions();

		List<TestFitnessFunction> goals = getGoals(true);
		if (!canGenerateTestsForSUT()) {
			LoggingUtils.getEvoLogger()
					.info("* Found no testable methods in the target class " + Properties.TARGET_CLASS);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());

			return new TestSuiteChromosome();
		}

		/*
		 * Proceed with search if CRITERION=EXCEPTION, even if goals is empty
		 */
		TestSuiteChromosome testSuite = null;
		if (!(Properties.STOP_ZERO && goals.isEmpty())
				|| ArrayUtil.contains(Properties.CRITERION, Criterion.EXCEPTION)) {
			// Perform search
			LoggingUtils.getEvoLogger().info("* Using seed {}", Randomness.getSeed());
			LoggingUtils.getEvoLogger().info("* Starting evolution");
			ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

			testSuite = generateSuite();
		} else {
			zeroFitness.setFinished();
			testSuite = new TestSuiteChromosome();
			for (FitnessFunction<?> ff : fitnessFunctions) {
				testSuite.setCoverage(ff, 1.0);
			}
		}

		long endTime = System.currentTimeMillis() / 1000;

		goals = getGoals(false); // recalculated now after the search, eg to
									// handle exception fitness
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());

		// Newline after progress bar
		if (Properties.SHOW_PROGRESS)
			LoggingUtils.getEvoLogger().info("");

		if (!Properties.IS_RUNNING_A_SYSTEM_TEST) { // avoid printing time
													// related info in system
													// tests due to lack of
													// determinism
			LoggingUtils.getEvoLogger()
					.info("* Search finished after " + (endTime - startTime) + "s and "
							+ MaxStatementsStoppingCondition.getNumExecutedStatements()
							+ " statements, best individual has fitness: " + testSuite.getFitness());
		}

		// Search is finished, send statistics
		sendExecutionStatistics();

		return testSuite;

	}

	private TestSuiteChromosome generateSuite() {
		final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
		final Set<Method> staticMethods = getStaticMethods(targetClass);
		TestSuiteChromosome result = new TestSuiteChromosome();
		for (Method staticMethod : staticMethods) {
			List<TestChromosome> testCases = generateTests(staticMethod);
			result.addTests(testCases);
		}
		return result;
	}

	private List<TestChromosome> generateTests(Method staticMethod) {
		List<TestChromosome> testCases = new LinkedList<TestChromosome>();
		// TODO DSE Algorithm goes here
		return testCases;
	}

	private List<TestFitnessFunction> getGoals(boolean verbose) {
		List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
		List<TestFitnessFunction> goals = new ArrayList<>();

		if (goalFactories.size() == 1) {
			TestFitnessFactory<? extends TestFitnessFunction> factory = goalFactories.iterator().next();
			goals.addAll(factory.getCoverageGoals());

			if (verbose) {
				LoggingUtils.getEvoLogger().info("* Total number of test goals: {}", factory.getCoverageGoals().size());
				if (Properties.PRINT_GOALS) {
					for (TestFitnessFunction goal : factory.getCoverageGoals())
						LoggingUtils.getEvoLogger().info("" + goal.toString());
				}
			}
		} else {
			if (verbose) {
				LoggingUtils.getEvoLogger().info("* Total number of test goals: ");
			}

			for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
				goals.addAll(goalFactory.getCoverageGoals());

				if (verbose) {
					LoggingUtils.getEvoLogger()
							.info("  - " + goalFactory.getClass().getSimpleName().replace("CoverageFactory", "") + " "
									+ goalFactory.getCoverageGoals().size());
					if (Properties.PRINT_GOALS) {
						for (TestFitnessFunction goal : goalFactory.getCoverageGoals())
							LoggingUtils.getEvoLogger().info("" + goal.toString());
					}
				}
			}
		}
		return goals;
	}

	private static Set<Method> getStaticMethods(Class<?> targetClass) {
		Method[] declaredMethods = targetClass.getDeclaredMethods();
		Set<Method> staticMethods = new HashSet<Method>();
		for (Method m : declaredMethods) {
			if (Modifier.isStatic(m.getModifiers())) {
				staticMethods.add(m);
			}
		}
		return staticMethods;
	}

}
