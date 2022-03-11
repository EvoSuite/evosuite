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
package org.evosuite.junit.writer;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.OutputFormat;
import org.evosuite.junit.JUnit3TestAdapter;
import org.evosuite.junit.JUnit4TestAdapter;
import org.evosuite.junit.JUnit5TestAdapter;
import org.evosuite.junit.UnitTestAdapter;
import org.evosuite.testcarver.testcase.CarvedTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Set of utility functions for generation of JUnit files
 *
 * @author arcuri
 */
public class TestSuiteWriterUtils {

    public static final String METHOD_SPACE = "  ";
    public static final String BLOCK_SPACE = "    ";
    public static final String INNER_BLOCK_SPACE = "      ";
    public static final String INNER_INNER_BLOCK_SPACE = "        ";
    public static final String INNER_INNER_INNER_BLOCK_SPACE = "          ";

    protected final static Logger logger = LoggerFactory.getLogger(TestSuiteWriterUtils.class);


    /**
     * Check the configuration settings to see if we are doing any instrumentation.
     * If so, we ll need to use the Java Agent in the generated tests
     *
     * @return
     */
    public static boolean needToUseAgent() {
        return Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
                || Properties.RESET_STATIC_FIELDS || Properties.VIRTUAL_NET;
    }


    public static boolean doesUseMocks(List<ExecutionResult> results) {
        for (ExecutionResult er : results) {
            for (Statement st : er.test) {
                if (st instanceof FunctionalMockStatement) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAnySecurityException(List<ExecutionResult> results) {
        for (ExecutionResult result : results) {
            if (result.hasSecurityException()) {
                return true;
            }
        }
        return false;
    }

    private static boolean wasAnyWrittenProperty(List<ExecutionResult> results) {
        for (ExecutionResult res : results) {
            if (res.wasAnyPropertyWritten()) {
                return true;
            }
        }
        return false;
    }

    public static String getNameOfTest(List<TestCase> tests, int position) {

        TestCase test = tests.get(position);
        String testName = null;
        if (test instanceof CarvedTestCase) {
            testName = ((CarvedTestCase) test).getName();
        } else {
            int totalNumberOfTests = tests.size();
            String totalNumberOfTestsString = String.valueOf(totalNumberOfTests - 1);
            String testNumber = StringUtils.leftPad(String.valueOf(position),
                    totalNumberOfTestsString.length(), "0");
            testName = "test" + testNumber;
        }
        return testName;
    }

    public static Set<String> mergeProperties(List<ExecutionResult> results) {
        if (results == null) {
            return null;
        }
        Set<String> set = new LinkedHashSet<>();
        for (ExecutionResult res : results) {
            Set<String> props = res.getReadProperties();
            if (props != null) {
                set.addAll(props);
            }
        }
        return set;
    }

    public static boolean shouldResetProperties(List<ExecutionResult> results) {
        /*
         * Note: we need to reset the properties even if the SUT only read them. Reason is
         * that we are modifying them in the test case in the @Before method
         */
        Set<String> readProperties = null;
        if (Properties.REPLACE_CALLS) {
            readProperties = mergeProperties(results);
            if (readProperties.isEmpty()) {
                readProperties = null;
            }
        }

        boolean shouldResetProperties = Properties.REPLACE_CALLS
                && (wasAnyWrittenProperty(results) || readProperties != null);

        return shouldResetProperties;
    }

    /**
     * Create subdirectory for package in test directory
     *
     * @param directory a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String mainDirectory(String directory) {
        String dirname = directory + File.separator
                + Properties.PROJECT_PREFIX.replace('.', File.separatorChar); // +"/GeneratedTests";
        File dir = new File(dirname);
        logger.debug("Target directory: " + dirname);
        dir.mkdirs();
        return dirname;
    }

    public static UnitTestAdapter getAdapter() {
        if (Properties.TEST_FORMAT == OutputFormat.JUNIT3)
            return new JUnit3TestAdapter();
        else if (Properties.TEST_FORMAT == OutputFormat.JUNIT4)
            return new JUnit4TestAdapter();
        else if (Properties.TEST_FORMAT == OutputFormat.JUNIT5)
            return new JUnit5TestAdapter();
        else
            throw new RuntimeException("Unknown output format: " + Properties.TEST_FORMAT);
    }

    /**
     * Create subdirectory for package in test directory
     *
     * @param directory a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String makeDirectory(String directory) {
        String dirname = directory + File.separator
                + Properties.CLASS_PREFIX.replace('.', File.separatorChar); // +"/GeneratedTests";
        File dir = new File(dirname);
        logger.debug("Target directory: " + dirname);
        dir.mkdirs();
        return dirname;
    }


}
