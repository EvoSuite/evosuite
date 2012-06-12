/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.junit;

import java.util.List;
import java.util.Map;

import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCodeVisitor;

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

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getTestString(int, de.unisb.cs.st.evosuite.testcase.TestCase, java.util.Map, de.unisb.cs.st.evosuite.testcase.TestCodeVisitor)
	 */
	@Override
	public String getTestString(int id, TestCase test,
	        Map<Integer, Throwable> exceptions, TestCodeVisitor visitor) {
		test.accept(visitor);
		return visitor.getCode();
	}
}
