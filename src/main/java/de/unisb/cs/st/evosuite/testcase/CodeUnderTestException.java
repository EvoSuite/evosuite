package de.unisb.cs.st.evosuite.testcase;

import org.apache.log4j.Logger;

/**
 * Used to wrap exceptions thrown in code under test. This is needed as VariableReference.getObjects/.setObject 
 * and AbstractStatement.execute() do not operate on the same layer.
 * 
 * With the introduction of FieldReferences VariableReferences can throw arbitrary (of cource wrapped) exceptions, 
 * as a Field.get() can trigger static{} blocks
 * 
 * @author Sebastian Steenbuck
 *
 */
public class CodeUnderTestException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CodeUnderTestException.class);
	
	public CodeUnderTestException(Throwable cause){
		super(cause);
	}

	/**
	 * Used by code calling VariableReference.setObject/2 and .getObject()/1 
	 * @param e
	 * @return only there to make the compiler happy, this method always throws an exception
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NullPointerException
	 * @throws ExceptionInInitializerError
	 * @throws AssertionError if e wasn't one of listed for types
	 */
	public static Error throwException(Throwable e) throws IllegalAccessException, IllegalArgumentException, NullPointerException, ExceptionInInitializerError{
		if(e instanceof IllegalAccessException){
			throw (IllegalAccessException)e;
		}else if(e instanceof IllegalArgumentException){
			throw (IllegalArgumentException)e;
		}else if(e instanceof NullPointerException){
			throw (NullPointerException)e;
		}else if(e instanceof ArrayIndexOutOfBoundsException){
			throw (ArrayIndexOutOfBoundsException)e;
		}else if(e instanceof ExceptionInInitializerError){
			throw (ExceptionInInitializerError)e;
		}else{
			logger.error("We expected the exception to be one of the listed but it was", e);
			throw new AssertionError("We expected the exception to be one of the listed but it was" + e.getClass());
		}
	}
}
