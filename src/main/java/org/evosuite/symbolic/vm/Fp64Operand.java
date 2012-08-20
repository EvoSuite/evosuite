package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.RealExpression;

/**
 * 
 * @author galeotti
 *
 */
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