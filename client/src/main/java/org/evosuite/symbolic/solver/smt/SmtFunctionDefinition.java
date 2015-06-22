package org.evosuite.symbolic.solver.smt;

public final class SmtFunctionDefinition {

	private final String functionDefinition;

	public SmtFunctionDefinition(String functionDefinition) {
		this.functionDefinition = functionDefinition;
	}

	public String getFunctionDefinition() {
		return functionDefinition;
	}
}
