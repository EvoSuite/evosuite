package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.StringExpression;

/**
 * 
 * @author galeotti
 *
 */
public final class StringReference extends NonNullReference {

	private StringExpression strExpr;

	public StringReference(StringExpression strExpr) {
		super("java.lang.String", -1);
		this.strExpr = strExpr;
	}

	public StringExpression getStringExpression() {
		return strExpr;
	}

	@Override
	public String toString() {
		return strExpr.toString();
	}
}
