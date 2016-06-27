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
package org.evosuite.regression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.TimeController;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.assertion.OutputTrace;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCaseMinimizer;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegressionSuiteMinimizer {

	private final static Logger logger = LoggerFactory
			.getLogger(RegressionSuiteMinimizer.class);

	RegressionAssertionGenerator rgen = new RegressionAssertionGenerator();

	public void minimize(TestSuiteChromosome suite) {
		ClientServices.getInstance().getClientNode()
				.trackOutputVariable(RuntimeVariable.Result_Size, suite.size());
		ClientServices
				.getInstance()
				.getClientNode()
				.trackOutputVariable(RuntimeVariable.Result_Length,
						suite.totalLengthOfTestCases());
		ClientServices.getInstance().getClientNode()
				.trackOutputVariable(RuntimeVariable.RSM_OverMinimized, 0);

		logger.warn("Going to minimize test suite. Length: {} ",
				suite.totalLengthOfTestCases());

		RegressionTestSuiteChromosome regressionSuite = new RegressionTestSuiteChromosome();
		regressionSuite.addTests(suite.getTestChromosomes());

		// Seems to be broken:
		// removeUnusedVariables(regressionSuite);

		executeSuite(regressionSuite);

		removeDuplicateAssertions(regressionSuite);

		removeDuplicateExceptions(regressionSuite);

		removePassingTests(regressionSuite);

		int testCount = regressionSuite.size();

		minimizeSuite(regressionSuite);

		executeSuite(regressionSuite);

		removePassingTests(regressionSuite);

		sendStats(regressionSuite);

		// Sanity check
		if (regressionSuite.size() == 0 && testCount > 0){
			ClientServices.getInstance().getClientNode()
					.trackOutputVariable(RuntimeVariable.RSM_OverMinimized, 1);
		} else {
    		// Adding tests back to the original test suite (if minimization didn't remove all tests)
    		suite.clearTests();
    		for (TestChromosome t : regressionSuite.getTestChromosomes()) {
    			RegressionTestChromosome rtc = (RegressionTestChromosome) t;
    			suite.addTest(rtc.getTheTest());
    		}
		}

		logger.warn("Minimized Length: {} ", suite.totalLengthOfTestCases());
	}

	private void sendStats(RegressionTestSuiteChromosome regressionSuite) {
		int assCount = 0;
		/*
		 * int i=0; for (TestChromosome c :
		 * regressionSuite.getTestChromosomes()) { RegressionTestChromosome test
		 * = (RegressionTestChromosome) c; assCount += test.assertionCount;
		 * if(test.exAssertionCount==0) continue;
		 * logger.warn("adding exception comment for test{}:\n{}"
		 * ,i,test.getTheTest()); //logger.wran("test{}") ExecutionResult
		 * resultA = test.getLastExecutionResult(); ExecutionResult resultB =
		 * test.getLastRegressionExecutionResult();
		 * logger.warn("map1:\n{}map2:{}\n",resultA.getCopyOfExceptionMapping(),
		 * resultB.getCopyOfExceptionMapping());
		 * RegressionAssertionCounter.addExceptionAssertionComments(test,
		 * resultA.getCopyOfExceptionMapping(),
		 * resultB.getCopyOfExceptionMapping()); i++; }
		 */
		assCount = numFailingAssertions(regressionSuite);
		ClientServices
				.getInstance()
				.getClientNode()
				.trackOutputVariable(RuntimeVariable.Generated_Assertions,
						assCount);

		ClientServices
				.getInstance()
				.getClientNode()
				.trackOutputVariable(RuntimeVariable.Minimized_Size,
						regressionSuite.size());
		ClientServices
				.getInstance()
				.getClientNode()
				.trackOutputVariable(RuntimeVariable.Minimized_Length,
						regressionSuite.totalLengthOfTestCases());

		RegressionSearchListener.flushLastLine(assCount,
				regressionSuite.size(),
				regressionSuite.totalLengthOfTestCases());
	}

	private void executeSuite(RegressionTestSuiteChromosome regressionSuite) {
		for (TestChromosome chromosome : regressionSuite.getTestChromosomes()) {
			RegressionTestChromosome c = (RegressionTestChromosome) chromosome;
			try {
				executeTest(c);
			} catch (Throwable t) {
				logger.error("could not execute tets");
				t.printStackTrace();
			}
		}
	}

	private void executeTest(RegressionTestChromosome regressionTest) {
		TestChromosome testChromosome = regressionTest.getTheTest();
		TestChromosome otherChromosome = regressionTest
				.getTheSameTestForTheOtherClassLoader();

		ExecutionResult result = rgen.runTest(testChromosome.getTestCase());
		ExecutionResult otherResult = rgen.runTest(otherChromosome
				.getTestCase());

		regressionTest.setLastExecutionResult(result);
		regressionTest.setLastRegressionExecutionResult(otherResult);

		testChromosome.setLastExecutionResult(result);
		otherChromosome.setLastExecutionResult(otherResult);
	}

	private void removeDuplicateAssertions(RegressionTestSuiteChromosome suite) {
		Iterator<TestChromosome> it = suite.getTestChromosomes().iterator();
		Map<String, List<String>> uniqueAssertions = new HashMap<String, List<String>>();
		// int i = -1;
		while (it.hasNext()) {
			// i++;
			RegressionTestChromosome test = (RegressionTestChromosome) it
					.next();
			boolean changed = false;
			boolean hadAssertion = false;
			Iterator<Assertion> assertions = test.getTheTest().getTestCase()
					.getAssertions().iterator();
			// keep track of new unique assertions, and if not unique, remove
			// the assertion
			while (assertions.hasNext()) {
				Assertion a = assertions.next();
				String aClass = a.getClass().getSimpleName();
				List<String> aTypes = uniqueAssertions.get(aClass);
				if (aTypes == null)
					aTypes = new ArrayList<String>();
				String aType = "";
				if (a instanceof InspectorAssertion) {
					InspectorAssertion ia = (InspectorAssertion) a;
					try {
						aType = ia.getInspector().getMethod().getName();
					} catch (NullPointerException e) {
						// technically this should not happen
						Statement s = ia.getStatement();
						if (s instanceof MethodStatement) {
							aType = ((MethodStatement) s).getMethod().getName();
						}
					}
				}
				if (aTypes.contains(aType)) {
					// logger.warn("removing non-unique assertion: {}-{}",
					// aClass, aType);
					changed = true;
					a.getStatement().getPosition();
					test.getTheTest().getTestCase().removeAssertion(a);
					continue;
				}
				aTypes.add(aType);
				uniqueAssertions.put(aClass, aTypes);
				hadAssertion = true;
			}

			if (changed) {
				test.updateClassloader();
			}
		}
		if (uniqueAssertions.size() > 0)
			logger.warn("unique assertions: {}", uniqueAssertions);
	}

	private void removeDuplicateExceptions(RegressionTestSuiteChromosome suite) {

		Set<String> uniqueExceptions = new HashSet<String>();
		Map<String, Integer> exceptionStatementMapping = new HashMap<String, Integer>();
		List<TestChromosome> chromosomes = suite.getTestChromosomes();

		for (int i = 0; i < chromosomes.size(); i++) {

			RegressionTestChromosome test = (RegressionTestChromosome) chromosomes
					.get(i);

			boolean changed = false;
			boolean hadAssertion = test.getTheTest().getTestCase()
					.getAssertions().size() > 0;

			ExecutionResult resultA = test.getLastExecutionResult();
			ExecutionResult resultB = test.getLastRegressionExecutionResult();

			if (resultA == null || resultB == null) {
				executeTest(test);
				if (resultA == null || resultB == null)
					continue;
			}

			Map<Integer, Throwable> exceptionMapA = resultA
					.getCopyOfExceptionMapping();
			Map<Integer, Throwable> exceptionMapB = resultB
					.getCopyOfExceptionMapping();

			// logger.warn("Test{} - had exceptions? {} {} - {} {}",i,
			// resultA.noThrownExceptions(), resultB.noThrownExceptions(),
			// exceptionMapA.size(), exceptionMapB.size());
			if (!resultA.noThrownExceptions() || !resultB.noThrownExceptions()) {
				double exDiff = RegressionAssertionCounter
						.compareExceptionDiffs(exceptionMapA, exceptionMapB);
				logger.warn("Test{} - Exdiff: {}", i, exDiff);
				if (exDiff > 0) {
					/*
					 * Three scenarios: 1. Same exception, different messages 2.
					 * Different exception in A 3. Different exception in B
					 */
					for (Entry<Integer, Throwable> ex : exceptionMapA
							.entrySet()) {
						String exception = simpleExceptionName(test,
								ex.getKey(), ex.getValue());

						Throwable exB = exceptionMapB.get(ex.getKey());
						if (exB != null) {
							String exceptionB = simpleExceptionName(test,
									ex.getKey(), exB);

							if (exception.equals(exceptionB))
								exceptionMapB.remove(ex.getKey());
						}
						logger.warn("Test{}, uniqueExceptions: {}", i,
								uniqueExceptions);
						logger.warn("checking exception: {} at {}", exception,
								ex.getKey());

						if (uniqueExceptions.contains(exception)
								&& exceptionStatementMapping.get(exception) != ex
										.getKey() && !hadAssertion) {
							TestChromosome originalTestChromosome = (TestChromosome) test
									.getTheTest().clone();
							try {
								TestFactory testFactory = TestFactory
										.getInstance();
								testFactory.deleteStatementGracefully(test
										.getTheTest().getTestCase(), ex
										.getKey());
								test.getTheTest().setChanged(true);
								logger.warn(
										"removed exception throwing line {}",
										ex.getKey());
							} catch (ConstructionFailedException e) {
								test.getTheTest().setChanged(false);
								test.getTheTest().setTestCase(
										originalTestChromosome.getTestCase());
								logger.error("Deleting failed");
								continue;
							}
							changed = true;
						} else {
							uniqueExceptions.add(exception);
							exceptionStatementMapping.put(exception,
									ex.getKey());
						}
					}

					for (Entry<Integer, Throwable> ex : exceptionMapB
							.entrySet()) {
						String exception = simpleExceptionName(test,
								ex.getKey(), ex.getValue());

						logger.warn("Test{}, uniqueExceptions: {}", i,
								uniqueExceptions);
						logger.warn("checking exceptionB: {} at {}", exception,
								ex.getKey());
						if (uniqueExceptions.contains(exception)
								&& exceptionStatementMapping.get(exception) != ex
										.getKey()
								&& !hadAssertion
								&& test.getTheTest().getTestCase()
										.hasStatement(ex.getKey())) {
							TestChromosome originalTestChromosome = (TestChromosome) test
									.getTheTest().clone();
							try {
								TestFactory testFactory = TestFactory
										.getInstance();
								logger.warn("removing statementB: {}", test
										.getTheTest().getTestCase()
										.getStatement(ex.getKey()));
								testFactory.deleteStatementGracefully(test
										.getTheTest().getTestCase(), ex
										.getKey());
								test.getTheTest().setChanged(true);
								logger.warn(
										"removed exceptionB throwing line {}",
										ex.getKey());
							} catch (ConstructionFailedException e) {
								test.getTheTest().setChanged(false);
								test.getTheTest().setTestCase(
										originalTestChromosome.getTestCase());
								logger.error("Deleting failed");
								continue;
							}
							changed = true;
						} else {
							uniqueExceptions.add(exception);
							exceptionStatementMapping.put(exception,
									ex.getKey());
						}
					}
				}
			}

			if (changed) {
				test.updateClassloader();
				executeTest(test);
				i--;
			}
		}

		if (uniqueExceptions.size() > 0)
			logger.warn("unique exceptions: {}", uniqueExceptions);
	}

	/**
	 * Get a simple (and unique looking) exception name
	 */
	public static String simpleExceptionName(RegressionTestChromosome test,
			Integer statementPos, Throwable ex) {
		if (ex == null)
			return "";
		String exception = ex.getClass().getSimpleName();
		if (test.getTheTest().getTestCase().hasStatement(statementPos)) {
			Statement exThrowingStatement = test.getTheTest().getTestCase()
					.getStatement(statementPos);
			if (exThrowingStatement instanceof MethodStatement) {
				String exMethodcall = ((MethodStatement) exThrowingStatement)
						.getMethod().getName();
				exception = exMethodcall + ":" + exception;
			}
		}
		return exception;
	}

	private int numFailingAssertions(RegressionTestSuiteChromosome suite) {

		int count = 0;

		Iterator<TestChromosome> it = suite.getTestChromosomes().iterator();
		while (it.hasNext()) {
			RegressionTestChromosome test = (RegressionTestChromosome) it
					.next();

			count += numFailingAssertions(test);
		}
		return count;
	}

	private int numFailingAssertions(RegressionTestChromosome test) {

		int count = 0;
		Set<Assertion> invalidAssertions = new HashSet<Assertion>();

		ExecutionResult resultA = test.getLastExecutionResult();
		ExecutionResult resultB = test.getLastRegressionExecutionResult();

		for (Assertion assertion : test.getTheSameTestForTheOtherClassLoader()
				.getTestCase().getAssertions()) {
			for (OutputTrace<?> outputTrace : resultA.getTraces()) {
				if (outputTrace.isDetectedBy(assertion)) {
					logger.error("shouldn't be happening: assertion was failing on original version");
					invalidAssertions.add(assertion);
					break;
				}
			}
			for (OutputTrace<?> outputTrace : resultB.getTraces()) {
				if (outputTrace.isDetectedBy(assertion)
						&& !invalidAssertions.contains(assertion)) {
					count++;
					break;
				}
			}
		}

		if (invalidAssertions.size() != 0) {
			logger.warn("{} invalid assertion(s) to be removed",
					invalidAssertions);
			for (Assertion assertion : invalidAssertions) {
				test.getTheTest().getTestCase().removeAssertion(assertion);
				test.getTheTest().setChanged(true);
				test.updateClassloader();
			}
		}

		int exDiffCount = 0;

		if (resultA != null && resultB != null) {
			exDiffCount = (int) RegressionAssertionCounter
					.compareExceptionDiffs(resultA.getCopyOfExceptionMapping(),
							resultB.getCopyOfExceptionMapping());
			// logger.warn("exDiffCount: {}: \nmap1:{}\nmap2:{}\n",exDiffCount,resultA.getCopyOfExceptionMapping(),
			// resultB.getCopyOfExceptionMapping());
			count += exDiffCount;
			// logger.warn("adding exception comment for test:\n{}",test.getTheTest());
			// logger.wran("test{}")

			if (exDiffCount > 0 && !test.exCommentsAdded) {
				// logger.warn("Adding Exception Comments for test: \nmap1:{}\nmap2:{}\n",resultA.getCopyOfExceptionMapping(),
				// resultB.getCopyOfExceptionMapping());
				RegressionAssertionCounter.addExceptionAssertionComments(test,
						resultA.getCopyOfExceptionMapping(),
						resultB.getCopyOfExceptionMapping());
				test.exCommentsAdded = true;
			}
		} else
			logger.error("resultA: {} | resultB: {}", resultA, resultB);
		// logger.warn("{} assertions", count);

		// test.assertionCount = count;
		// test.exAssertionCount = exDiffCount;

		return count;
	}

	private void removePassingTests(RegressionTestSuiteChromosome suite) {
		Iterator<TestChromosome> it = suite.getTestChromosomes().iterator();
		int i = 0;
		while (it.hasNext()) {
			i++;
			RegressionTestChromosome test = (RegressionTestChromosome) it
					.next();

			if (numFailingAssertions(test) == 0) {
				logger.debug("Removing test {}: no assertions", (i - 1));
				it.remove();
			}
		}
	}

	private void minimizeSuite(RegressionTestSuiteChromosome suite) {
		// logger.warn("minimizeSuite:\n{}", suite);
		Iterator<TestChromosome> it = suite.getTestChromosomes().iterator();
		int testCount = 0;
		while (it.hasNext()) {
			if (isTimeoutReached())
				break;
			// logger.warn("##########################   TEST{}   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%",
			// testCount);
			RegressionTestChromosome test = (RegressionTestChromosome) it
					.next();

			for (int i = test.getTheTest().size() - 1; i >= 0; i--) {
				if (isTimeoutReached())
					break;

				logger.debug("Current size: " + suite.size() + "/"
						+ suite.totalLengthOfTestCases());
				logger.debug("Deleting statement {} "
						+ test.getTheTest().getTestCase().getStatement(i)
								.getCode() + " from test", i);
				TestChromosome originalTestChromosome = (TestChromosome) test
						.getTheTest().clone();

				executeTest(test);
				/*
				 * if(test.getLastExecutionResult()==null ||
				 * test.getLastRegressionExecutionResult()==null){
				 * logger.error("test execution result was null"); //continue; }
				 */
				// originalTestChromosome.setLastExecutionResult(test.getLastExecutionResult());

				int preRemovalAssertions = numFailingAssertions(test);

				try {
					TestFactory testFactory = TestFactory.getInstance();
					testFactory.deleteStatementGracefully(test.getTheTest()
							.getTestCase(), i);
					test.getTheTest().setChanged(true);
				} catch (ConstructionFailedException e) {
					test.getTheTest().setChanged(false);
					test.getTheTest().setTestCase(
							originalTestChromosome.getTestCase());
					logger.error("Deleting failed");
					continue;
				}

				RegressionTestChromosome rtc = new RegressionTestChromosome();
				rtc.setTest((TestChromosome) test.getTheTest().clone());
				// rtc.updateClassloader();

				executeTest(rtc);

				int postRemovalAssertions = numFailingAssertions(rtc);
				// logger.warn("Pre-Removal Assertions: {} | Post-Removal Assertions: {}",
				// preRemovalAssertions, postRemovalAssertions);
				if (postRemovalAssertions == preRemovalAssertions) {
					test.updateClassloader();
					continue; // the change had no effect
				} else if (postRemovalAssertions != preRemovalAssertions) {
					// Restore previous state
					logger.debug("Can't remove statement "
							+ originalTestChromosome.getTestCase()
									.getStatement(i).getCode());
					test.getTheTest().setTestCase(
							originalTestChromosome.getTestCase());
					test.getTheTest().setLastExecutionResult(
							originalTestChromosome.getLastExecutionResult());
					test.getTheTest().setChanged(false);
				}
			}

			test.updateClassloader();
			if (test.getTheTest().isChanged()) {
				executeTest(test);
			}
			testCount++;
		}
	}

	/*
	 * "borrowed" from TestCaseMinimizer
	 */
	private void removeUnusedVariables(RegressionTestSuiteChromosome suite) {
		Iterator<TestChromosome> it = suite.getTestChromosomes().iterator();
		while (it.hasNext()) {
			RegressionTestChromosome test = (RegressionTestChromosome) it
					.next();
			boolean changed = TestCaseMinimizer.removeUnusedVariables(test
					.getTheTest().getTestCase());
			if (changed) {
				test.updateClassloader();
				executeSuite(suite);
			}

		}
	}

	/*
	 * "borrowed" from TestSuiteMinimizer
	 */
	private boolean isTimeoutReached() {
		return !TimeController.getInstance().isThereStillTimeInThisPhase();
	}

}
