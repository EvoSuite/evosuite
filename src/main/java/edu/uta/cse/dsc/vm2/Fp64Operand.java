package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.expr.RealExpression;

public final class Fp64Operand implements DoubleWordOperand {
	private final RealExpression realExpr;

	public Fp64Operand(RealExpression realExpr) {
		this.realExpr = realExpr;
	}

	public RealExpression getRealExpression() {
		return realExpr;
	}

	@Override
	public String toString() {
		return realExpr.toString();
	}
	
}