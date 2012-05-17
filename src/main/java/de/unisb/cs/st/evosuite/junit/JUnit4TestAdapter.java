/**
 * 
 */
package de.unisb.cs.st.evosuite.junit;

import java.util.List;
import java.util.Map;

import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author fraser
 * 
 */
public class JUnit4TestAdapter implements UnitTestAdapter {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getImports()
	 */
	@Override
	public String getImports() {
		//		return "import org.junit.Before;\n" + "import org.junit.Ignore;\n"
		//		        + "import org.junit.Test;\n" + "import static org.junit.Assert.*;\n";
		return "import org.junit.Test;\n" + "import static org.junit.Assert.*;\n";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getClassDefinition(java.lang.String)
	 */
	@Override
	public String getClassDefinition(String testName) {
		return "public class " + testName;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getMethodDefinition(java.lang.String)
	 */
	@Override
	public String getMethodDefinition(String testName) {
		return "  @Test\n  public void " + testName + "() ";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getSuite(java.util.List)
	 */
	@Override
	public String getSuite(List<String> suites) {
		StringBuilder builder = new StringBuilder();
		builder.append("import org.junit.runner.RunWith;\n");
		builder.append("import org.junit.runners.Suite;\n\n");

		for (String suite : suites) {
			if (suite.contains(".")) {
				builder.append("import ");
				builder.append(suite);
				builder.append(";\n");
			}
		}
		builder.append("\n");

		builder.append("@RunWith(Suite.class)\n");
		builder.append("@Suite.SuiteClasses({\n");
		boolean first = true;
		for (String suite : suites) {
			if (!first) {
				builder.append(",\n");
			}
			first = false;
			builder.append("  ");
			builder.append(suite.substring(suite.lastIndexOf(".") + 1));
			builder.append(".class");
		}
		builder.append("})\n");

		builder.append(getClassDefinition("GeneratedTestSuite"));
		builder.append(" {\n");
		builder.append("}\n");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getTestString(de.unisb.cs.st.evosuite.testcase.TestCase, java.util.Map)
	 */
	@Override
	public String getTestString(int id, TestCase test, Map<Integer, Throwable> exceptions) {
		return test.toCode(exceptions);
	}
}
