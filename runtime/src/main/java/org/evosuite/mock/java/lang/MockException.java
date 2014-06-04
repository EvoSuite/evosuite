package org.evosuite.mock.java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.evosuite.runtime.Thread;

public class MockException extends Exception {

	private static final long serialVersionUID = 8001149552489118355L;

	public MockException() {
		
	}
	
	public MockException(String message) {
		super(message);
	}

	public MockException(Throwable cause) {
		super(cause);
	}

	public MockException(String message, Throwable cause) {
		super(message, cause);
	}
	
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
	
	public void printStackTrace(PrintStream p) {
		for(StackTraceElement elem : Thread.getStackTrace()) {
			p.append(elem.toString());
			p.append("\n");
		}
	}
	
	public void printStackTrace(PrintWriter p) {
		for(StackTraceElement elem : Thread.getStackTrace()) {
			p.append(elem.toString());
			p.append("\n");
		}		
	}
	
	public String toString() {
		return getClass().getCanonicalName();
	}

	public String getMessage() {
		return getClass().getCanonicalName();
	}
	
	public StackTraceElement[] getStackTrace() {
		StackTraceElement[] stack = new StackTraceElement[3];
		stack[0] = new StackTraceElement("<evosuite>", "<evosuite>", "<evosuite>", -1);
		stack[1] = new StackTraceElement("<evosuite>", "<evosuite>", "<evosuite>", -1);
		stack[2] = new StackTraceElement("<evosuite>", "<evosuite>", "<evosuite>", -1);
		return stack;
	}
}
