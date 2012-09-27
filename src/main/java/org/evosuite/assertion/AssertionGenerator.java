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

import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
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
	protected static final PrimitiveTraceObserver primitive_observer = new PrimitiveTraceObserver();

	/** Constant <code>comparison_observer</code> */
	protected static final ComparisonTraceObserver comparison_observer = new ComparisonTraceObserver();

	/** Constant <code>same_observer</code> */
	protected static final SameTraceObserver same_observer = new SameTraceObserver();

	/** Constant <code>inspector_observer</code> */
	protected static final InspectorTraceObserver inspector_observer = new InspectorTraceObserver();

	/** Constant <code>field_observer</code> */
	protected static final PrimitiveFieldTraceObserver field_observer = new PrimitiveFieldTraceObserver();

	/** Constant <code>null_observer</code> */
	protected static final NullTraceObserver null_observer = new NullTraceObserver();

	/** Constant <code>executor</code> */
	protected static final TestCaseExecutor executor = TestCaseExecutor.getInstance();

	/**
	 * <p>
	 * Constructor for AssertionGenerator.
	 * </p>
	 */
	public AssertionGenerator() {
		executor.addObserver(primitive_observer);
		executor.addObserver(comparison_observer);
		executor.addObserver(inspector_observer);
		executor.addObserver(field_observer);
		executor.addObserver(null_observer);
		executor.addObserver(same_observer);
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
			result = executor.execute(test);
			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
			result.setTrace(comparison_observer.getTrace(), ComparisonTraceEntry.class);
			result.setTrace(primitive_observer.getTrace(), PrimitiveTraceEntry.class);
			result.setTrace(inspector_observer.getTrace(), InspectorTraceEntry.class);
			result.setTrace(field_observer.getTrace(), PrimitiveFieldTraceEntry.class);
			result.setTrace(null_observer.getTrace(), NullTraceEntry.class);
			result.setTrace(same_observer.getTrace(), SameTraceEntry.class);
		} catch (Exception e) {
			throw new Error(e);
		}

		return result;
	}

}
