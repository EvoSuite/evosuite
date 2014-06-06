package org.evosuite.symbolic.expr.token;

import java.util.Set;
import java.util.StringTokenizer;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.str.StringValue;

public final class NextTokenizerExpr extends TokenizerExpr {

	private final TokenizerExpr tokenizerExpr;

	public NextTokenizerExpr(TokenizerExpr expr) {
		super(1 + expr.getSize(), expr.containsSymbolicVariable());
		tokenizerExpr = expr;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException(getSize());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5041244020293557448L;

	@Override
	public StringTokenizer execute() {
		StringTokenizer tokenizer = this.tokenizerExpr.execute();
		tokenizer.nextToken();
		return tokenizer;
	}

	@Override
	public Set<Variable<?>> getVariables() {
		return tokenizerExpr.getVariables();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj instanceof NextTokenizerExpr) {
			NextTokenizerExpr that = this;
			return this.tokenizerExpr.equals(that.tokenizerExpr);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return tokenizerExpr.hashCode();
	}

	@Override
	public String toString() {
		return "NEXT_TOKEN(" + tokenizerExpr.toString() + ")";
	}

	@Override
	public StringValue getDelimiter() {
		return tokenizerExpr.getDelimiter();
	}

	@Override
	public StringValue getString() {
		return tokenizerExpr.getString();
	}

	@Override
	public int getNextTokenCount() {
		return 1 + tokenizerExpr.getNextTokenCount();
	}

	@Override
	public Set<Object> getConstants() {
		return tokenizerExpr.getConstants();
	}
}
