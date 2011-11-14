/**
 * 
 */
package de.unisb.cs.st.evosuite.junit;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author fraser
 * 
 */
public class JUnit3TestAdapter implements UnitTestAdapter {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getImports()
	 */
	@Override
	public String getImports() {
		return "import junit.framework.TestCase;\n";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getClassDefinition(java.lang.String)
	 */
	@Override
	public String getClassDefinition(String testName) {
		return "public class " + testName + " extends TestCase";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getMethodDefinition(java.lang.String)
	 */
	@Override
	public String getMethodDefinition(String testName) {
		return "public void " + testName + "() ";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getSuite(java.util.List)
	 */
	@Override
	public String getSuite(List<String> suites) {
		StringBuilder builder = new StringBuilder();
		builder.append("import junit.framework.Test;\n");
		builder.append("import junit.framework.TestCase;\n");
		builder.append("import junit.framework.TestSuite;\n\n");

		for (String suite : suites) {
			builder.append("import ");
			builder.append(Properties.PROJECT_PREFIX);
			builder.append(suite);
			builder.append(";\n");
		}
		builder.append("\n");

		builder.append(getClassDefinition("GeneratedTestSuite"));
		builder.append(" {\n");
		builder.append("  public static Test suite() {\n");
		builder.append("    TestSuite suite = new TestSuite();\n");
		for (String suite : suites) {
			builder.append("    suite.addTestSuite(");
			builder.append(suite.substring(suite.lastIndexOf(".") + 1));
			builder.append(".class);\n");
		}

		builder.append("    return suite;\n");
		builder.append("  }\n");
		builder.append("}\n");
		return builder.toString();
	}

}
