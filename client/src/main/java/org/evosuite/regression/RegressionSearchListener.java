/**
 * 
 */
package org.evosuite.regression;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.assertion.Assertion;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sina
 * 
 */
public class RegressionSearchListener implements SearchListener {

	public boolean isFirstRun = true;
	public boolean isFirst = true;
	public boolean isLastRun = false;
	public static List<TestCase> previousTestSuite;
	public static List<TestCase> currentTestSuite;
	public static int firstAssertionCount = 0;
	public static String statsID = "";

	public static long ObjectDistanceTime = 0;
	public static long branchDistanceTime = 0;
	public static long odCollectionTime = 0;
	public static long coverageTime = 0;
	public static long assertionTime = 0;
	public static long testExecutionTime = 0;

	public static boolean killTheSearch = false;

	public static long startTime;

	protected static final Logger logger = LoggerFactory
			.getLogger(RegressionSearchListener.class);

	public static String jdiffReport = "";

	public static String analysisReport = "";
	
	public static int exceptionDiff = 0;

	public RegressionSearchListener() {
		previousTestSuite = new ArrayList<TestCase>();
		currentTestSuite = new ArrayList<TestCase>();
	}

	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub
		String statsDirName = "evosuiter-stats";
		File statsDir = new File(statsDirName);
		int filecount = 0;
		if (statsDir.exists() && statsDir.isDirectory()) {
			filecount = statsDir.list().length;
		} else {
			statsDir.mkdirs();
		}

		statsFile = new File(statsDirName + "/" + (filecount + 1) + "" + ((int) (Math.random()*1000)) + "_"
				+ Properties.getTargetClass().getSimpleName() + ".csv");

		if (statsFile.exists())
			statsFile = new File(statsDirName + "/" + (filecount + 1) + "" + ((int) (Math.random()*1000)) + "_"
					+ Properties.getTargetClass().getSimpleName() + "_"
					+ System.currentTimeMillis() + ".csv");

		statsID = statsFile.getName().replaceFirst("[.][^.]+$", "");

		try {
			String data = "fitness,test_count,test_size,branch_distance,object_distance,coverage,exception_diff,total_exceptions,coverage_old,coverage_new,executed_statements,age,time,assertions,state,exec_time,assert_time,cover_time,state_diff_time,branch_time,obj_time";
			FileUtils.writeStringToFile(statsFile, data, false);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		startTime = System.currentTimeMillis();
	}

	public static File statsFile;
	public static double lastOD = 0.0;
	public static int lastAssertions = 0;

	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub
		char runState = (isFirst) ? 'F' : ((isLastRun) ? 'L' : 'P');
		if (isFirst)
			isFirst = false;
		RegressionTestSuiteChromosome ind = null;
		Chromosome individual = algorithm.getBestIndividual();
		if(individual instanceof RegressionTestChromosome)
		{
			ind = new RegressionTestSuiteChromosome();
			ind.addTest((RegressionTestChromosome) individual);
			ind.fitnessData = ((RegressionTestChromosome)individual).fitnessData;
			ind.objDistance =((RegressionTestChromosome)individual).objDistance;
			ind.diffExceptions = ((RegressionTestChromosome)individual).diffExceptions;
		} else {
		//	assert false;
		 ind = (RegressionTestSuiteChromosome) individual;
		 ind.fitnessData = ((RegressionTestSuiteChromosome)individual).fitnessData;
		 ind.objDistance =((RegressionTestSuiteChromosome)individual).objDistance;
		 ind.diffExceptions = ((RegressionTestSuiteChromosome)individual).diffExceptions;
		}
		// hack for getting the correct number of different exceptions
		ind.fitnessData = ind.fitnessData.replace("numDifferentExceptions", "" + exceptionDiff);
		
		try {
			int curAssertions = getNumAssertions(ind);
			//curAssertions += ind.diffExceptions;
			
			
			
			if(curAssertions>0){
				algorithm.getBestIndividual().setFitness(algorithm.getFitnessFunction(), 0);
				algorithm.setStoppingConditionLimit(0);
				//RegressionSearchListener.killTheSearch = false;
			}
			
			double curOD =ind.objDistance;
			FileUtils
					.writeStringToFile(
							statsFile,
							"\r\n"
									+ (ind).fitnessData
									+ ","
									+ ((isLastRun) ? (algorithm.getAge()+1):algorithm.getAge())
									+ ","
									+ (System.currentTimeMillis() - startTime)
									+ ","
									+ curAssertions
									+ ","
									+ runState
									+ ((isLastRun) ? (","
											+ (testExecutionTime + 1) / 1000000
											+ "," + (assertionTime + 1)
											/ 1000000 + ","
											+ (coverageTime + 1) / 1000000
											+ "," + (ObjectDistanceTime + 1)
											/ 1000000 + ","
											+ (branchDistanceTime + 1)
											/ 1000000 + "," + (odCollectionTime + 1) / 1000000)
											: ",,,,,,"), true);

			if (!isFirstRun) {
				if (lastAssertions < curAssertions && lastAssertions == 0
						&& lastOD <= curOD && (lastOD != 0 && curOD != 0)
						&& (algorithm.getAge() != 0)) {
					/*
					 * int aCount = getNumAssertions(
					 * algorithm.getBestIndividual(), false);
					 */
					String comments = "// Assertions count: " + curAssertions
							+ "\n" + "// Last assertions count: "
							+ lastAssertions + "\n"
							+ "// Current Object Distance: " + curOD + "\n"
							+ "// Last Object Distance: " + this.lastOD + "\n"
							+ "// StatsID: " + statsID + "\n" + "// Age: "
							+ algorithm.getAge() + "\n"
							+ "//--------------------------------------------"
							+ "\n" + "//--- CURRENT VERSION" + "\n"
							+ "//--------------------------------------------"
							+ "\n" + "\n";
					/*
					 * TestSuiteGenerator.keepJUnitTests(
					 * ((RegressionTestSuiteChromosome) algorithm
					 * .getBestIndividual()).getTests(), comments);
					 */
					/*TestSuiteGenerator.keepJUnitTests(previousTestSuite,
							comments);
					TestSuiteGenerator.keepJUnitTests(currentTestSuite,
							comments);*/
					// getNumAssertions(algorithm.getBestIndividual());
				} else if (curAssertions == 0 && lastAssertions > 0
						&& (algorithm.getAge() != 0)
						&& previousTestSuite.size() > 0) {
					/*
					 * int aCount = getNumAssertions(
					 * algorithm.getBestIndividual(), false);
					 */
					String comments = "// Assertions count: " + curAssertions
							+ "\n" + "// Last assertions count: "
							+ lastAssertions + "\n"
							+ "// Current Object Distance: " + curOD + "\n"
							+ "// Last Object Distance: " + this.lastOD + "\n"
							+ "// StatsID: " + statsID + "\n" + "// Age: "
							+ algorithm.getAge() + "\n"
							+ "//--------------------------------------------"
							+ "\n" + "//--- OLD VERSION" + "\n"
							+ "//--------------------------------------------"
							+ "\n" + "\n";
					/*TestSuiteGenerator.keepJUnitTests(previousTestSuite,
							comments);
					TestSuiteGenerator.keepJUnitTests(currentTestSuite,
							comments);*/
					// getNumAssertions(algorithm.getBestIndividual());
				}

			}

			// previousTestSuite.clear();
			if (curAssertions > 0) {
				/*
				 * previousTestSuite .addAll( ((RegressionTestSuiteChromosome)
				 * algorithm .getBestIndividual()).getTests() );
				 */
				/*
				 * previousTestSuite = new
				 * ArrayList<TestCase>(((RegressionTestSuiteChromosome
				 * )((RegressionTestSuiteChromosome) algorithm
				 * .getBestIndividual()).clone()).getTests() );
				 */
			}

			this.lastOD = curOD;
			lastAssertions = curAssertions;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub
		isLastRun = true;
		this.iteration(algorithm);
		int iteration = algorithm.getAge();
		logger.warn("total number of generations: " + iteration);
		RegressionTestSuiteChromosome ind = null;
		Chromosome individual = algorithm.getBestIndividual();
		if(individual instanceof RegressionTestChromosome)
		{
			ind = new RegressionTestSuiteChromosome();
			ind.addTest((RegressionTestChromosome) individual);
			ind.fitnessData = ((RegressionTestChromosome)individual).fitnessData;
			ind.objDistance =((RegressionTestChromosome)individual).objDistance;
			ind.diffExceptions = ((RegressionTestChromosome)individual).diffExceptions;
		} else {
		 ind = (RegressionTestSuiteChromosome) individual;
		 ind.fitnessData = ((RegressionTestSuiteChromosome)individual).fitnessData;
		 ind.objDistance =((RegressionTestSuiteChromosome)individual).objDistance;
		 ind.diffExceptions = ((RegressionTestSuiteChromosome)individual).diffExceptions;
		// assert false;
		}
		// hack for getting the correct number of different exceptions
		ind.fitnessData = ind.fitnessData.replace("numDifferentExceptions", "" + exceptionDiff);
		
		int totalCount = 0;
		if (iteration > 0) {
			totalCount = getNumAssertions(ind);
			//totalCount += ind.diffExceptions;
			if (firstAssertionCount == 0 && totalCount > 0) {
				logger.warn("Successful GA! First Gen: " + firstAssertionCount
						+ " | Last Gen: " + totalCount);
			} else {
				logger.warn("Failed GA! First Gen: " + firstAssertionCount
						+ " | Last Gen: " + totalCount);
			}
		}

		if (Properties.REGRESSION_ANALYZE) {
			String analyzeDirName = "evosuiter-analyze";
			File analyzeDir = new File(analyzeDirName);
			int filecount = 0;
			if (analyzeDir.exists() && analyzeDir.isDirectory()) {
				filecount = analyzeDir.list().length;
			} else {
				analyzeDir.mkdirs();
			}
			File analysisFile = new File(analyzeDirName + "/analysis.txt");
			String report = "Class: " + Properties.getTargetClass().getName()
					+ " | " + "gens: " + iteration + " | assertions: "
					+ totalCount + " | " + analysisReport + " | " + jdiffReport;
			logger.warn("Analysis report: " + report);
			try {
				FileUtils.writeStringToFile(analysisFile, report + "\n", true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub
		if (isFirstRun) {
			isFirstRun = false;
			int totalCount = getNumAssertions(individual);
			RegressionSearchListener.firstAssertionCount = totalCount;
		}
	}

	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	public int getNumAssertions(Chromosome individual) {
		int numAssertions =  getNumAssertions(individual, true);
		
		if (numAssertions > 0) {
			logger.warn("num assertions bigger than 0");
			RegressionTestSuiteChromosome clone = new RegressionTestSuiteChromosome();

			List<TestCase> testCases = new ArrayList<TestCase>();
			
			if(individual instanceof RegressionTestChromosome)
			{
				//clone.addTest((RegressionTestChromosome)individual);
				testCases.add(((RegressionTestChromosome)individual).getTheTest().getTestCase());
				
			} else {
				RegressionTestSuiteChromosome ind = (RegressionTestSuiteChromosome) individual;
				testCases.addAll(ind.getTests());
				/*for (RegressionTestChromosome regressionTest : ind.getTestChromosomes()) {

					clone.addTest(regressionTest);
				}*/
			}
			logger.warn("tests are copied");
			//List<TestCase> testCases = clone.getTests();
			numAssertions = 0;
			logger.warn("checking if compilable ...");
			
			//boolean compilable = JUnitAnalyzer.verifyCompilationAndExecution(testCases);
			if(true){
				logger.warn("yep, it was");
				JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);
				logger.warn("... removeTestsThatDoNotCompile()");
				JUnitAnalyzer.handleTestsThatAreUnstable(testCases);
				logger.warn("... handleTestsThatAreUnstable()");
				if(testCases.size()>0){
					logger.warn("{} tests remaining now!", testCases.size());
					clone = new RegressionTestSuiteChromosome();
					
					for(TestCase t: testCases){
					//	logger.warn("adding cloned test ...");
						RegressionTestChromosome rtc = new RegressionTestChromosome();
						TestChromosome tc = new TestChromosome();
						tc.setTestCase(t);
						rtc.setTest(tc);
						clone.addTest(rtc);
					}
					//test.set
					//clone.addTest(testCases);
					 logger.warn("getting new num assertions ...");
					numAssertions = RegressionSearchListener.getNumAssertions(
							clone, false );
					logger.warn("Keeping {} assertions.", numAssertions);

				} else {
					logger.warn("ignored assertions. tests were removed.");
				}
			} else {
				logger.warn("ignored assertions. not compilable.");
			}
		}
		
		return numAssertions;
		
	}

	public static int getNumAssertions(Chromosome individual,
			Boolean removeAssertions) {
		return getNumAssertions(individual, removeAssertions, false);

	}

	public static int getNumAssertions(Chromosome individual,
			Boolean removeAssertions, Boolean noExecution) {
		long startTime = System.nanoTime();
		RegressionAssertionGenerator rgen = new RegressionAssertionGenerator();
		int oldTimeout = Properties.TIMEOUT;
		Properties.TIMEOUT *= 20;
		int totalCount = 0;
		exceptionDiff = 0;

		boolean timedOut = false;

		logger.debug("Running assertion generator...");

		//RegressionTestSuiteChromosome ind = null;
		
		previousTestSuite = new ArrayList<TestCase>();
		previousTestSuite.addAll(currentTestSuite);
		currentTestSuite.clear();
		if(individual instanceof RegressionTestChromosome)
		{
			totalCount += checkForAssertions(removeAssertions, noExecution,
					rgen, (RegressionTestChromosome)individual);
		} else {
		//	assert false;
			RegressionTestSuiteChromosome ind = (RegressionTestSuiteChromosome) individual;
			for (RegressionTestChromosome regressionTest : ind.getTestChromosomes()) {

				totalCount += checkForAssertions(removeAssertions, noExecution,
						rgen, regressionTest);
			}
		}
		
		// if(totalCount>0)
		Properties.TIMEOUT = oldTimeout;
		logger.warn("Assertions generated for the individual: " + totalCount);
		RegressionSearchListener.assertionTime += System.nanoTime() - startTime;
		return totalCount;
	}
	public static boolean enable_a = false;

	private static int checkForAssertions(Boolean removeAssertions,
			Boolean noExecution, RegressionAssertionGenerator rgen,
			RegressionTestChromosome regressionTest) {
		long execStartTime = 0;
		long execEndTime = 0;
		int totalCount = 0;

		boolean timedOut;
		if (!noExecution) {
			 execStartTime = System.currentTimeMillis();
			 
			ExecutionResult result1 = rgen.runTest(regressionTest
					.getTheTest().getTestCase());
			//enable_a=true;
//logger.warn("Fitness is: {}", regressionTest.getFitness());
//rgen = new RegressionAssertionGenerator();
			ExecutionResult result2 = rgen.runTest(regressionTest
					.getTheSameTestForTheOtherClassLoader().getTestCase());
//			enable_a = false;
			 execEndTime = System.currentTimeMillis();
			 /*if((execEndTime-execStartTime)>1500)
					assert false;*/
			 
			
			if (result1.test == null || result2.test == null
					|| result1.hasTimeout() || result2.hasTimeout()) {

				logger.warn("================================== HAD TIMEOUT ==================================");
				timedOut = true;
				//assert false;
			} else {
				
				
				Map<Integer, Throwable> originalExceptionMapping = result1
						.getCopyOfExceptionMapping();
				Map<Integer, Throwable> regressionExceptionMapping = result2
						.getCopyOfExceptionMapping();

				double exDiff = Math
						.abs((double) (originalExceptionMapping.size() - regressionExceptionMapping
								.size()));

				if (exDiff == 0) {
					for (Entry<Integer, Throwable> origException : originalExceptionMapping
							.entrySet()) {
						boolean skip = false;

						if (origException.getValue() == null
								|| origException.getValue().getMessage() == null) {
							originalExceptionMapping.remove(origException.getKey());
							skip = true;
						}
						if (regressionExceptionMapping.containsKey(origException
								.getKey())
								&& (regressionExceptionMapping.get(origException
										.getKey()) == null || regressionExceptionMapping
										.get(origException.getKey()).getMessage() == null)) {
							regressionExceptionMapping.remove(origException
									.getKey());
							skip = true;
						}
						if (skip)
							continue;
						if (!regressionExceptionMapping.containsKey(origException
								.getKey())
								|| (!regressionExceptionMapping
										.get(origException.getKey())
										.getMessage()
										.equals(origException.getValue()
												.getMessage()))) {
							exDiff++;
						}
					}
					for (Entry<Integer, Throwable> regException : regressionExceptionMapping
							.entrySet()) {
						if (!originalExceptionMapping.containsKey(regException
								.getKey()))
							exDiff++;
					}
				}

				if (exDiff > 0) {
					logger.warn("Had {} different exceptions! ({})", exDiff,
							totalCount);
					logger.warn("mapping1: {} | mapping 2: {}",
							result1.getCopyOfExceptionMapping(),
							result2.getCopyOfExceptionMapping());
				}

				totalCount += exDiff;
				exceptionDiff += exDiff;

				for(Entry<Integer, Throwable> origException: originalExceptionMapping.entrySet()){
					 if(!regressionExceptionMapping.containsKey(origException.getKey())){
						 logger.warn("Test with exception \"{}\" was: \n{}\n---------\nException:\n{}", origException.getValue().getMessage(), regressionTest.getTheTest().getTestCase(),origException.getValue().toString());
						 if(regressionTest.getTheTest().getTestCase().hasStatement(origException.getKey()) && !regressionTest.getTheTest().getTestCase().getStatement(origException.getKey()).getComment().contains("modified version")){
							 regressionTest.getTheTest().getTestCase().getStatement(origException.getKey()).addComment("EXCEPTION DIFF:\nThe modified version did not exhibit this exception:\n    " + origException.getValue().getMessage() + "\n");
							 //regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase().getStatement(origException.getKey()).addComment("EXCEPTION DIFF:\nThe modified version did not exhibit this exception:\n    " + origException.getValue().getMessage() + "\n");
						 }
					 } else {
						 if(!origException.getValue().getMessage().equals(regressionExceptionMapping.get(origException.getKey()).getMessage())){
							 if(regressionTest.getTheTest().getTestCase().hasStatement(origException.getKey()) && !regressionTest.getTheTest().getTestCase().getStatement(origException.getKey()).getComment().contains("EXCEPTION DIFF:"))
								 regressionTest.getTheTest().getTestCase().getStatement(origException.getKey()).addComment("EXCEPTION DIFF:\nDifferent Exceptions were thrown:\nOriginal Version:\n    " + origException.getValue().getMessage() + "\nModified Version:\n    " + regressionExceptionMapping.get(origException.getKey()).getMessage() + "\n");
						 }
						 // If both show the same error, pop the error from the regression exception, to get to a diff.
						 regressionExceptionMapping.remove(origException.getKey());
					 }
				 }
				 for(Entry<Integer, Throwable> regException: regressionExceptionMapping.entrySet()){
					 if(regressionTest.getTheTest().getTestCase().hasStatement(regException.getKey()) && !regressionTest.getTheTest().getTestCase().getStatement(regException.getKey()).getComment().contains("original version")){
						 logger.warn("Regression Test with exception \"{}\" was: \n{}\n---------\nException:\n{}", regException.getValue().getMessage(), regressionTest.getTheTest().getTestCase(),regException.getValue().toString());
						 regressionTest.getTheTest().getTestCase().getStatement(regException.getKey()).addComment("EXCEPTION DIFF:\nThe original version did not exhibit this exception:\n    " + regException.getValue().getMessage()+ "\n\n");
						 regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase().getStatement(regException.getKey()).addComment("EXCEPTION DIFF:\nThe original version did not exhibit this exception:\n    " + regException.getValue().getMessage()+ "\n\n");
					 }
				 }
				
				
				//if(result1.hasTestException() || result2.hasTestException() || result1.hasUndeclaredException() || result2.hasUndeclaredException())
				//	logger.warn("================================== HAD EXCEPTION ==================================");

				for (Class<?> observerClass : RegressionAssertionGenerator.observerClasses) {
					if (result1.getTrace(observerClass) != null) {
						result1.getTrace(observerClass).getAssertions(
								regressionTest.getTheTest().getTestCase(),
								result2.getTrace(observerClass));
					}

				}

			}

		}
		int assertionCount = regressionTest.getTheTest().getTestCase()
				.getAssertions().size();
		totalCount += assertionCount;
		
		if(assertionCount>0){
			List<Assertion> asses = regressionTest.getTheTest().getTestCase()
			.getAssertions();
			for(Assertion ass:asses)
				logger.warn("+++++ Assertion code: " + ass.getCode());
			
			if(asses.size()==0)
				logger.warn("=========> NO ASSERTIONS!!!");
			else
				logger.warn("Assertions ^^^^^^^^^");
		}

		currentTestSuite.add(regressionTest.getTheTest().getTestCase()
				.clone());

		if (removeAssertions)
			regressionTest.getTheTest().getTestCase().removeAssertions();
		return totalCount;
	}

}
