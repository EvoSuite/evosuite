/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class UnitAssertionGenerator extends AssertionGenerator {

	private boolean isRelevant(StatementInterface s, TestCase t) {
		// Always allow assertions on the last statement
		if (s.getPosition() == (t.size() - 1))
			return true;

		// Allow assertions after method calls on the UUT
		if (s instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) s;
			String declaringClass = ms.getMethod().getDeclaringClass().getName();
			while (declaringClass.contains("$"))
				declaringClass = declaringClass.substring(0, declaringClass.indexOf("$"));

			if (declaringClass.equals(Properties.TARGET_CLASS) || (!Properties.TARGET_CLASS_PREFIX.isEmpty() && declaringClass.startsWith(Properties.TARGET_CLASS_PREFIX)))
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
		for (OutputTrace<?> trace : result.getTraces()) {
			trace.getAllAssertions(test);
		}

		for (int i = 0; i < test.size(); i++) {
			StatementInterface s = test.getStatement(i);
			if (!isRelevant(s, test))
				s.removeAssertions();
		}
	}

}
