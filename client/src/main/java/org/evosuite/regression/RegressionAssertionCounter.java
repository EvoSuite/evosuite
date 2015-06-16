package org.evosuite.regression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.ga.Chromosome;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegressionAssertionCounter {
	protected static final Logger logger = LoggerFactory
			.getLogger(RegressionAssertionCounter.class);
	
	private static List<List<String>> assertionComments = new ArrayList<List<String>>();

	/*
	 * Gets and removes the number of assertions for the individual
	 */
	public static int getNumAssertions(Chromosome individual) {
		assertionComments.clear();
		int numAssertions = getNumAssertions(individual, true);
		int oldNumAssertions = numAssertions;

		if (numAssertions > 0) {
			logger.warn("num assertions bigger than 0");
			RegressionTestSuiteChromosome clone = new RegressionTestSuiteChromosome();

			List<TestCase> testCases = new ArrayList<TestCase>();

			if (individual instanceof RegressionTestChromosome) {
				// clone.addTest((RegressionTestChromosome)individual);
				testCases.add(((RegressionTestChromosome) individual)
						.getTheTest().getTestCase());

			} else {
				RegressionTestSuiteChromosome ind = (RegressionTestSuiteChromosome) individual;
				testCases.addAll(ind.getTests());
				/*
				 * for (RegressionTestChromosome regressionTest :
				 * ind.getTestChromosomes()) {
				 * 
				 * clone.addTest(regressionTest); }
				 */
			}
			logger.warn("tests are copied");
			// List<TestCase> testCases = clone.getTests();
			numAssertions = 0;
			//logger.warn("checking if compilable ...");

			// boolean compilable =
			// JUnitAnalyzer.verifyCompilationAndExecution(testCases);
			if (true) {
				
				//logger.warn("yep, it was");
				JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);
				logger.warn("... removeTestsThatDoNotCompile()");
				int numUnstable = JUnitAnalyzer.handleTestsThatAreUnstable(testCases);
				
				logger.warn("... handleTestsThatAreUnstable() = {}", numUnstable);
				if (testCases.size() > 0) {
					logger.warn("{} tests remaining now!", testCases.size());
					clone = new RegressionTestSuiteChromosome();

					for (TestCase t : testCases) {
						// logger.warn("adding cloned test ...");
						if(t.isUnstable())
							continue;
						RegressionTestChromosome rtc = new RegressionTestChromosome();
						TestChromosome tc = new TestChromosome();
						tc.setTestCase(t);
						rtc.setTest(tc);
						clone.addTest(rtc);
					}
					// test.set
					// clone.addTest(testCases);
					logger.warn("getting new num assertions ...");
					List<List<String>> oldAssertionComments = new ArrayList<List<String>>(assertionComments);
					assertionComments.clear();
					numAssertions = getNumAssertions(clone, false);
					if(oldAssertionComments.size()!=assertionComments.size())
						numAssertions=0;
					else
						for(int i=0; i<oldAssertionComments.size();i++){
							List<String> testAssertionCommentsOld = oldAssertionComments.get(i);
							List<String> testAssertionCommentsNew = assertionComments.get(i);
							
							if(testAssertionCommentsNew.size()!= testAssertionCommentsOld.size()){
								numAssertions=0;
								break;
							}
							
							for(int j=0; j<testAssertionCommentsOld.size(); j++){
								if(!testAssertionCommentsOld.get(j).equals(testAssertionCommentsNew.get(j))){
									numAssertions=0;
									break;
								}
							}
						}
					
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
		
		//(Hack) temporarily changing timeout to allow the assertions to run
		int oldTimeout = Properties.TIMEOUT;
		Properties.TIMEOUT *= 20;
		int totalCount = 0;
		RegressionSearchListener.exceptionDiff = 0;

		boolean timedOut = false;

		logger.debug("Running assertion generator...");

		// RegressionTestSuiteChromosome ind = null;

		RegressionSearchListener.previousTestSuite = new ArrayList<TestCase>();
		RegressionSearchListener.previousTestSuite
				.addAll(RegressionSearchListener.currentTestSuite);
		RegressionSearchListener.currentTestSuite.clear();
		if (individual instanceof RegressionTestChromosome) {
			totalCount += checkForAssertions(removeAssertions, noExecution,
					rgen, (RegressionTestChromosome) individual);
		} else {
			// assert false;
			RegressionTestSuiteChromosome ind = (RegressionTestSuiteChromosome) individual;
			for (RegressionTestChromosome regressionTest : ind
					.getTestChromosomes()) {

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

	// public static boolean enable_a = false;

	private static int checkForAssertions(Boolean removeAssertions,
			Boolean noExecution, RegressionAssertionGenerator rgen,
			RegressionTestChromosome regressionTest) {
		long execStartTime = 0;
		long execEndTime = 0;
		int totalCount = 0;

		boolean timedOut;
		if (!noExecution) {
			execStartTime = System.currentTimeMillis();

			ExecutionResult result1 = rgen.runTest(regressionTest.getTheTest()
					.getTestCase());
			// enable_a=true;
			// logger.warn("Fitness is: {}", regressionTest.getFitness());
			// rgen = new RegressionAssertionGenerator();
			ExecutionResult result2 = rgen.runTest(regressionTest
					.getTheSameTestForTheOtherClassLoader().getTestCase());
			// enable_a = false;
			execEndTime = System.currentTimeMillis();
			/*
			 * if((execEndTime-execStartTime)>1500) assert false;
			 */

			if (result1.test == null || result2.test == null
					|| result1.hasTimeout() || result2.hasTimeout()) {

				logger.warn("================================== HAD TIMEOUT ==================================");
				timedOut = true;
				// assert false;
			} else {

				Map<Integer, Throwable> originalExceptionMapping = result1
						.getCopyOfExceptionMapping();
				Map<Integer, Throwable> regressionExceptionMapping = result2
						.getCopyOfExceptionMapping();

				double exDiff = Math.abs((double) (originalExceptionMapping
						.size() - regressionExceptionMapping.size()));

				if (exDiff == 0) {
					for (Entry<Integer, Throwable> origException : originalExceptionMapping
							.entrySet()) {
						boolean skip = false;

						if (origException.getValue() == null
								|| origException.getValue().getMessage() == null) {
							originalExceptionMapping.remove(origException
									.getKey());
							skip = true;
						}
						if (regressionExceptionMapping
								.containsKey(origException.getKey())
								&& (regressionExceptionMapping
										.get(origException.getKey()) == null || regressionExceptionMapping
										.get(origException.getKey())
										.getMessage() == null)) {
							regressionExceptionMapping.remove(origException
									.getKey());
							skip = true;
						}
						if (skip)
							continue;
						if (!regressionExceptionMapping
								.containsKey(origException.getKey())
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
				RegressionSearchListener.exceptionDiff += exDiff;

				// The following ugly code adds exception diff comments
				addExceptionAssertions(regressionTest,
						originalExceptionMapping, regressionExceptionMapping);

				// if(result1.hasTestException() || result2.hasTestException()
				// || result1.hasUndeclaredException() ||
				// result2.hasUndeclaredException())
				// logger.warn("================================== HAD EXCEPTION ==================================");

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

		if (assertionCount > 0) {
			List<Assertion> asses = regressionTest.getTheTest().getTestCase()
					.getAssertions();
			List<String> assComments = new ArrayList<String>();
			for (Assertion ass : asses){
				logger.warn("+++++ Assertion code: " + ass.getCode());
				assComments.add(ass.getComment());
			}
			RegressionAssertionCounter.assertionComments.add(assComments);

			if (asses.size() == 0)
				logger.warn("=========> NO ASSERTIONS!!!");
			else
				logger.warn("Assertions ^^^^^^^^^");
		}

		RegressionSearchListener.currentTestSuite.add(regressionTest
				.getTheTest().getTestCase().clone());

		if (removeAssertions)
			regressionTest.getTheTest().getTestCase().removeAssertions();
		return totalCount;
	}

	private static void addExceptionAssertions(
			RegressionTestChromosome regressionTest,
			Map<Integer, Throwable> originalExceptionMapping,
			Map<Integer, Throwable> regressionExceptionMapping) {
		for (Entry<Integer, Throwable> origException : originalExceptionMapping
				.entrySet()) {
			if (!regressionExceptionMapping.containsKey(origException
					.getKey())) {
				logger.warn(
						"Test with exception \"{}\" was: \n{}\n---------\nException:\n{}",
						origException.getValue().getMessage(),
						regressionTest.getTheTest().getTestCase(),
						origException.getValue().toString());
				if (regressionTest.getTheTest().getTestCase()
						.hasStatement(origException.getKey())
						&& !regressionTest.getTheTest().getTestCase()
								.getStatement(origException.getKey())
								.getComment()
								.contains("modified version")) {
					regressionTest
							.getTheTest()
							.getTestCase()
							.getStatement(origException.getKey())
							.addComment(
									"EXCEPTION DIFF:\nThe modified version did not exhibit this exception:\n    "
											+ origException.getValue()
													.getMessage()
											+ "\n");
					// regressionTest.getTheSameTestForTheOtherClassLoader().getTestCase().getStatement(origException.getKey()).addComment("EXCEPTION DIFF:\nThe modified version did not exhibit this exception:\n    "
					// + origException.getValue().getMessage() + "\n");
				}
			} else {
				if (!origException
						.getValue()
						.getMessage()
						.equals(regressionExceptionMapping.get(
								origException.getKey()).getMessage())) {
					if (regressionTest.getTheTest().getTestCase()
							.hasStatement(origException.getKey())
							&& !regressionTest
									.getTheTest()
									.getTestCase()
									.getStatement(
											origException.getKey())
									.getComment()
									.contains("EXCEPTION DIFF:"))
						regressionTest
								.getTheTest()
								.getTestCase()
								.getStatement(origException.getKey())
								.addComment(
										"EXCEPTION DIFF:\nDifferent Exceptions were thrown:\nOriginal Version:\n    "
												+ origException
														.getValue()
														.getMessage()
												+ "\nModified Version:\n    "
												+ regressionExceptionMapping
														.get(origException
																.getKey())
														.getMessage()
												+ "\n");
				}
				// If both show the same error, pop the error from the
				// regression exception, to get to a diff.
				regressionExceptionMapping.remove(origException
						.getKey());
			}
		}
		for (Entry<Integer, Throwable> regException : regressionExceptionMapping
				.entrySet()) {
			if (regressionTest.getTheTest().getTestCase()
					.hasStatement(regException.getKey())
					&& !regressionTest.getTheTest().getTestCase()
							.getStatement(regException.getKey())
							.getComment().contains("original version")) {
				logger.warn(
						"Regression Test with exception \"{}\" was: \n{}\n---------\nException:\n{}",
						regException.getValue().getMessage(),
						regressionTest.getTheTest().getTestCase(),
						regException.getValue().toString());
				regressionTest
						.getTheTest()
						.getTestCase()
						.getStatement(regException.getKey())
						.addComment(
								"EXCEPTION DIFF:\nThe original version did not exhibit this exception:\n    "
										+ regException.getValue()
												.getMessage() + "\n\n");
				regressionTest
						.getTheSameTestForTheOtherClassLoader()
						.getTestCase()
						.getStatement(regException.getKey())
						.addComment(
								"EXCEPTION DIFF:\nThe original version did not exhibit this exception:\n    "
										+ regException.getValue()
												.getMessage() + "\n\n");
			}
		}
	}

}
