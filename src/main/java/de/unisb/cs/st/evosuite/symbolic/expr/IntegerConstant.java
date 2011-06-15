package de.unisb.cs.st.evosuite.symbolic.expr;

public class IntegerConstant extends IntegerExpression {

	private static final long serialVersionUID = 3770747666367222441L;

	protected long value;

	public IntegerConstant(long longValue) {
		this.value = longValue;
	}

	@Override
	public Long getConcreteValue() {
		return value;
	}

	@Override
	public String toString() {
		return Long.toString(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntegerConstant) {
			IntegerConstant v = (IntegerConstant) obj;
			return this.value == v.value;
		}
		return false;
	}

	@Override
	public int getSize() {
		return 1;
	}

}
