package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.expr.IntegerExpression;

public final class Bv32Operand implements SingleWordOperand {

	private final IntegerExpression integerExpr;

	public Bv32Operand(IntegerExpression integerExpr) {
		this.integerExpr = integerExpr;
	}

	public IntegerExpression getIntegerExpression() {
		return integerExpr;
	}
}