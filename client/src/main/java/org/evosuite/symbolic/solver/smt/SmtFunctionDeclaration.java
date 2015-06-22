package org.evosuite.symbolic.solver.smt;

public final class SmtFunctionDeclaration {

	private final String functionName;

	private final String functionSort;

	public SmtFunctionDeclaration(String funcName, String funcSort) {
		this.functionName = funcName;
		this.functionSort = funcSort;
	}

	public String getFunctionName() {
		return functionName;
	}

	public String getFunctionSort() {
		return functionSort;
	}
}
