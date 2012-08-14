package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.expr.IntegerExpression;

public final class Bv64Operand implements DoubleWordOperand, IntegerOperand {
	private final IntegerExpression integerExpr;

	public Bv64Operand(IntegerExpression integerExpr) {
		this.integerExpr = integerExpr;
	}

	@Override
	public IntegerExpression getIntegerExpression() {
		return integerExpr;
	}
}