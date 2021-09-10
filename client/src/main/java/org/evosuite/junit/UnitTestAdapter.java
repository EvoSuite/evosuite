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

import org.evosuite.junit.writer.JUnitAnnotationProvider;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;

import java.util.List;
import java.util.Map;


/**
 * <p>UnitTestAdapter interface.</p>
 *
 * @author fraser
 */
public interface UnitTestAdapter extends JUnitAnnotationProvider {

    /**
     * Get all the framework dependent imports
     *
     * @return a {@link java.lang.String} object.
     */
    String getImports();

    /**
     * Get the framework specific definition of the test class
     *
     * @param testName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getClassDefinition(String testName);

    /**
     * Get the framework specific definition of a test method
     *
     * @param testName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getMethodDefinition(String testName);

    /**
     * Get the class definition of a test suite
     *
     * @param tests a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    String getSuite(List<String> tests);

    /**
     * Return the sequence of method calls for a test
     *
     * @param test       a {@link org.evosuite.testcase.TestCase} object.
     * @param exceptions a {@link java.util.Map} object.
     * @param id         a int.
     * @return a {@link java.lang.String} object.
     */
    String getTestString(int id, TestCase test, Map<Integer, Throwable> exceptions);

    /**
     * Return the sequence of method calls for a test
     *
     * @param test       a {@link org.evosuite.testcase.TestCase} object.
     * @param exceptions a {@link java.util.Map} object.
     * @param visitor    a {@link org.evosuite.testcase.TestCodeVisitor} object.
     * @param id         a int.
     * @return a {@link java.lang.String} object.
     */
    String getTestString(int id, TestCase test,
                         Map<Integer, Throwable> exceptions, TestCodeVisitor visitor);

    /**
     * Add the non-functional requirement to the test case.
     *
     * @param builder The string builder which should be extended.
     */
    void addNFR(StringBuilder builder);
}
