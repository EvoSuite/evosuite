package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.IntegerExpression;

/**
 * 
 * @author galeotti
 *
 */
public final class Bv32Operand implements SingleWordOperand, IntegerOperand {

	private final IntegerExpression integerExpr;

	public Bv32Operand(IntegerExpression integerExpr) {
		this.integerExpr = integerExpr;
	}

	@Override
	public IntegerExpression getIntegerExpression() {
		return integerExpr;
	}

	@Override
	public String toString() {
		return integerExpr.toString();
	}
}