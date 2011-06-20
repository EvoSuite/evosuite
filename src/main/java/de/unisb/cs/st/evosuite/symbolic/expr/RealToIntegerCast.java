package de.unisb.cs.st.evosuite.symbolic.expr;

public class RealToIntegerCast extends IntegerExpression {
	private static final long serialVersionUID = 1L;

	protected Long concretValue;
	
	protected Expression<Double> expr;
	
	public RealToIntegerCast(Expression<Double> myExpressionFromJeremeysExpresion,Long concretValue) {
		this.expr=myExpressionFromJeremeysExpresion;
		this.concretValue=concretValue;
	}

	@Override
	public Long getConcreteValue() {
		return concretValue;
	}
	
	@Override
	public String toString() {
		return "((INT)"+expr+")";
	}

	public Expression<Double> getExpression() {
		return expr;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this)
		{
			return true;
		}
		if(obj instanceof RealToIntegerCast)
		{
			RealToIntegerCast other=(RealToIntegerCast) obj;
			return this.getSize()==other.getSize() && this.expr.equals(other.expr);
		}

		return false;
	}
	
	protected int size=0;
	@Override
	public int getSize() {
		if(size == 0)
		{
			size=1+ getExpression().getSize();
		}
		return size;
	}

}
