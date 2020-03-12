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
import org.evosuite.symbolic.DSE.algorithm.listener.implementations.MaxTimeStoppingCondition;
import org.evosuite.symbolic.DSE.algorithm.listener.implementations.TargetCoverageReachedStoppingCondition;
import org.evosuite.symbolic.DSE.algorithm.listener.implementations.ZeroFitnessStoppingCondition;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;

/**
 * <p>
 * DSEStrategy class.
 * </p>
 *
 * NOTE: even though we are on evosuite, this module is a bit out of context with the GA general framework.
 *       In the future rebuild it as a standalone library.
 *
 * @author ignacio lebrero
 */
public class DSEStrategy extends TestGenerationStrategy {

	public static final String SETTING_UP_DSE_GENERATION_INFO_MESSAGE = "* Setting up DSE test suite generation";
	public static final String NOT_SOUITALE_METHOD_FOUND_INFO_MESSAGE = "* Found no testable methods in the target class {}";

	/** DSE Stopping conditions */
	private final MaxTimeStoppingCondition maxTimeStoppingCondition = new MaxTimeStoppingCondition();
	private final ZeroFitnessStoppingCondition zeroFitnessStoppingCondition = new ZeroFitnessStoppingCondition();
	private final TargetCoverageReachedStoppingCondition targetCoverageReachedStoppingCondition = new TargetCoverageReachedStoppingCondition();

	@Override
	public TestSuiteChromosome generateTests() {
		LoggingUtils.getEvoLogger().info(SETTING_UP_DSE_GENERATION_INFO_MESSAGE);
		Properties.CRITERION = Properties.DSE_ALGORITHM_TYPE.getCriteria();
		Criterion[] criterion = Properties.CRITERION;

		long startTime = System.currentTimeMillis() / 1000;

		List<TestFitnessFunction> goals = FitnessFunctionsUtils.getFitnessFunctionsGoals(criterion, true);
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
				|| ArrayUtil.contains(criterion, Criterion.EXCEPTION)) {
			// Perform search
			// This is in case any algorithm internal strategy uses some random behaviour.
			//     e.g. after x iterations selects the next path randomly.
			LoggingUtils.getEvoLogger().info("* Using seed {}", Randomness.getSeed());
			LoggingUtils.getEvoLogger().info("* Starting DSE");
			ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

			// Builds the actual algorithm
			DSEAlgorithm algorithm = buildDSEAlgorithm();

			// ????
			if (Properties.STOP_ZERO) {

			}

			testSuite = algorithm.generateSolution();
		} else {
			testSuite = setNoGoalsCoverage(Properties.DSE_ALGORITHM_TYPE);
		}

		long endTime = System.currentTimeMillis() / 1000;

		goals = FitnessFunctionsUtils.getFitnessFunctionsGoals(criterion, false); // recalculated now after the search, eg to
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

	private DSEAlgorithm buildDSEAlgorithm() {
		DSEAlgorithmFactory dseFactory = new DSEAlgorithmFactory();
		DSEAlgorithms dseAlgorithmType = Properties.DSE_ALGORITHM_TYPE;

		LoggingUtils.getEvoLogger().info("* Using DSE algorithm: {}", dseAlgorithmType.getName());
		DSEAlgorithm algorithm = dseFactory.getDSEAlgorithm(dseAlgorithmType);

        // Adding stopping conditions
		algorithm.addStoppingCondition(maxTimeStoppingCondition);
		algorithm.addStoppingCondition(zeroFitnessStoppingCondition);
		algorithm.addStoppingCondition(targetCoverageReachedStoppingCondition);

		LoggingUtils.getEvoLogger().debug("* With timeout: {}", Properties.GLOBAL_TIMEOUT);
		LoggingUtils.getEvoLogger().debug("* With target coverage: {}", Properties.DSE_TARGET_COVERAGE);

		return algorithm;
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




}
