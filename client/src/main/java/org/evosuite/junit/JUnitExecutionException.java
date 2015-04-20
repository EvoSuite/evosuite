package org.evosuite.junit;


public class JUnitExecutionException extends Exception {

	public JUnitExecutionException(String message) {
		super(message);
	}

	public JUnitExecutionException(Exception e) {
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 9063744097191003972L;

}
