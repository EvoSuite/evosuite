package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.expr.StringExpression;

public final class StringReferenceOperand extends ReferenceOperand {

	private StringExpression strExpr;

	public StringReferenceOperand(StringExpression strExpr) {
		super(strExpr.getConcreteValue());
		this.strExpr = strExpr;
	}

	public StringExpression getStringExpression() {
		return strExpr;
	}

}
