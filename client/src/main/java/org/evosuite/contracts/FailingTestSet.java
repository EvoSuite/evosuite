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

package org.evosuite.contracts;

import org.evosuite.Properties;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * FailingTestSet class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class FailingTestSet {

    private static final Logger logger = LoggerFactory.getLogger(FailingTestSet.class);

    /*
     * FIXME: if actually used, need way to reset them
     */

    /**
     * The violated tracked
     */
    private static final List<ContractViolation> violations = new ArrayList<>();

    private static int violationCount = 0;

    public static void addFailingTest(ContractViolation violation) {
        violationCount++;
        if (!hasViolation(violation)) {
            violations.add(violation);
        }
    }

    /**
     * How many violations have we observed in total?
     *
     * @return a int.
     */
    public static int getNumberOfViolations() {
        return violationCount;
    }

    /**
     * How many violations of this contract have we observed in total?
     *
     * @param contract a {@link org.evosuite.contracts.Contract} object.
     * @return a int.
     */
    public static int getNumberOfViolations(Contract contract) {
        int num = 0;
        for (ContractViolation violation : violations) {
            if (violation.getContract().equals(contract))
                num++;
        }
        return num;
    }

    /**
     * How many violations of this contract have we observed in total?
     *
     * @param contractClass a {@link java.lang.Class} object.
     * @return a int.
     */
    public static int getNumberOfViolations(Class<?> contractClass) {
        int num = 0;
        for (ContractViolation violation : violations) {
            if (violation.getContract().getClass().equals(contractClass))
                num++;
        }
        return num;
    }

    /**
     * How many unique violations have we observed?
     *
     * @return a int.
     */
    public static int getNumberOfUniqueViolations() {
        return violations.size();
    }

    public static Collection<ContractViolation> getContractViolations() {
        return Collections.unmodifiableCollection(violations);
    }

    public static List<TestCase> getFailingTests() {
        List<TestCase> tests = new ArrayList<>();
        ContractChecker.setActive(false);
        TestCaseExecutor.getInstance().newObservers();

        for (int i = 0; i < violations.size(); i++) {
            logger.debug("Writing test {}/{}", i, violations.size());
            ContractViolation violation = violations.get(i);
            violation.minimizeTest();
            TestCase test = violation.getTestCase();
            //violation.addAssertion(test);
            tests.add(test);
        }
        return tests;
    }

    /**
     * Output the failing tests in a JUnit test suite
     */
    public static void writeJUnitTestSuite() {
        logger.info("Writing {} failing tests", violations.size());
        TestSuiteWriter writer = new TestSuiteWriter();
        writeJUnitTestSuite(writer);
        String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
        String testDir = Properties.TEST_DIR;
        writer.writeTestSuite("Failures" + name, testDir, Collections.emptyList());
    }

    /**
     * Output the failing tests in a JUnit test suite
     */
    public static void writeJUnitTestSuite(TestSuiteWriter writer) {
        logger.info("Writing {} failing tests", violations.size());
        ContractChecker.setActive(false);
        TestCaseExecutor.getInstance().newObservers();
        for (int i = 0; i < violations.size(); i++) {
            logger.debug("Writing test {}/{}", i, violations.size());
            ContractViolation violation = violations.get(i);
            violation.minimizeTest();
            TestCase test = violation.getTestCase();
            test.addContractViolation(violation);
            //violation.addAssertion(test);
            // TODO: Add comment about contract violation
            writer.insertTest(test, " Contract violation: "
                    + violation.getContract().toString());
        }
    }

    /**
     * Determine if we already have an instance of this violation
     *
     * @param violation a {@link org.evosuite.contracts.ContractViolation} object.
     * @return a boolean.
     */
    public static boolean hasViolation(ContractViolation violation) {
        for (ContractViolation oldViolation : violations) {
            if (oldViolation.same(violation))
                return true;
        }

        return false;
    }

    public static void changeClassLoader(ClassLoader classLoader) {
        for (ContractViolation violation : violations) {
            violation.changeClassLoader(classLoader);
        }
    }

    public static void clear() {
        violations.clear();
        violationCount = 0;
    }

    public static void sendStatistics() {
        if (!Properties.NEW_STATISTICS)
            return;

        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.AssertionContract, getNumberOfViolations(AssertionErrorContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.EqualsContract, getNumberOfViolations(EqualsContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.EqualsHashcodeContract, getNumberOfViolations(EqualsHashcodeContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.EqualsNullContract, getNumberOfViolations(EqualsNullContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.EqualsSymmetricContract, getNumberOfViolations(EqualsSymmetricContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.HashCodeReturnsNormallyContract, getNumberOfViolations(HashCodeReturnsNormallyContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.JCrasherExceptionContract, getNumberOfViolations(JCrasherExceptionContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.NullPointerExceptionContract, getNumberOfViolations(NullPointerExceptionContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.ToStringReturnsNormallyContract, getNumberOfViolations(ToStringReturnsNormallyContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.UndeclaredExceptionContract, getNumberOfViolations(UndeclaredExceptionContract.class));
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Contract_Violations, getNumberOfViolations());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Unique_Violations, getNumberOfUniqueViolations());
    }
}
