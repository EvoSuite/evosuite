package org.evosuite.regression;

import org.evosuite.Properties;
import org.evosuite.assertion.AssertionGenerator;
import org.evosuite.assertion.ComparisonTraceEntry;
import org.evosuite.assertion.ComparisonTraceObserver;
import org.evosuite.assertion.InspectorTraceEntry;
import org.evosuite.assertion.InspectorTraceObserver;
import org.evosuite.assertion.NullTraceEntry;
import org.evosuite.assertion.NullTraceObserver;
import org.evosuite.assertion.PrimitiveFieldTraceEntry;
import org.evosuite.assertion.PrimitiveFieldTraceObserver;
import org.evosuite.assertion.PrimitiveTraceEntry;
import org.evosuite.assertion.PrimitiveTraceObserver;
import org.evosuite.assertion.SameTraceEntry;
import org.evosuite.assertion.SameTraceObserver;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.TestCaseExecutor;


public class RegressionAssertionGenerator extends AssertionGenerator {

	private static PrimitiveTraceObserver primitiveObserver = new PrimitiveTraceObserver();
	private static ComparisonTraceObserver comparisonObserver = new ComparisonTraceObserver();
	private static SameTraceObserver sameObserver = new SameTraceObserver();
	private static InspectorTraceObserver inspectorObserver = new InspectorTraceObserver();
	private static PrimitiveFieldTraceObserver fieldObserver = new PrimitiveFieldTraceObserver();
	private static NullTraceObserver nullObserver = new NullTraceObserver();

	/** Constant <code>observerClasses</code> */
	public static Class<?>[] observerClasses = { PrimitiveTraceEntry.class,
	        ComparisonTraceEntry.class, SameTraceEntry.class, InspectorTraceEntry.class,
	        PrimitiveFieldTraceEntry.class, NullTraceEntry.class };

	@Override
	public void addAssertions(TestCase test) {
		// TODO Auto-generated method stub

	}


	 public RegressionAssertionGenerator() {
		super();
		TestCaseExecutor.getInstance().newObservers();
		TestCaseExecutor.getInstance().addObserver(primitiveObserver);
		TestCaseExecutor.getInstance().addObserver(comparisonObserver);
		TestCaseExecutor.getInstance().addObserver(sameObserver);
		if(!Properties.REGRESSION_DISABLE_SPECIAL_ASSERTIONS)
			TestCaseExecutor.getInstance().addObserver(inspectorObserver);
		TestCaseExecutor.getInstance().addObserver(fieldObserver);
		TestCaseExecutor.getInstance().addObserver(nullObserver);
	}

	/**
	 * Execute a test case on a mutant
	 * 
	 * @param test
	 *            The test case that should be executed
	 * @param mutant
	 *            The mutant on which the test case shall be executed
	 */
	@Override
	public ExecutionResult runTest(TestCase test) {
		ExecutionResult result = new ExecutionResult(test, null);
		//resetObservers();
		comparisonObserver.clear();
		sameObserver.clear();
		primitiveObserver.clear();
		if(!Properties.REGRESSION_DISABLE_SPECIAL_ASSERTIONS)
			inspectorObserver.clear();
		fieldObserver.clear();
		nullObserver.clear();
		try {
			logger.debug("Executing test");
			//MutationObserver.activateMutation(mutant);
			result = TestCaseExecutor.getInstance().execute(test);
			//MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);

			result.setTrace(comparisonObserver.getTrace(), ComparisonTraceEntry.class);
			result.setTrace(sameObserver.getTrace(), SameTraceEntry.class);
			result.setTrace(primitiveObserver.getTrace(), PrimitiveTraceEntry.class);
			if(!Properties.REGRESSION_DISABLE_SPECIAL_ASSERTIONS)
				result.setTrace(inspectorObserver.getTrace(), InspectorTraceEntry.class);
			result.setTrace(fieldObserver.getTrace(), PrimitiveFieldTraceEntry.class);
			result.setTrace(nullObserver.getTrace(), NullTraceEntry.class);

		} catch (Exception e) {
			throw new Error(e);
		}

		return result;
	}

	/*public ExecutionResult runTest(TestCase test){
		return super.runTest(test);
	}*/

}
