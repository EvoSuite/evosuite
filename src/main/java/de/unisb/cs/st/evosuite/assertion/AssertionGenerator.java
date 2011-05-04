/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class AssertionGenerator {

	protected static Logger logger = Logger.getLogger(AssertionGenerator.class);

	protected PrimitiveOutputTraceObserver primitive_observer = new PrimitiveOutputTraceObserver();

	protected ComparisonTraceObserver comparison_observer = new ComparisonTraceObserver();

	protected InspectorTraceObserver inspector_observer = new InspectorTraceObserver();

	protected PrimitiveFieldTraceObserver field_observer = new PrimitiveFieldTraceObserver();

	protected NullOutputObserver null_observer = new NullOutputObserver();

	protected TestCaseExecutor executor = TestCaseExecutor.getInstance();

	public AssertionGenerator() {
		executor.addObserver(primitive_observer);
		executor.addObserver(comparison_observer);
		executor.addObserver(inspector_observer);
		executor.addObserver(field_observer);
		executor.addObserver(null_observer);
	}

	public abstract void addAssertions(TestCase test);

	public static AssertionGenerator getDefaultGenerator() {
		return new UnitAssertionGenerator();
	}

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
			executor.setLogging(true);
			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
			result.comparison_trace = comparison_observer.getTrace();
			result.primitive_trace = primitive_observer.getTrace();
			result.inspector_trace = inspector_observer.getTrace();
			result.field_trace = field_observer.getTrace();
			result.null_trace = null_observer.getTrace();
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

}
