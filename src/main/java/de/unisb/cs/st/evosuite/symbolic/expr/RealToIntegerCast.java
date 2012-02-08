package de.unisb.cs.st.evosuite.symbolic.expr;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

public class RealToIntegerCast extends IntegerExpression implements Cast<Double> {
	private static final long serialVersionUID = 1L;

	protected Long concValue;

	protected Expression<Double> expr;

	public RealToIntegerCast(Expression<Double> _expr, Long _concValue) {
		this.expr = _expr;
		this.concValue = _concValue;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	@Override
	public Long getConcreteValue() {
		return concValue;
	}

	@Override
	public Expression<Double> getConcreteObject() {
		return expr;
	}

	@Override
	public String toString() {
		return "((INT)" + expr + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RealToIntegerCast) {
			RealToIntegerCast other = (RealToIntegerCast) obj;
			return this.expr.equals(other.expr)
								 && this.getSize()==other.getSize();
		}

		return false;
	}

	protected int size=0;
	@Override
	public int getSize() {
		if(size == 0)
		{
			size=1+ expr.getSize();
		}
		return size;
	}

	@Override
	public Long execute() {
		return ((Number) expr.execute()).longValue();
	}

}
