package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.expr.RealExpression;

public final class Fp32Operand implements SingleWordOperand, RealOperand {
	
	private final RealExpression realExpr;
	
	public Fp32Operand(RealExpression realExpr) {
		this.realExpr=realExpr;
	}

	@Override
	public RealExpression getRealExpression() {
		return realExpr;
	}
	
	@Override
	public String toString() {
		return realExpr.toString();
	}
}