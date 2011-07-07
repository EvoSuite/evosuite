package de.unisb.cs.st.evosuite.testcase;

/**
 * This error can be used to signal an throwable from evosuite code, below the
 * class under test. E.g. the class under tests is instrumented to call the
 * method evosuite.something() which throws and error. If the error is of the
 * type EvosuiteError the exception is thrown. If it is of any other type, the
 * exception is catched and it is assumed, that the exception was thrown by the
 * class under test
 * 
 * @author Sebastian Steenbuck
 * 
 */
public class EvosuiteError extends Error {
	private static final long serialVersionUID = 454018150971425158L;

	public EvosuiteError(Throwable cause) {
		super(cause);
	}
	
	public EvosuiteError(String msg){
		super(msg);
	}
}
