package org.evosuite.mock.java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.evosuite.runtime.Thread;

public class MockThrowable extends Throwable {

	private static final long serialVersionUID = 4078375023919805371L;

	public MockThrowable() {
		
	}
	
	public MockThrowable(String message) {
		super(message);
	}

	public MockThrowable(Throwable cause) {
		super(cause);
	}

	public MockThrowable(String message, Throwable cause) {
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
