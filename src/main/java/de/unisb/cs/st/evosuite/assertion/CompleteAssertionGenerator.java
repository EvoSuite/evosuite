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

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.AssertionGenerator#addAssertions(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public void addAssertions(TestCase test) {
		ExecutionResult result = runTest(test);
		for (OutputTrace<?> trace : result.getTraces()) {
			trace.getAllAssertions(test);
			trace.clear();
		}
	}
}
