/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.assertion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract AssertionGenerator class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class AssertionGenerator {

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(AssertionGenerator.class);

	/** Constant <code>primitive_observer</code> */
	protected static final PrimitiveTraceObserver primitiveObserver = new PrimitiveTraceObserver();

	/** Constant <code>comparison_observer</code> */
	protected static final ComparisonTraceObserver comparisonObserver = new ComparisonTraceObserver();

	/** Constant <code>same_observer</code> */
	protected static final SameTraceObserver sameObserver = new SameTraceObserver();

	/** Constant <code>inspector_observer</code> */
	protected static final InspectorTraceObserver inspectorObserver = new InspectorTraceObserver();

	/** Constant <code>field_observer</code> */
	protected static final PrimitiveFieldTraceObserver fieldObserver = new PrimitiveFieldTraceObserver();

	/** Constant <code>null_observer</code> */
	protected static final NullTraceObserver nullObserver = new NullTraceObserver();

	/** Constant <code>null_observer</code> */
	protected static final ArrayTraceObserver arrayObserver = new ArrayTraceObserver();

	/**
	 * <p>
	 * Constructor for AssertionGenerator.
	 * </p>
	 */
	public AssertionGenerator() {
		TestCaseExecutor.getInstance().addObserver(primitiveObserver);
		TestCaseExecutor.getInstance().addObserver(comparisonObserver);
		TestCaseExecutor.getInstance().addObserver(inspectorObserver);
		TestCaseExecutor.getInstance().addObserver(fieldObserver);
		TestCaseExecutor.getInstance().addObserver(nullObserver);
		TestCaseExecutor.getInstance().addObserver(sameObserver);
		TestCaseExecutor.getInstance().addObserver(arrayObserver);
	}

	/**
	 * <p>
	 * addAssertions
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public abstract void addAssertions(TestCase test);

	/**
	 * Add assertions to all tests in a test suite
	 * 
	 * @param suite
	 */
	public void addAssertions(TestSuiteChromosome suite) {
		for(TestChromosome test : suite.getTestChromosomes()) {
			addAssertions(test.getTestCase());
		}
	}
	
	/**
	 * Execute a test case on the original unit
	 * 
	 * @param test
	 *            The test case that should be executed
	 * @return a {@link org.evosuite.testcase.ExecutionResult} object.
	 */
	protected ExecutionResult runTest(TestCase test) {
		ExecutionResult result = new ExecutionResult(test);
		try {
			logger.debug("Executing test");
			result = TestCaseExecutor.getInstance().execute(test);
			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
			result.setTrace(comparisonObserver.getTrace(), ComparisonTraceEntry.class);
			result.setTrace(primitiveObserver.getTrace(), PrimitiveTraceEntry.class);
			result.setTrace(inspectorObserver.getTrace(), InspectorTraceEntry.class);
			result.setTrace(fieldObserver.getTrace(), PrimitiveFieldTraceEntry.class);
			result.setTrace(nullObserver.getTrace(), NullTraceEntry.class);
			result.setTrace(sameObserver.getTrace(), SameTraceEntry.class);
			result.setTrace(arrayObserver.getTrace(), ArrayTraceEntry.class);
		} catch (Exception e) {
			throw new Error(e);
		}

		return result;
	}
	
	protected void filterFailingAssertions(TestCase test) {
		
		// Make sure we are not keeping assertions influenced by static state
		// TODO: Need to handle statically initialized classes
		ExecutionResult result = runTest(test);
		Set<Assertion> invalidAssertions = new HashSet<Assertion>();
		for(Assertion assertion : test.getAssertions()) {
			for(OutputTrace<?> outputTrace : result.getTraces()) {
				if(outputTrace.isDetectedBy(assertion)) {
					invalidAssertions.add(assertion);
					break;
				}
			}
		}
		logger.info("Removing {} nondeterministic assertions", invalidAssertions.size());
		for(Assertion assertion : invalidAssertions) {
			test.removeAssertion(assertion);
		}
	}

	public void filterFailingAssertions(List<TestCase> testCases) {
		List<TestCase> tests = new ArrayList<TestCase>();
		tests.addAll(testCases);
		for(TestCase test : tests) {
			filterFailingAssertions(test);
		}
		
		// Execute again in different order 
		Randomness.shuffle(tests);
		for(TestCase test : tests) {
			filterFailingAssertions(test);
		}		
	}
	
	public void filterFailingAssertions(TestSuiteChromosome testSuite) {
		List<TestChromosome> tests = testSuite.getTestChromosomes();
		for(TestChromosome test : tests) {
			filterFailingAssertions(test.getTestCase());
		}
		
		// Execute again in different order 
		Randomness.shuffle(tests);
		for(TestChromosome test : tests) {
			filterFailingAssertions(test.getTestCase());
		}
	}
	
}
