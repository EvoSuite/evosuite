package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.expr.IntegerExpression;

public final class Bv64Operand implements DoubleWordOperand {
	private final IntegerExpression integerExpr;

	public Bv64Operand(IntegerExpression integerExpr) {
		this.integerExpr = integerExpr;
	}

	public IntegerExpression getIntegerExpression() {
		return integerExpr;
	}
}