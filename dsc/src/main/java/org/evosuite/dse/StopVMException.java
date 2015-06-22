package org.evosuite.dse;

public abstract class StopVMException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9179045415806106855L;

	public StopVMException(String msg) {
		super(msg);
	}

	public StopVMException() {
		super();
	}

}
