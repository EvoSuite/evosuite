package org.evosuite.regression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

		RegressionTestSuiteChromosome regressionSuite = new RegressionTestSuiteChromosome();
		regressionSuite.addTests(suite.getTestChromosomes());

		// seems to be broken.
		// removeUnusedVariables(regressionSuite);

		executeSuite(regressionSuite);

		removeDuplicateAssertions(regressionSuite);

		removePassingTests(regressionSuite);

		minimizeSuite(regressionSuite);
		
		sendStats(regressionSuite);

		// Adding tests back to the original test suite
		suite.clearTests();
		for (TestChromosome t : regressionSuite.getTestChromosomes()) {
			RegressionTestChromosome rtc = (RegressionTestChromosome) t;
			suite.addTest(rtc.getTheTest());
		}
	}

	private void sendStats(RegressionTestSuiteChromosome regressionSuite) {
		int assCount = 0;
		for (TestChromosome chromosome : regressionSuite.getTestChromosomes()) {
			RegressionTestChromosome c = (RegressionTestChromosome) chromosome;
			assCount += c.getTheTest().getTestCase().getAssertions().size();
		}
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Generated_Assertions, assCount);
		
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
		while (it.hasNext()) {
			RegressionTestChromosome test = (RegressionTestChromosome) it
					.next();
			boolean changed = false;
			Iterator<Assertion> assertions = test.getTheTest().getTestCase()
					.getAssertions().iterator();
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
			}
			
			//TODO: unique exceptions
			/*ExecutionResult resultA = test.getLastExecutionResult();
			ExecutionResult resultB = test.getLastRegressionExecutionResult();

			double exDiff = RegressionAssertionCounter.compareExceptionDiffs(
					resultA.getCopyOfExceptionMapping(),
					resultB.getCopyOfExceptionMapping());
			
			if(exDiff>0){
				Iterator<Throwable> throwables =resultA.getCopyOfExceptionMapping().values().iterator();
				while(throwables.hasNext()){
					Throwable t = throwables.next();
					
				}
			}*/
			
			if (changed) {
				test.updateClassloader();
				executeTest(test);
			}
		}
		if (uniqueAssertions.size() > 0)
			logger.warn("unique assertions: {}", uniqueAssertions);
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
		
		test.assertionCount = count;

		count += (int) RegressionAssertionCounter.compareExceptionDiffs(
				resultA.getCopyOfExceptionMapping(),
				resultB.getCopyOfExceptionMapping());
		// logger.warn("{} assertions", count);
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
				logger.warn("Removing test {}: no assertions", i);
				it.remove();
			}
		}
	}

	private void minimizeSuite(RegressionTestSuiteChromosome suite) {
		Iterator<TestChromosome> it = suite.getTestChromosomes().iterator();
		while (it.hasNext()) {
			if (isTimeoutReached())
				break;

			RegressionTestChromosome test = (RegressionTestChromosome) it
					.next();

			for (int i = test.getTheTest().size() - 1; i >= 0; i--) {
				if (isTimeoutReached())
					break;

				logger.debug("Current size: " + suite.size() + "/"
						+ suite.totalLengthOfTestCases());
				logger.debug("Deleting statement "
						+ test.getTheTest().getTestCase().getStatement(i)
								.getCode() + " from test");
				TestChromosome originalTestChromosome = (TestChromosome) test
						.getTheTest().clone();

				executeTest(test);
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
				rtc.setTest(test.getTheTest());
				rtc.updateClassloader();

				executeTest(rtc);

				int postRemovalAssertions = numFailingAssertions(rtc);

				if (postRemovalAssertions == preRemovalAssertions) {
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
