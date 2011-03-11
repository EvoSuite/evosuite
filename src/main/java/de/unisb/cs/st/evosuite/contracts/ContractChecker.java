/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.Statement;

/**
 * @author Gordon Fraser
 * 
 */
public class ContractChecker extends ExecutionObserver {

	private static Logger logger = Logger.getLogger(ContractChecker.class);

	private final Set<Contract> contracts = new HashSet<Contract>();

	private final Set<ContractViolation> violations = new HashSet<ContractViolation>();

	public ContractChecker() {
		contracts.add(new EqualsContract());
		contracts.add(new EqualsNullContract());
		contracts.add(new ExceptionContract());
		contracts.add(new ToStringReturnsNormally());
		contracts.add(new EqualsHashcodeContract());
		contracts.add(new EqualsSymmetricContract());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
	 */
	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#statement(int, de.unisb.cs.st.evosuite.testcase.Scope, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void statement(Statement statement, Scope scope, Throwable exception) {
		logger.warn("Checking contracts ");
		for (Contract contract : contracts) {
			try {
				if (!contract.check(statement, scope, exception)) {
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
				}
			} catch (Throwable t) {
				logger.info("Caught exception during contract checking");
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
