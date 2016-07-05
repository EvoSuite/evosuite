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
/**
 * 
 */
package org.evosuite.assertion;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.TimeController;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.execution.reset.ClassReInitializer;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		if(!Properties.isRegression())
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

		setupClassLoader(suite);

		for(TestChromosome test : suite.getTestChromosomes()) {
			if(!TimeController.getInstance().hasTimeToExecuteATestCase())
				break;
			
			addAssertions(test.getTestCase());
		}
	}
	
	/**
	 * Execute a test case on the original unit
	 * 
	 * @param test
	 *            The test case that should be executed
	 * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
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
			if(!Properties.isRegression())
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
	
	/**
	 * Reinstrument to make sure final fields are removed
	 * 
	 * @param suite
	 */
	public void setupClassLoader(TestSuiteChromosome suite) {
		if (!Properties.RESET_STATIC_FIELDS) {
			return;
		}
		final boolean reset_all_classes = Properties.RESET_ALL_CLASSES_DURING_ASSERTION_GENERATION;
		ClassReInitializer.getInstance().setReInitializeAllClasses(reset_all_classes);
		changeClassLoader(suite);
	}
	
	protected void changeClassLoader(TestSuiteChromosome suite) {
		Sandbox.goingToExecuteSUTCode();
		TestGenerationContext.getInstance().goingToExecuteSUTCode();
		Sandbox.goingToExecuteUnsafeCodeOnSameThread();
		try {
			

			TestGenerationContext.getInstance().resetContext();
			TestGenerationContext.getInstance().goingToExecuteSUTCode();
			// We need to reset the target Class since it requires a different instrumentation
			// for handling assertion generation.
			Properties.resetTargetClass();

			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Mutants, MutationPool.getMutantCounter());

			for(TestChromosome test : suite.getTestChromosomes()) {
				DefaultTestCase dtest = (DefaultTestCase) test.getTestCase();
				dtest.changeClassLoader(TestGenerationContext.getInstance().getClassLoaderForSUT());
				test.setChanged(true); // clears cached results
				test.clearCachedMutationResults();
			}
		} catch (Throwable e) {
			LoggingUtils.getEvoLogger().error("* Error while initializing target class: "
					+ (e.getMessage() != null ? e.getMessage()
							: e.toString()));
			logger.error("Problem for " + Properties.TARGET_CLASS + ". Full stack:", e);
		} finally {
			TestGenerationContext.getInstance().doneWithExecutingSUTCode();
			Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
			Sandbox.doneWithExecutingSUTCode();
			TestGenerationContext.getInstance().doneWithExecutingSUTCode();
		}
	}
	
}
