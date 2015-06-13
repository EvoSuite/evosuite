package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.execution.UncompilableCodeException;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedNumRandomTestStrategy extends TestGenerationStrategy {

	private final static Logger logger = LoggerFactory.getLogger(FixedNumRandomTestStrategy.class);
	
	@Override
	public TestSuiteChromosome generateTests() {
		LoggingUtils.getEvoLogger().info("* Generating fixed number of random tests");
		RandomLengthTestFactory factory = new org.evosuite.testcase.factories.RandomLengthTestFactory();
		TestSuiteChromosome suite = new TestSuiteChromosome();
		// The GA is not actually used, except to provide the same statistics as
		// during search
		//GeneticAlgorithm<TestSuiteChromosome> suiteGA = getGeneticAlgorithm(new TestSuiteChromosomeFactory());
		// GeneticAlgorithm suiteGA = setup();
		//stopping_condition = getStoppingCondition();
		//statistics.searchStarted(suiteGA);

		for (int i = 0; i < Properties.NUM_RANDOM_TESTS; i++) {
			logger.info("Current test: " + i + "/" + Properties.NUM_RANDOM_TESTS);
			TestChromosome test = factory.getChromosome();
			ExecutionResult result = TestCaseExecutor.runTest(test.getTestCase());
			Integer pos = result.getFirstPositionOfThrownException();
			if (pos != null) {
				if (result.getExceptionThrownAtPosition(pos) instanceof CodeUnderTestException
				        || result.getExceptionThrownAtPosition(pos) instanceof UncompilableCodeException
				        || result.getExceptionThrownAtPosition(pos) instanceof TestCaseExecutor.TimeoutExceeded) {
					continue;
					// test.getTestCase().chop(pos);
				} else {
					test.getTestCase().chop(pos + 1);
				}
				test.setChanged(true);
			} else {
				test.setLastExecutionResult(result);
			}
			suite.addTest(test);
		}
        // Search is finished, send statistics
        sendExecutionStatistics();

		return suite;
	}

}
