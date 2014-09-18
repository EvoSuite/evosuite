package org.evosuite.runtime.mock.java.io;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.java.lang.MockThrowable;

public class MockIOException extends IOException  implements OverrideMock{

	/*
	 * "Exception" class only defines constructors, like all (?) its subclasses.
	 * So, just need to override constructors, and delegate methods.
	 * 
	 *  All subclasses will have same code, albeit with different class names.
	 *  Unfortunately, we end up with copy&paste, which cannot be avoided, as
	 *  we cannot have multi-inheritance. 
	 *  
	 *  WARN: any change would likely end up in having to redo the copy&paste :(  
	 */

	private static final long serialVersionUID = 8001149552489118355L;

	/**
	 * Instead of copy&paste functionalities from MockThrowable, use a delegate
	 */
	private volatile MockThrowable delegate;

	/*
	 * This is needed for when super constructors call overridden methods,
	 * and proper delegate method (right inputs) is not instantiated yet  
	 */
	private MockThrowable getDelegate(){
		if(delegate == null){
			delegate = new MockThrowable(); //placeholder
		}
		return delegate;
	}

	// ----- constructor --------

	public MockIOException() {
		super();
		delegate = new MockThrowable();
	}

	public MockIOException(String message) {
		super(message);
		delegate = new MockThrowable(message);
	}

	public MockIOException(Throwable cause) {
		super(cause);
		delegate = new MockThrowable(cause);
	}

	public MockIOException(String message, Throwable cause) {
		super(message, cause);
		delegate = new MockThrowable(message, cause);
	}


	// ----- delegation methods -------

	@Override
	public String getMessage() {		
		if(!MockFramework.isEnabled()){
			return super.getMessage();
		}		
		return getDelegate().getMessage();
	}

	@Override
	public String getLocalizedMessage() {		
		if(!MockFramework.isEnabled()){
			return super.getLocalizedMessage();
		}		
		return getDelegate().getLocalizedMessage();
	}

	@Override
	public synchronized Throwable getCause() {
		if(!MockFramework.isEnabled()){
			return super.getCause();
		}
		return getDelegate().getCause();
	}

	@Override
	public String toString() {
		if(!MockFramework.isEnabled()){
			return super.toString();
		}
		return getDelegate().toString();
	}

	@Override
	public void printStackTrace() {
		if(!MockFramework.isEnabled()){
			super.printStackTrace();
			return;
		}
		getDelegate().printStackTrace();
	}


	@Override
	public synchronized Throwable initCause(Throwable cause) {
		if(!MockFramework.isEnabled()){
			return super.initCause(cause);
		}
		return getDelegate().initCause(cause);
	}

	@Override
	public void printStackTrace(PrintStream p) {
		if(!MockFramework.isEnabled()){
			super.printStackTrace(p);
			return;
		}
		getDelegate().printStackTrace(p);
	}

	public void printStackTrace(PrintWriter p) {
		if(!MockFramework.isEnabled()){
			super.printStackTrace(p);
			return;
		}
		getDelegate().printStackTrace(p);
	}


	@Override
	public synchronized Throwable fillInStackTrace() {
		if(!MockFramework.isEnabled()){
			return super.fillInStackTrace();
		}
		return getDelegate().fillInStackTrace();
	}

	@Override
	public StackTraceElement[] getStackTrace() {		
		if(!MockFramework.isEnabled()){
			return super.getStackTrace();
		}
		return getDelegate().getStackTrace();
	}

	@Override
	public void setStackTrace(StackTraceElement[] stackTrace) {
		if(!MockFramework.isEnabled()){
			super.setStackTrace(stackTrace);
			return;
		}
		getDelegate().setStackTrace(stackTrace);
	}
}
