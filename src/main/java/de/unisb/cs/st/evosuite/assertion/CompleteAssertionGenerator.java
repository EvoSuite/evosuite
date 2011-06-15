/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class CompleteAssertionGenerator extends AssertionGenerator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.assertion.AssertionGenerator#addAssertions(de
	 * .unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public void addAssertions(TestCase test) {
		ExecutionResult result = runTest(test);
		result.comparison_trace.getAllAssertions(test);
		result.primitive_trace.getAllAssertions(test);
		result.inspector_trace.getAllAssertions(test);
		result.field_trace.getAllAssertions(test);
		result.null_trace.getAllAssertions(test);
	}

}
