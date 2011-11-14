/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class AssertionGenerator {

	protected static Logger logger = LoggerFactory.getLogger(AssertionGenerator.class);

	protected static PrimitiveTraceObserver primitive_observer = new PrimitiveTraceObserver();

	protected static ComparisonTraceObserver comparison_observer = new ComparisonTraceObserver();

	protected static InspectorTraceObserver inspector_observer = new InspectorTraceObserver();

	protected static PrimitiveFieldTraceObserver field_observer = new PrimitiveFieldTraceObserver();

	protected static NullTraceObserver null_observer = new NullTraceObserver();

	protected static TestCaseExecutor executor = TestCaseExecutor.getInstance();

	public AssertionGenerator() {
		executor.addObserver(primitive_observer);
		executor.addObserver(comparison_observer);
		executor.addObserver(inspector_observer);
		executor.addObserver(field_observer);
		executor.addObserver(null_observer);
	}

	public abstract void addAssertions(TestCase test);

	/**
	 * Execute a test case on the original unit
	 * 
	 * @param test
	 *            The test case that should be executed
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
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

}
