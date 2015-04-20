package edu.uta.cse.dsc;

/**
 * This error can be used to signal an throwable from VM code, below the
 * instrumented class. E.g. the instrumented class calls the method
 * VM.something() which throws and error.
 * 
 * @author galeotti
 */
public class VMError extends Error {

	public VMError(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4986434593568038501L;

}
