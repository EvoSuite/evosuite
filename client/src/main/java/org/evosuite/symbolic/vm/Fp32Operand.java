package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.fp.RealValue;

/**
 * 
 * @author galeotti
 *
 */
public final class Fp32Operand implements SingleWordOperand, RealOperand {
	
	private final RealValue realExpr;
	
	public Fp32Operand(RealValue realExpr) {
		this.realExpr=realExpr;
	}

	@Override
	public RealValue getRealExpression() {
		return realExpr;
	}
	
	@Override
	public String toString() {
		return realExpr.toString();
	}
}