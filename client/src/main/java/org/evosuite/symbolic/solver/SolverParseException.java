package org.evosuite.symbolic.solver;

public final class SolverParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5368822908522148969L;

	private final String solverResult;

	public SolverParseException(String msg, String notParsedSolverResult) {
		super(msg);
		this.solverResult = notParsedSolverResult;
	}

	public String getNotParsedResult() {
		return solverResult;
	}
}
