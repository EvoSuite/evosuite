package de.unisb.cs.st.evosuite.symbolic.expr;

public class IntegerToRealCast extends RealExpression {
	private static final long serialVersionUID = -3070453617714122236L;

	protected Double concreteValue;

	protected Expression<Long> expr;

	public IntegerToRealCast(Expression<Long> myExpressionFromJeremeysExpresion,
	        Double concreteValue) {
		this.expr = myExpressionFromJeremeysExpresion;
		this.concreteValue = concreteValue;
	}

	@Override
	public Double getConcreteValue() {
		return concreteValue;
	}

	@Override
	public String toString() {
		return "((REAL)" + expr + ")";
	}

	public Expression<Long> getExpression() {
		return expr;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntegerToRealCast) {
			IntegerToRealCast other = (IntegerToRealCast) obj;
			return this.getSize() == other.getSize() && this.expr.equals(other.expr);
		}

		return false;
	}

	protected int size = 0;

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + getExpression().getSize();
		}
		return size;
	}

	@Override
	public Object execute() {
		// TODO Auto-generated method stub
		return null;
	}

}
