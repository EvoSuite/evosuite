/*
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
package org.evosuite.strategy;

import org.evosuite.ClientProcess;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.TestSuiteChromosomeFactoryMock;
import org.evosuite.ga.TestSuiteFitnessFunctionMock;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test generation with MOSA
 * 
 * @author Annibale,Fitsum
 *
 */
public class MOSuiteStrategy extends TestGenerationStrategy {

	@Override	
	public TestSuiteChromosome generateTests() {
		// Currently only LIPS uses its own Archive
		if (Properties.ALGORITHM == Properties.Algorithm.LIPS) {
			Properties.TEST_ARCHIVE = false;
		}

		// Set up search algorithm
		PropertiesSuiteGAFactory algorithmFactory = new PropertiesSuiteGAFactory();

		GeneticAlgorithm<TestSuiteChromosome> algorithm = algorithmFactory.getSearchAlgorithm();
		
		// Override chromosome factory
		// TODO handle this better by introducing generics
		ChromosomeFactory<TestSuiteChromosome> factory =
				new TestSuiteChromosomeFactoryMock(new RandomLengthTestFactory());
		algorithm.setChromosomeFactory(factory);
		
		if(Properties.SERIALIZE_GA || Properties.CLIENT_ON_THREAD)
			TestGenerationResultBuilder.getInstance().setGeneticAlgorithm(algorithm);

		long startTimeMillis = System.currentTimeMillis();
		long startTime = startTimeMillis / 1000;

		// What's the search target
		List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
		List<FitnessFunction<TestSuiteChromosome>> fitnessFunctions = new ArrayList<>();

		for (TestFitnessFactory<? extends TestFitnessFunction> f : goalFactories) {
			for (TestFitnessFunction goal : f.getCoverageGoals()) {
				FitnessFunction<TestSuiteChromosome> mock = new TestSuiteFitnessFunctionMock(goal);
				fitnessFunctions.add(mock);
			}
		}

		algorithm.addFitnessFunctions(fitnessFunctions);

		// if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
		algorithm.addListener(progressMonitor); // FIXME progressMonitor may cause
		// client hang if EvoSuite is
		// executed with -prefix!
		
//		List<TestFitnessFunction> goals = getGoals(true);
		LoggingUtils.getEvoLogger().info("* " + ClientProcess.getPrettyPrintIdentifier() + "Total number of test goals for {}: {}",
				Properties.ALGORITHM.name(), fitnessFunctions.size());
		
//		ga.setChromosomeFactory(getChromosomeFactory(fitnessFunctions.get(0))); // FIXME: just one fitness function?

//		if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
//			ga.addListener(progressMonitor); // FIXME progressMonitor may cause

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.ALLDEFS) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.STATEMENT) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.RHO) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.BRANCH) ||
				ArrayUtil.contains(Properties.CRITERION, Criterion.AMBIGUITY))
			ExecutionTracer.enableTraceCalls();

		algorithm.resetStoppingConditions();
		
		TestSuiteChromosome testSuite = null;

		if (!(Properties.STOP_ZERO && fitnessFunctions.isEmpty()) || ArrayUtil.contains(Properties.CRITERION, Criterion.EXCEPTION)) {
			// Perform search
			LoggingUtils.getEvoLogger().info("* " + ClientProcess.getPrettyPrintIdentifier() + "Using seed {}", Randomness.getSeed());
			LoggingUtils.getEvoLogger().info("* " + ClientProcess.getPrettyPrintIdentifier() + "Starting evolution");
			ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

			algorithm.generateSolution();

			testSuite = algorithm.getBestIndividual();
			if (testSuite.getTestChromosomes().isEmpty()) {
				LoggingUtils.getEvoLogger().warn(ClientProcess.getPrettyPrintIdentifier() + "Could not generate any test case");
			}
		} else {
			zeroFitness.setFinished();
			testSuite = new TestSuiteChromosome();
			for (FitnessFunction<TestSuiteChromosome> ff : testSuite.getFitnessValues().keySet()) {
				testSuite.setCoverage(ff, 1.0);
			}
		}

		long endTimeMillis = System.currentTimeMillis();
		long endTime = endTimeMillis / 1000;

//		goals = getGoals(false); //recalculated now after the search, eg to handle exception fitness
//        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());
        
		// Newline after progress bar
		if (Properties.SHOW_PROGRESS)
			LoggingUtils.getEvoLogger().info("");
		
		String text = " statements, best individual has fitness: ";
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Search_Time,
				endTimeMillis-startTimeMillis);
		LoggingUtils.getEvoLogger().info("* " + ClientProcess.getPrettyPrintIdentifier() + "Search finished after "
				+ (endTime - startTime)
				+ "s and "
				+ algorithm.getAge()
				+ " generations, "
				+ MaxStatementsStoppingCondition.getNumExecutedStatements()
				+ text
				+ testSuite.getFitness());

		long binaryImageBranchDistances = computeBinaryImageBranchDistances(fitnessFunctions);
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BINARY_IMAGE_BRANCHES,
				binaryImageBranchDistances);

		// Search is finished, send statistics
		sendExecutionStatistics();

		// We send the info about the total number of coverage goals/targets only after 
		// the end of the search. This is because the number of coverage targets may vary
		// when the criterion Properties.Criterion.EXCEPTION is used (exception coverage
		// goal are dynamically added when the generated tests trigger some exceptions
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, algorithm.getFitnessFunctions().size());
		
		return testSuite;
	}

	/**
	 * Compute how many Branch Distance Fitness Functions have a binary image
	 *
	 * @param fitnessFunctions The fitness functions, that should be filtered
	 * @return The count of the fitness functions, which fulfill the condition.
	 */
	long computeBinaryImageBranchDistances(List<TestFitnessFunction> fitnessFunctions){

		return fitnessFunctions.stream()
				// Only branch distance fitness functions are relevant
				.filter(ff -> ff instanceof BranchCoverageTestFitness)
				// Map to unique fitness values
				.map(ff -> ((BranchCoverageTestFitness) ff).getUniqueFitnessValueView())
				// Ignore Approach Level and count unique values
				.map(doubles -> doubles.stream().filter(d -> 0 <= d && d < 1).count()).map(Long::intValue)
				.filter(i -> i <= 2)
				.count();
	}
	
}
