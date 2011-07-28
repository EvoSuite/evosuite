/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class ContractViolation {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ContractViolation.class);

	private final Contract contract;

	private final TestCase test;

	private final StatementInterface statement;

	@SuppressWarnings("unused")
	private final Throwable exception;

	public ContractViolation(Contract contract, TestCase test, StatementInterface statement,
	        Throwable exception) {
		this.contract = contract;
		this.test = test.clone();
		this.statement = statement.clone(this.test);
		this.exception = exception;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contract == null) ? 0 : contract.hashCode());

		if (statement != null) {
			if (statement instanceof MethodStatement) {
				MethodStatement ms1 = (MethodStatement) statement;
				result = prime * result + ms1.getMethod().getDeclaringClass().hashCode()
				        + ms1.getMethod().hashCode();
			} else if (statement instanceof ConstructorStatement) {
				ConstructorStatement ms1 = (ConstructorStatement) statement;
				result = prime * result
				        + ms1.getConstructor().getDeclaringClass().hashCode()
				        + ms1.getConstructor().hashCode();
			} else {
				result = prime * result + statement.hashCode();
			}
		}

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		// Same contract?
		ContractViolation other = (ContractViolation) obj;
		if (contract == null) {
			if (other.contract != null)
				return false;
		} else if (!contract.equals(other.contract))
			return false;

		// Same call?
		if (statement == null) {
			if (other.statement != null)
				return false;
		} else {
			if (!statement.getClass().equals(other.statement.getClass()))
				return false;
			else {
				if (statement instanceof MethodStatement) {
					MethodStatement ms1 = (MethodStatement) statement;
					MethodStatement ms2 = (MethodStatement) other.statement;
					if (ms1.getMethod().equals(ms2.getMethod())) {
						return true;
						//} else {
						//	logger.info("Unequal methods: " + ms1.getCode() + " / "
						//	        + ms2.getCode());
					}
				} else if (statement instanceof ConstructorStatement) {
					ConstructorStatement ms1 = (ConstructorStatement) statement;
					ConstructorStatement ms2 = (ConstructorStatement) other.statement;
					if (ms1.getConstructor().equals(ms2.getConstructor())) {
						return true;
					}
				}
			}
		}

		return false;
	}

}
