/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * @author fraser
 * 
 */
public class SingleContractChecker extends ExecutionObserver {

	private final Contract contract;

	private boolean valid = true;

	public SingleContractChecker(Contract contract) {
		this.contract = contract;
	}

	public boolean isValid() {
		return valid;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
	 */
	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionObserver#statement(de.unisb.cs.st.evosuite.testcase.StatementInterface, de.unisb.cs.st.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		try {
			if (!contract.check(statement, scope, exception)) {
				FailingTestSet.addFailingTest(currentTest, contract, statement, exception);
				valid = false;
			}
		} catch (Throwable t) {
			//logger.info("Caught exception during contract checking");
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
