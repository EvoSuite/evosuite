package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.RealExpression;

/**
 * 
 * @author galeotti
 *
 */
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