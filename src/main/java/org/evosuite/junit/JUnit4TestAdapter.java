/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.junit;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.Properties;
import org.evosuite.reset.ResetManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.utils.SystemInUtil;

/**
 * <p>
 * JUnit4TestAdapter class.
 * </p>
 * 
 * @author fraser
 */
public class JUnit4TestAdapter implements UnitTestAdapter {

	private String getJUnitTestShortName() {
		if(Properties.TARGET_CLASS.equals("Test"))
			return "org.junit.Test";
		else
			return "Test";
	}

	/* (non-Javadoc)
	 * @see org.evosuite.junit.UnitTestAdapter#getImports()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<String> getImports(boolean wasSecurityException) {
		Set<String> imports = new HashSet<String>();
		imports.add("static org.junit.Assert.*");
		if(!Properties.TARGET_CLASS.equals("Test"))
			imports.add(org.junit.Test.class.getCanonicalName());
		
		
		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
				|| Properties.RESET_STATIC_FIELDS || wasSecurityException
				|| SystemInUtil.getInstance().hasBeenUsed()) {
			imports.add(org.junit.Rule.class.getCanonicalName());
			imports.add(org.evosuite.junit.rules.Instrumentation.class.getCanonicalName());
		}
		if (Properties.REPLACE_CALLS) {
			imports.add(org.evosuite.junit.rules.Stubbing.class.getCanonicalName());			
		}
		if (Properties.VIRTUAL_FS) {
			imports.add(org.evosuite.junit.rules.VirtualFS.class.getCanonicalName());			
		}
		if(Properties.RESET_STATIC_FIELDS) {
			imports.add(org.evosuite.junit.rules.StaticStateResetter.class.getCanonicalName());
			if(Properties.REPLACE_CALLS) {
				imports.add(org.evosuite.junit.rules.InitializeClasses.class.getCanonicalName());
				imports.add(org.junit.ClassRule.class.getCanonicalName());
			}
		}
		return imports;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.junit.UnitTestAdapter#getClassDefinition(java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public String getClassDefinition(String testName) {
		return "public class " + testName;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.junit.UnitTestAdapter#getMethodDefinition(java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public String getMethodDefinition(String testName) {
		return "  @"+getJUnitTestShortName()+"\n  public void " + testName + "() ";
	}

	/* (non-Javadoc)
	 * @see org.evosuite.junit.UnitTestAdapter#getSuite(java.util.List)
	 */
	/** {@inheritDoc} */
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
	 * @see org.evosuite.junit.UnitTestAdapter#getTestString(org.evosuite.testcase.TestCase, java.util.Map)
	 */
	/** {@inheritDoc} */
	@Override
	public String getTestString(int id, TestCase test, Map<Integer, Throwable> exceptions) {
		return test.toCode(exceptions);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.junit.UnitTestAdapter#getTestString(int, org.evosuite.testcase.TestCase, java.util.Map, org.evosuite.testcase.TestCodeVisitor)
	 */
	/** {@inheritDoc} */
	@Override
	public String getTestString(int id, TestCase test,
	        Map<Integer, Throwable> exceptions, TestCodeVisitor visitor) {
		visitor.setExceptions(exceptions);
		test.accept(visitor);
		visitor.clearExceptions();
		return visitor.getCode();
	}

	@Override
	public String getInstrumentationCode(boolean wasSecurityException) {
		StringBuilder sb = new StringBuilder();
		sb.append(TestSuiteWriter.METHOD_SPACE);
		sb.append("@Rule\n");
		sb.append(TestSuiteWriter.METHOD_SPACE);
		sb.append("public Instrumentation instrumentation = new Instrumentation();\n\n");
		return sb.toString();
	}

	@Override
	public String getStaticResettingCode() {
		StringBuilder sb = new StringBuilder();
		List<String> classesToReset = ResetManager.getInstance()
				.getClassResetOrder();

		sb.append(TestSuiteWriter.METHOD_SPACE);
		sb.append("@Rule\n");
		sb.append(TestSuiteWriter.METHOD_SPACE);
		sb.append(      "public StaticStateResetter staticState = new StaticStateResetter(");
		String indent = "                                                                 ";
		boolean first = true;
		StringBuilder classNameList = new StringBuilder();
		for(String className : classesToReset) {
			if(first) {
				first = false;
			} else {
				classNameList.append(",\n");
				classNameList.append(TestSuiteWriter.METHOD_SPACE);
				classNameList.append(indent);
			}
			classNameList.append("\"");
			classNameList.append(className);
			classNameList.append("\"");
		}
		String classNames = classNameList.toString();
		sb.append(classNames);
		sb.append(");\n\n");
		
		if(Properties.REPLACE_CALLS) {
			sb.append(TestSuiteWriter.METHOD_SPACE);
			sb.append("@ClassRule\n");
			sb.append(TestSuiteWriter.METHOD_SPACE);
			sb.append("public static InitializeClasses classInitializer = new InitializeClasses(");
			sb.append(classNames);
			sb.append(");\n\n");			
		}

		return sb.toString();
	}

	@Override
	public String getStubbingCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(TestSuiteWriter.METHOD_SPACE);
		sb.append("@Rule\n");
		sb.append(TestSuiteWriter.METHOD_SPACE);
		sb.append(      "public Stubbing stubbing = new Stubbing();\n\n");
		return sb.toString();
	}
	
	@Override
	public String getVirtualFSCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(TestSuiteWriter.METHOD_SPACE);
		sb.append("@Rule\n");
		sb.append(TestSuiteWriter.METHOD_SPACE);
		sb.append("public VirtualFS virtualFS = new VirtualFS();\n\n");
		return sb.toString();
	}

}
