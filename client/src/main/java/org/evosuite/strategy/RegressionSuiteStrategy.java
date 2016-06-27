/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.CoverageCriteriaAnalyzer;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.regression.RegressionAssertionCounter;
import org.evosuite.regression.RegressionMeasure;
import org.evosuite.regression.RegressionSearchListener;
import org.evosuite.regression.RegressionTestChromosome;
import org.evosuite.regression.RegressionTestChromosomeFactory;
import org.evosuite.regression.RegressionTestSuiteChromosome;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;

public class RegressionSuiteStrategy extends TestGenerationStrategy {

	private final RegressionSearchListener regressionMonitor = new RegressionSearchListener();

	public final static ZeroFitnessStoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();

	@Override
	public TestSuiteChromosome generateTests() {
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, 0);
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Generated_Assertions, 0);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Coverage_Old, 0);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Coverage_New, 0);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Exception_Difference, 0);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.State_Distance, 0);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Testsuite_Diversity, 0);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Regression_ID, RegressionSearchListener.statsID);
		
        // Disable test archive
        Properties.TEST_ARCHIVE = false;
        
        // Disable functional mocking stuff (due to incompatibilities)
        Properties.P_FUNCTIONAL_MOCKING = 0;
        Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT = 0;
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0;
            
        
		// Regression random strategy switch. 
		if (Properties.REGRESSION_FITNESS == RegressionMeasure.RANDOM) {
			 return generateRandomRegressionTests();
		}

		LoggingUtils.getEvoLogger().info(
				"* Setting up search algorithm for REGRESSION suite generation");
		PropertiesSuiteGAFactory algorithmFactory = new PropertiesSuiteGAFactory();
		GeneticAlgorithm<?> algorithm = algorithmFactory.getSearchAlgorithm();

		if (Properties.SERIALIZE_GA || Properties.CLIENT_ON_THREAD)
			TestGenerationResultBuilder.getInstance().setGeneticAlgorithm(
					algorithm);

		long startTime = System.currentTimeMillis() / 1000;

		Properties.CRITERION = new Criterion[] {Criterion.REGRESSION};
		// What's the search target
		List<TestSuiteFitnessFunction> fitnessFunctions = getFitnessFunctions();

		// TODO: Argh, generics.
		algorithm.addFitnessFunctions((List) fitnessFunctions);

		algorithm.addListener(regressionMonitor); // FIXME progressMonitor may
													// cause
		// client hang if EvoSuite is
		// executed with -prefix!

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
				|| ArrayUtil.contains(Properties.CRITERION, Criterion.ALLDEFS)
				|| ArrayUtil
						.contains(Properties.CRITERION, Criterion.STATEMENT)
				|| ArrayUtil.contains(Properties.CRITERION, Criterion.RHO)
				|| ArrayUtil
						.contains(Properties.CRITERION, Criterion.AMBIGUITY))
			ExecutionTracer.enableTraceCalls();

		// TODO: why it was only if "analyzing"???
		// if (analyzing)
		algorithm.resetStoppingConditions();

		List<TestFitnessFunction> goals = getGoals(true);


		// List<TestSuiteChromosome> bestSuites = new
		// ArrayList<TestSuiteChromosome>();
		TestSuiteChromosome bestSuites = new TestSuiteChromosome();
		RegressionTestSuiteChromosome best = null;
		if (!(Properties.STOP_ZERO && goals.isEmpty())) {
			// logger.warn("performing search ... ############################################################");
			// Perform search
			LoggingUtils.getEvoLogger().info("* Using seed {}",
					Randomness.getSeed());
			LoggingUtils.getEvoLogger().info("* Starting evolution");
			ClientServices.getInstance().getClientNode()
					.changeState(ClientState.SEARCH);

			algorithm.generateSolution();
			best = (RegressionTestSuiteChromosome) algorithm.getBestIndividual();
			// List<TestSuiteChromosome> tmpTestSuiteList = new
			// ArrayList<TestSuiteChromosome>();
			for (TestCase t : best.getTests())
				bestSuites.addTest(t);
			// bestSuites = (List<TestSuiteChromosome>) ga.getBestIndividuals();
			if (bestSuites.size() == 0) {
				LoggingUtils.getEvoLogger().warn(
						"Could not find any suiteable chromosome");
				return bestSuites;
			}
		} else {			
			zeroFitness.setFinished();
			bestSuites = new TestSuiteChromosome();
			for (FitnessFunction<?> ff : bestSuites.getFitnessValues().keySet()) {
				bestSuites.setCoverage(ff, 1.0);
			}
		}

		long end_time = System.currentTimeMillis() / 1000;
		
		goals = getGoals(false); //recalculated now after the search, eg to handle exception fitness
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());
        
              

		// Newline after progress bar
		if (Properties.SHOW_PROGRESS)
			LoggingUtils.getEvoLogger().info("");
		String text = " statements, best individual has fitness: ";
		if (bestSuites.size() > 1) {
			text = " statements, best individuals have fitness: ";
		}
		LoggingUtils.getEvoLogger().info(
				"* Search finished after "
						+ (end_time - startTime)
						+ "s and "
						+ algorithm.getAge()
						+ " generations, "
						+ MaxStatementsStoppingCondition
								.getNumExecutedStatements() + text
						+ best.getFitness());

		// progressMonitor.updateStatus(33);

		// progressMonitor.updateStatus(66);

		if (Properties.COVERAGE) {
			for (Properties.Criterion pc : Properties.CRITERION)
				CoverageCriteriaAnalyzer.analyzeCoverage(bestSuites, pc); // FIXME: can
																	// we send
																	// all
																	// bestSuites?
		}

		// progressMonitor.updateStatus(99);

		int number_of_test_cases = 0;
		int totalLengthOfTestCases = 0;
		double coverage = 0.0;

		// for (TestSuiteChromosome tsc : bestSuites) {
		number_of_test_cases += bestSuites.size();
		totalLengthOfTestCases += bestSuites.totalLengthOfTestCases();
		coverage += bestSuites.getCoverage();
		// }
		// coverage = coverage / ((double)bestSuites.size());

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.MUTATION)
				|| ArrayUtil.contains(Properties.CRITERION,
						Criterion.STRONGMUTATION)) {
			// SearchStatistics.getInstance().mutationScore(coverage);
		}

		// StatisticsSender.executedAndThenSendIndividualToMaster(bestSuites);
		// // FIXME: can we send all bestSuites?
		// statistics.iteration(ga);
		// statistics.minimized(bestSuites.get(0)); // FIXME: can we send all
		// bestSuites?
		LoggingUtils.getEvoLogger().info(
				"* Generated " + number_of_test_cases
						+ " tests with total length " + totalLengthOfTestCases);

		// TODO: In the end we will only need one analysis technique
		if (!Properties.ANALYSIS_CRITERIA.isEmpty()) {
			// SearchStatistics.getInstance().addCoverage(Properties.CRITERION.toString(),
			// coverage);
			CoverageCriteriaAnalyzer.analyzeCriteria(bestSuites,
					Properties.ANALYSIS_CRITERIA); // FIXME: can we send all
													// bestSuites?
		}

		LoggingUtils.getEvoLogger().info(
				"* Resulting test suite's coverage: "
						+ NumberFormat.getPercentInstance().format(coverage));

		algorithm.printBudget();

		// System.exit(0);
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Regression_ID, RegressionSearchListener.statsID);

		return bestSuites;
	}

	private TestSuiteChromosome generateRandomRegressionTests() {
		LoggingUtils.getEvoLogger().info(
				"* Using RANDOM regression test generation");

		RegressionTestSuiteChromosome suite = new RegressionTestSuiteChromosome();
		
		PropertiesSuiteGAFactory algorithmFactory = new PropertiesSuiteGAFactory();
		GeneticAlgorithm<?> suiteGA = algorithmFactory.getSearchAlgorithm();
		
		//statistics.searchStarted(suiteGA);


		regressionMonitor.searchStarted(suiteGA);
		RegressionTestChromosomeFactory factory = new RegressionTestChromosomeFactory();
		LoggingUtils.getEvoLogger().warn("*** generating RANDOM regression tests");
		// TODO: Shutdown hook?
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals,
				goals.size());
		
		StoppingCondition stoppingCondition = getStoppingCondition();
		// fitnessFunction.getFitness(suite);
		int totalTestCount = 0;
		int usefulTestCount = 0;

		int simulatedAge = 0;
		int numAssertions = 0;
		
		int executedStatemets = 0;

		boolean firstTry = true;
		// Properties.REGRESSION_RANDOM_STRATEGY:
		// 0: skip evaluation after first find, dont keep tests 
		// 1: dont skip evaluation after first find, dont keep tests
		// 2: dont skip evaluation after first find, keep tests
		// 3: skip evaluation after first find, keep tests [default]

		long startTime = System.currentTimeMillis();
		while (!stoppingCondition.isFinished() || (numAssertions != 0)) {

			if (numAssertions == 0 || Properties.REGRESSION_RANDOM_STRATEGY==1 || Properties.REGRESSION_RANDOM_STRATEGY==2 ) {

				RegressionTestChromosome test = factory.getChromosome();
				RegressionTestSuiteChromosome clone = new RegressionTestSuiteChromosome();
				clone.addTest(test);
				
				List<TestCase> testCases = clone.getTests();
				// fitnessFunction.getFitness(clone);
				/*
				 * logger.debug("Old fitness: {}, new fitness: {}",
				 * suite.getFitness(), clone.getFitness());
				 */
				executedStatemets+= test.size();
				numAssertions = RegressionAssertionCounter.getNumAssertions(clone);
				if(numAssertions>0)
					LoggingUtils.getEvoLogger().warn("Generated test with {} assertions.", numAssertions);
				totalTestCount++;
				if (numAssertions > 0) {
					numAssertions = 0;
					//boolean compilable = JUnitAnalyzer.verifyCompilationAndExecution(testCases);
					if(true){
						JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);
						JUnitAnalyzer.handleTestsThatAreUnstable(testCases);	
						if(testCases.size()>0){
							clone = new RegressionTestSuiteChromosome();
							
							for(TestCase t: testCases){
								RegressionTestChromosome rtc = new RegressionTestChromosome();
								if(t.isUnstable())
									continue;
								TestChromosome tc = new TestChromosome();
								tc.setTestCase(t);
								rtc.setTest(tc);
								clone.addTest(rtc);
							}
							//test.set
							//clone.addTest(testCases);
							 
							numAssertions = RegressionAssertionCounter.getNumAssertions(
									clone, false ,false);
							LoggingUtils.getEvoLogger().warn("Keeping {} assertions.", numAssertions);
							if (numAssertions > 0) {
								usefulTestCount++;
								suite.addTest(test);
							}
						} else {
							LoggingUtils.getEvoLogger().warn("ignored assertions. tests were removed.");
						}
					} else {
						LoggingUtils.getEvoLogger().warn("ignored assertions. not compilable.");
					}
				}
			} else {
				
				if(numAssertions > 0)
					break;
				/*
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
			}

			// regressionMonitor.fitnessEvaluation(suite);
			// regressionMonitor.iteration(suiteGA);
			if (firstTry
					|| (System.currentTimeMillis() - startTime) >= 4000) {
				try {

					startTime = System.currentTimeMillis();
					simulatedAge++;
					if(!RegressionSearchListener.skipWritingStats){
					RegressionSearchListener
							.statsFileWriter.write(
									"\r\n"
											+ "0,"
											+ totalTestCount
											+ ","
											+ suite.totalLengthOfTestCases()
											+ ",0,0,0,0,"
											+ RegressionSearchListener.exceptionDiff
											+",0,0," + executedStatemets
											+ ","
											+ simulatedAge
											+ ","
											+ (System.currentTimeMillis() - RegressionSearchListener.startTime)
											+ "," + numAssertions + ","
											+ (firstTry ? "F" : "P") + ",,,,,,");
					
					RegressionSearchListener
					.statsFileWriter.flush();
					}
					firstTry = false;
				} catch (Exception e) {
					e.printStackTrace();
				} catch(Throwable t){
					// something happened, we don't care :-)
					t.printStackTrace();
				}
			}
		}

		RegressionSearchListener.lastLine = "\r\n"
				+ "0,"
				+ totalTestCount //suite.size()
				+ ","
				+ suite.totalLengthOfTestCases()
				+ ",0,0,0,0,0,0,0," + executedStatemets
				+ ","
				+ (++simulatedAge)
				+ ","
				+ (System.currentTimeMillis() - RegressionSearchListener.startTime)
				+ "," + "ASSERTIONS" + "," + "L"
				+ ",0,0,0,0,0,0";
		
		if(!Properties.MINIMIZE)
			RegressionSearchListener.flushLastLine(numAssertions,totalTestCount,suite.totalLengthOfTestCases());

		// regressionMonitor.searchFinished(suiteGA);
		LoggingUtils.getEvoLogger().warn("*** Random test generation finished.");
		LoggingUtils.getEvoLogger().warn("*=*=*=* Total tests: {} | Tests with assertion: {}",
				totalTestCount, usefulTestCount);
		
		//statistics.searchFinished(suiteGA);
		zero_fitness.setFinished();

		LoggingUtils.getEvoLogger().info(
				"* Generated " + suite.size() + " tests with total length "
						+ suite.totalLengthOfTestCases());
		/*try {
			File file = new File("results.txt");
			System.out.println("\n\r" + numAssertions + ", "
					+ suite.totalLengthOfTestCases());
			FileUtils.writeStringToFile(file, "\r\n" + executedStatemets + ", "
					+ suite.totalLengthOfTestCases(), true);
		} catch (IOException e) {
			assert false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		suiteGA.printBudget();

		if (!(Properties.REGRESSION_RANDOM_STRATEGY == 2 || Properties.REGRESSION_RANDOM_STRATEGY == 3))
			suite = new RegressionTestSuiteChromosome();

		TestSuiteChromosome bestSuites = new TestSuiteChromosome();
		

		for(TestCase t:suite.getTests())
			bestSuites.addTest(t);
		
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Regression_ID, RegressionSearchListener.statsID);
		
		return bestSuites;
	}

	private List<TestFitnessFunction> getGoals(boolean verbose) {
		List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
		List<TestFitnessFunction> goals = new ArrayList<>();

		if (goalFactories.size() == 1) {
			TestFitnessFactory<? extends TestFitnessFunction> factory = goalFactories
					.iterator().next();
			goals.addAll(factory.getCoverageGoals());

			if (verbose) {
				LoggingUtils.getEvoLogger().info(
						"* Total number of test goals: {}",
						factory.getCoverageGoals().size());
			}
		} else {
			if (verbose) {
				LoggingUtils.getEvoLogger().info(
						"* Total number of test goals: ");
			}

			for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
				goals.addAll(goalFactory.getCoverageGoals());

				if (verbose) {
					LoggingUtils.getEvoLogger().info(
							"  - "
									+ goalFactory.getClass().getSimpleName()
											.replace("CoverageFactory", "")
									+ " "
									+ goalFactory.getCoverageGoals().size());
				}
			}
		}
		return goals;
	}

}
