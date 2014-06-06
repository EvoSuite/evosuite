package org.evosuite.symbolic.expr.reader;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.str.StringValue;

public final class StringReaderExpr extends AbstractExpression<Long> implements
		IntegerValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = -744964586007203884L;
	/**
	 * 
	 */
	private final StringValue string;
	private final int readerPosition;

	public StringReaderExpr(Long conc_value, StringValue string) {
		this(conc_value, string, 0);
	}

	public StringReaderExpr(Long conc_value, StringValue string, int readerPosition) {
		super(conc_value, 1 + string.getSize(), string.containsSymbolicVariable());

		this.string = string;
		this.readerPosition = readerPosition;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException(getSize());
	}

	@Override
	public Long execute() {
		String conc_string = this.string.execute();
		if (readerPosition >= conc_string.length()) {
			return -1L;
		} else {
			return (long) conc_string.charAt(readerPosition);
		}
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		variables.addAll(this.string.getVariables());
		return variables;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this == obj)
			return true;

		if (obj instanceof StringReaderExpr) {
			StringReaderExpr that = (StringReaderExpr) obj;
			return this.string.equals(that.string)
					&& this.readerPosition == that.readerPosition;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return string.hashCode() + readerPosition;
	}

	@Override
	public String toString() {
		String toString = String.format("STRING_READER(%s, %s)",
				string.toString(), readerPosition);
		return toString;
	}

	public int getReaderPosition() {
		return readerPosition;
	}

	public StringValue getString() {
		return string;
	}

	@Override
	public Set<Object> getConstants() {
		Set<Object> result = new HashSet<Object>();
		result.add(string.getConcreteValue());
		return result;
	}
}
