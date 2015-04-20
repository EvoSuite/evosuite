package org.evosuite.symbolic.solver.smt;

public final class SmtStringVariable extends SmtVariable {

	public SmtStringVariable(String varName) {
		super(varName);
	}

	@Override
	public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
		return v.visit(this,arg);
	}
}
