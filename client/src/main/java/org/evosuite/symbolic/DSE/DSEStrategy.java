/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.DSE;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.FitnessFunctionsUtils;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.symbolic.DSE.algorithm.DSEAlgorithm;
import org.evosuite.symbolic.DSE.algorithm.DSEAlgorithmFactory;
import org.evosuite.symbolic.DSE.algorithm.DSEAlgorithms;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;

/**
 * <p>
 * DSEStrategy class
 * </p>
 *
 * @author ignacio lebrero
 */
public class DSEStrategy extends TestGenerationStrategy {

	public static final String SETTING_UP_DSE_GENERATION_INFO_MESSAGE = "* Setting up DSE test suite generation";
	public static final String NOT_SOUITALE_METHOD_FOUND_INFO_MESSAGE = "* Found no testable methods in the target class {}";

	@Override
	public TestSuiteChromosome generateTests() {
		LoggingUtils.getEvoLogger().info(SETTING_UP_DSE_GENERATION_INFO_MESSAGE);
		DSEAlgorithms selectedAlgorithm = Properties.DSE_ALGORITHM_TYPE;
		Properties.CRITERION = selectedAlgorithm.getCriteria();

		long startTime = System.currentTimeMillis() / 1000;

		//TODO: move this to a dependency injection schema
		List<TestFitnessFunction> goals = getFitnessFunctionsGoals(true);
		if (!canGenerateTestsForSUT()) {
			LoggingUtils.getEvoLogger().info(
				NOT_SOUITALE_METHOD_FOUND_INFO_MESSAGE,
				Properties.TARGET_CLASS
			);
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
			// This is in case any algorithm internal strategy uses some random behaviour.
			//     e.g. after x iterations selects the next path randomly.
			LoggingUtils.getEvoLogger().info("* Using seed {}", Randomness.getSeed());
			LoggingUtils.getEvoLogger().info("* Starting DSE");
			ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

			//TODO: move to dependency injection later on
			DSEAlgorithmFactory dseFactory = new DSEAlgorithmFactory();
			DSEAlgorithms dseAlgorithmType = Properties.DSE_ALGORITHM_TYPE;

			LoggingUtils.getEvoLogger().info("* Using DSE algorithm: {}", dseAlgorithmType.getName());
            DSEAlgorithm algorithm = dseFactory.getDSEAlgorithm(dseAlgorithmType);

			StoppingCondition stoppingCondition = getStoppingCondition();

			if (Properties.STOP_ZERO) {

			}

			testSuite = algorithm.generateSolution();

		} else {
			testSuite = setNoGoalsCoverage(Properties.DSE_ALGORITHM_TYPE);
		}

		long endTime = System.currentTimeMillis() / 1000;

		goals = getFitnessFunctionsGoals(false); // recalculated now after the search, eg to
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

	private TestSuiteChromosome setNoGoalsCoverage(DSEAlgorithms algorithm) {
		TestSuiteChromosome testSuite = new TestSuiteChromosome();;
		List<TestSuiteFitnessFunction> fitnessFunctions = FitnessFunctionsUtils
				.getFitnessFunctions(algorithm.getCriteria());

		zeroFitness.setFinished();

		for (FitnessFunction<?> ff : fitnessFunctions) {
			testSuite.setCoverage(ff, 1.0);
		}
		return testSuite;
	}


	/**
     * Returns current Fitness functions based on which Properties are currently set.
     *
     * @param verbose
     * @return
     */
    private static List<TestFitnessFunction> getFitnessFunctionsGoals(boolean verbose) {
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

}
