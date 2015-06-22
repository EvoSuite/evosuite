package org.evosuite.symbolic.solver.smt;

public final class SmtConstantDeclaration {

	private final String name;
	private final String sort;

	public SmtConstantDeclaration(String constantName, String constantSort) {
		this.name = constantName;
		this.sort = constantSort;
	}

	public String getConstantName() {
		return name;
	}

	public String getConstantSort() {
		return sort;
	}

}
