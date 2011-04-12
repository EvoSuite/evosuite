/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class UnitAssertionGenerator extends AssertionGenerator {

	private boolean isRelevant(Statement s, TestCase t) {
		// Always allow assertions on the last statement
		if (s.getPosition() == (t.size() - 1))
			return true;
		if (s instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) s;
			String declaringClass = ms.getMethod().getDeclaringClass().getName();
			while (declaringClass.contains("$"))
				declaringClass = declaringClass.substring(0, declaringClass.indexOf("$"));

			if (declaringClass.equals(Properties.TARGET_CLASS))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.AssertionGenerator#addAssertions(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public void addAssertions(TestCase test) {
		ExecutionResult result = runTest(test);
		result.comparison_trace.getAllAssertions(test);
		result.primitive_trace.getAllAssertions(test);
		result.inspector_trace.getAllAssertions(test);
		result.field_trace.getAllAssertions(test);
		result.null_trace.getAllAssertions(test);

		for (int i = 0; i < test.size(); i++) {
			Statement s = test.getStatement(i);
			if (!isRelevant(s, test))
				s.removeAssertions();
		}
	}

}
