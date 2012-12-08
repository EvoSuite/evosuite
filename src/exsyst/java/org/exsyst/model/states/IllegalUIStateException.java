package org.exsyst.model.states;

public class IllegalUIStateException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	public IllegalUIStateException() {
		super();
	}

	public IllegalUIStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalUIStateException(String message) {
		super(message);
	}

	public IllegalUIStateException(Throwable cause) {
		super(cause);
	}
}
