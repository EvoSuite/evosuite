/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class ContractChecker extends ExecutionObserver {

	private static Logger logger = LoggerFactory.getLogger(ContractChecker.class);

	private final Set<Contract> contracts = new HashSet<Contract>();

	private static final boolean checkAtEnd = Properties.CHECK_CONTRACTS_END;

	private static boolean valid = true;

	private static boolean active = true;

	public ContractChecker() {
		// Default from EvoSuite
		//		contracts.add(new UndeclaredExceptionContract());
		contracts.add(new JCrasherExceptionContract());

		// Defaults from Randoop paper
		contracts.add(new NullPointerExceptionContract());
		contracts.add(new AssertionErrorContract());
		contracts.add(new EqualsContract());
		contracts.add(new ToStringReturnsNormallyContract());
		contracts.add(new HashCodeReturnsNormallyContract());

		// Further Randoop contracts, not in paper
		contracts.add(new EqualsHashcodeContract());
		contracts.add(new EqualsNullContract());
		contracts.add(new EqualsSymmetricContract());
	}

	public static void setActive(boolean isActive) {
		active = isActive;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
	 */
	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	public static void currentTest(TestCase test) {
		currentTest = test;
		valid = true;
		// TODO: Keep track of objects that raised an exception, and exclude them from contract checking
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#statement(int, de.unisb.cs.st.evosuite.testcase.Scope, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		if (!valid) {
			logger.warn("Not checking contracts for invalid test");
			return;
		}

		if (!active) {
			return;
		}

		if (checkAtEnd && statement.getPosition() < (currentTest.size() - 1))
			return;

		// TODO: Only check contracts if the test hasn't already violated any other contracts
		for (Contract contract : contracts) {
			try {
				if (!contract.check(statement, scope, exception)) {
					logger.debug("Contract failed: " + contract + " on statement "
					        + statement.getCode());
					FailingTestSet.addFailingTest(currentTest, contract, statement,
					                              exception);
					//valid = false;
					break;
				}
			} catch (Throwable t) {
				//logger.info("Caught exception during contract checking");
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

}
