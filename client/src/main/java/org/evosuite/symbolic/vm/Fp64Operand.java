package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.fp.RealValue;

/**
 * 
 * @author galeotti
 *
 */
public final class Fp64Operand implements DoubleWordOperand, RealOperand {
	private final RealValue realExpr;

	public Fp64Operand(RealValue realExpr) {
		this.realExpr = realExpr;
	}

	public RealValue getRealExpression() {
		return realExpr;
	}

	@Override
	public String toString() {
		return realExpr.toString();
	}
	
}