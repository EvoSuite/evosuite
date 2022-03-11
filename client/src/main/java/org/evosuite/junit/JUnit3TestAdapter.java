/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package org.evosuite.junit;

import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.runtime.vnet.NonFunctionalRequirementRule;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;

import java.util.List;
import java.util.Map;

/**
 * <p>JUnit3TestAdapter class.</p>
 *
 * @author fraser
 */
public class JUnit3TestAdapter implements UnitTestAdapter {

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getImports()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImports() {
        return "import junit.framework.TestCase;\n";
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getClassDefinition(java.lang.String)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassDefinition(String testName) {
        return "public class " + testName + " extends TestCase";
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getMethodDefinition(java.lang.String)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethodDefinition(String testName) {
        return "public void " + testName + "() ";
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getSuite(java.util.List)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSuite(List<String> suites) {
        StringBuilder builder = new StringBuilder();
        builder.append("import junit.framework.Test;\n");
        builder.append("import junit.framework.TestCase;\n");
        builder.append("import junit.framework.TestSuite;\n\n");

        for (String suite : suites) {
            builder.append("import ");
            // builder.append(Properties.PROJECT_PREFIX);
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

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getTestString(org.evosuite.testcase.TestCase, java.util.Map)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestString(int id, TestCase test, Map<Integer, Throwable> exceptions) {
        return test.toCode(exceptions);
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getTestString(int, org.evosuite.testcase.TestCase, java.util.Map, org.evosuite.testcase.TestCodeVisitor)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestString(int id, TestCase test,
                                Map<Integer, Throwable> exceptions, TestCodeVisitor visitor) {
        visitor.setExceptions(exceptions);
        test.accept(visitor);
        visitor.clearExceptions();
        return visitor.getCode();
    }

    @Override
    public Class<?> testAnnotation() {
        return org.junit.Test.class;
    }

    @Override
    public Class<?> beforeAll() {
        return org.junit.BeforeClass.class;
    }

    @Override
    public Class<?> beforeEach() {
        return org.junit.Before.class;
    }

    @Override
    public Class<?> afterAll() {
        return org.junit.AfterClass.class;
    }

    @Override
    public Class<?> afterEach() {
        return org.junit.After.class;
    }

    @Override
    public void addNFR(StringBuilder builder) {
        builder.append(TestSuiteWriterUtils.METHOD_SPACE);
        builder.append("@").append(org.junit.Rule.class.getCanonicalName()).append("\n");
        builder.append(TestSuiteWriterUtils.METHOD_SPACE);
        builder.append("public ").append(NonFunctionalRequirementRule.class.getName()).append(" nfr = new ").append(NonFunctionalRequirementRule.class.getName()).append("();\n\n");
    }

}
