package de.unisb.cs.st.evosuite.symbolic.expr;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

public class IntegerToRealCast extends RealExpression implements Cast<Long> {
	private static final long serialVersionUID = -3070453617714122236L;

	protected Double concreteValue;

	protected Expression<Long> expr;

	public IntegerToRealCast(Expression<Long> _expr, Double _concValue) {
		this.expr = _expr;
		this.concreteValue = _concValue;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	@Override
	public Double getConcreteValue() {
		return concreteValue;
	}

	@Override
	public Expression<Long> getConcreteObject() {
		return expr;
	}

	@Override
	public String toString() {
		return "((REAL)" + expr + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntegerToRealCast) {
			IntegerToRealCast other = (IntegerToRealCast) obj;
			return this.expr.equals(other.expr);
			//					 && this.getSize() == other.getSize();
		}

		return false;
	}

	protected int size = 0;
	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + expr.getSize();
		}
		return size;
	}

	@Override
	public Double execute() {
		return ((Number) expr.execute()).doubleValue();
	}
}
