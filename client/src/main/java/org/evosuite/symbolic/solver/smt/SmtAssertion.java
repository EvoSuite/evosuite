package org.evosuite.symbolic.solver.smt;

public final class SmtAssertion {

	private final SmtExpr formula;

	public SmtAssertion(SmtExpr f) {
		this.formula = f;
	}

	public SmtExpr getFormula() {
		return formula;
	}

}
