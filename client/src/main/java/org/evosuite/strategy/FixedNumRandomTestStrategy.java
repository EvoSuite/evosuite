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

import org.evosuite.Properties;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
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

/**
 * This strategy consists of generating random tests.
 * The property NUM_RANDOM_TESTS is set on the command line
 * 
 * @author gordon
 *
 */
public class FixedNumRandomTestStrategy extends TestGenerationStrategy {

	private final static Logger logger = LoggerFactory.getLogger(FixedNumRandomTestStrategy.class);
	
	@Override
	public TestSuiteChromosome generateTests() {
		LoggingUtils.getEvoLogger().info("* Generating fixed number of random tests");
		RandomLengthTestFactory factory = new org.evosuite.testcase.factories.RandomLengthTestFactory();
		TestSuiteChromosome suite = new TestSuiteChromosome();
		if(!canGenerateTestsForSUT()) {
			LoggingUtils.getEvoLogger().info("* Found no testable methods in the target class "
					+ Properties.TARGET_CLASS);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, 0);
			return suite;
		}
		for (int i = 0; i < Properties.NUM_RANDOM_TESTS; i++) {
			logger.info("Current test: " + i + "/" + Properties.NUM_RANDOM_TESTS);
			TestChromosome test = factory.getChromosome();
			ExecutionResult result = TestCaseExecutor.runTest(test.getTestCase());
			Integer pos = result.getFirstPositionOfThrownException();
			if (pos != null) {
				if (result.getExceptionThrownAtPosition(pos) instanceof CodeUnderTestException
				        || result.getExceptionThrownAtPosition(pos) instanceof UncompilableCodeException
				        || result.getExceptionThrownAtPosition(pos) instanceof TestCaseExecutor.TimeoutExceeded) {
					// Filter invalid tests 
					continue;
				} else {
					// Remove anything that follows an exception
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
