/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class ContractChecker extends ExecutionObserver {

	private static Logger logger = Logger.getLogger(ContractChecker.class);

	private final Set<Contract> contracts = new HashSet<Contract>();

	private final Set<ContractViolation> violations = new HashSet<ContractViolation>();

	private static boolean valid = true;

	public ContractChecker() {
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
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#statement(int, de.unisb.cs.st.evosuite.testcase.Scope, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		if (!valid)
			return;
		//logger.info("Skipping contract checking because test already violated a contract");
		// TODO: Only check contracts if the test hasn't already violated any other contracts
		for (Contract contract : contracts) {
			try {
				if (!contract.check(statement, scope, exception)) {
					valid = false;
					ContractViolation violation = new ContractViolation(contract,
					        ExecutionObserver.getCurrentTest(), statement, exception);
					// TODO: A contract violation should not include objects that already violated another contract (i.e., once an object raises a nullpointerexception any further nullpointerexceptions are to be expected)
					if (!violations.contains(violation)) {
						logger.warn("New contract violation detected in test case: ");
						logger.warn(ExecutionObserver.getCurrentTest().toCode());
						logger.warn(contract + " violated at statement "
						        + statement.getCode());
						violations.add(violation);
						logger.warn("Current number of violations: " + violations.size());
					}
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
