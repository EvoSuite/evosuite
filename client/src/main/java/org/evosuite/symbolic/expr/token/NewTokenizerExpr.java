package org.evosuite.symbolic.expr.token;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.str.StringValue;

public final class NewTokenizerExpr extends TokenizerExpr {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6640965868758291282L;
	private final StringValue string;
	private final StringValue delim;

	public NewTokenizerExpr(StringValue string, StringValue delim) {
		super(1 + string.getSize() + delim.getSize(), string.containsSymbolicVariable()
		        || delim.containsSymbolicVariable());

		this.string = string;
		this.delim = delim;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException(getSize());
	}

	@Override
	public StringTokenizer execute() {
		String stringVal = string.execute();
		String delimVal = delim.execute();
		StringTokenizer tokenizer = new StringTokenizer(stringVal, delimVal);
		return tokenizer;
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		variables.addAll(this.string.getVariables());
		variables.addAll(this.delim.getVariables());
		return variables;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this == obj)
			return true;

		if (obj instanceof NewTokenizerExpr) {
			NewTokenizerExpr that = (NewTokenizerExpr) obj;
			return this.string.equals(that.string) && this.delim.equals(that.delim);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return string.hashCode() + delim.hashCode();
	}

	@Override
	public String toString() {
		String toString = String.format("TOKENIZE(%s, %s)", string.toString(),
		                                delim.toString());
		return toString;
	}

	@Override
	public StringValue getDelimiter() {
		return delim;
	}

	@Override
	public StringValue getString() {
		return string;
	}

	@Override
	public int getNextTokenCount() {
		return 0;
	}

	@Override
	public Set<Object> getConstants() {
		Set<Object> result = new HashSet<Object>();
		result.add(delim.getConcreteValue());
		result.add(string.getConcreteValue());
		return result;
	}
}
