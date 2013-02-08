package org.evosuite.symbolic.expr.token;

import java.util.StringTokenizer;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.str.StringValue;

public abstract class TokenizerExpr extends AbstractExpression<StringTokenizer> {

	public TokenizerExpr(int size,
			boolean containsSymbolicVariable) {
		super(null, size, containsSymbolicVariable);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7584961134006709947L;

	public abstract StringValue getDelimiter();
	public abstract StringValue getString();
	public abstract int getNextTokenCount();
	
}
