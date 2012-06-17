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
package de.unisb.cs.st.evosuite.contracts;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.junit.TestSuiteWriter;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

/**
 * @author Gordon Fraser
 * 
 */
public class FailingTestSet {

	private static Logger logger = LoggerFactory.getLogger(FailingTestSet.class);

	/*
	 * FIXME: if actually used, need way to reset them
	 */

	/** The violated tracked */
	private static final List<ContractViolation> violations = new ArrayList<ContractViolation>();

	private static int violationCount = 0;

	/**
	 * Keep track of a new observed contract violation
	 * 
	 * @param test
	 * @param contract
	 * @param statement
	 * @param exception
	 */
	public static void addFailingTest(TestCase test, Contract contract,
	        StatementInterface statement, Throwable exception) {
		violationCount++;
		ContractViolation violation = new ContractViolation(contract, test, statement,
		        exception);

		if (!hasViolation(violation)) {
			violations.add(violation);
		}
	}

	/**
	 * How many violations have we observed in total?
	 * 
	 * @return
	 */
	public static int getNumberOfViolations() {
		return violationCount;
	}

	/**
	 * How many violations of this contract have we observed in total?
	 * 
	 * @param contract
	 * @return
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
	 * @param contract
	 * @return
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
	 * @return
	 */
	public static int getNumberOfUniqueViolations() {
		return violations.size();
	}

	/**
	 * Output the failing tests in a JUnit test suite
	 */
	public static void writeJUnitTestSuite() {
		logger.info("Writing {} failing tests", violations.size());
		TestSuiteWriter writer = new TestSuiteWriter();
		ContractChecker.setActive(false);
		TestCaseExecutor.getInstance().newObservers();
		for (int i = 0; i < violations.size(); i++) {
			logger.debug("Writing test {}/{}", i, violations.size());
			ContractViolation violation = violations.get(i);
			violation.minimizeTest();
			// TODO: Add comment about contract violation
			writer.insertTest(violation.getTestCase(), " Contract violation: "
			        + violation.getContract().toString());
		}
		String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
		String testDir = Properties.TEST_DIR;
		writer.writeTestSuite("Failures" + name, testDir);
	}

	/**
	 * Determine if we already have an instance of this violation
	 */
	public static boolean hasViolation(ContractViolation violation) {
		for (ContractViolation oldViolation : violations) {
			if (oldViolation.same(violation))
				return true;
		}

		return false;
	}

}
