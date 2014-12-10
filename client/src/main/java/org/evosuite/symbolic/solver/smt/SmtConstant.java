package org.evosuite.symbolic.solver.smt;

public abstract class SmtConstant extends SmtExpr {

	@Override
	public final boolean hasSymbolicValue() {
		return false;
	}

}
