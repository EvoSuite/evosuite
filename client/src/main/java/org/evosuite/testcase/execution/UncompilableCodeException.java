package org.evosuite.testcase.execution;

public class UncompilableCodeException extends RuntimeException {

	private static final long serialVersionUID = 2111256673991944796L;

	public UncompilableCodeException() {
		super();
	}

	public UncompilableCodeException(String message) {
		super(message);
	}
}
