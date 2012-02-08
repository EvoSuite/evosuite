/**
 * 
 */
package de.unisb.cs.st.evosuite.repair;

import java.util.Map;

import de.unisb.cs.st.evosuite.junit.JUnit4TestAdapter;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class JUnit4AssertionLogAdapter extends JUnit4TestAdapter {
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getImports()
	 */
	@Override
	public String getImports() {
		return "import org.junit.Before;\n" + "import org.junit.Ignore;\n"
		        + "import org.junit.Test;\n"
		        + "import static de.unisb.cs.st.evosuite.repair.AssertionLogger.*;\n";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getTestString(de.unisb.cs.st.evosuite.testcase.TestCase, java.util.Map)
	 */
	@Override
	public String getTestString(int id, TestCase test, Map<Integer, Throwable> exceptions) {
		AssertionLogTestVisitor visitor = new AssertionLogTestVisitor(id);
		visitor.setExceptions(exceptions);
		test.accept(visitor);
		return visitor.getCode();
	}
}
