/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.junit.TestSuiteWriter;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class FailingTestSet {

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
		TestSuiteWriter writer = new TestSuiteWriter();
		ContractChecker.setActive(false);
		for (int i = 0; i < violations.size(); i++) {
			ContractViolation violation = violations.get(i);
			violation.minimizeTest();
			writer.insertTest(violation.getTestCase());
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
