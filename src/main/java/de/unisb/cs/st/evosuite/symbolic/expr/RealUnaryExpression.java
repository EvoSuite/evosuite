package de.unisb.cs.st.evosuite.symbolic.expr;

public class RealUnaryExpression extends RealExpression implements
		UnaryExpression<Double> {
	private static final long serialVersionUID = 1L;

	protected Double concretValue;
	
	protected Operator op;
	
	protected Expression<Double> expr;

	public RealUnaryExpression(Expression<Double> e, Operator op2, Double con) {
		this.expr=e;
		this.op=op2;
		this.concretValue=con;
	}

	@Override
	public Double getConcreteValue() {
		return concretValue;
	}

	@Override
	public Expression<Double> getOperand() {
		return expr;
	}

	@Override
	public Operator getOperator() {
		return op;
	}
	
	@Override
	public String toString() {
		return "("+op.toString()+"("+expr+"))";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RealUnaryExpression)
		{
			RealUnaryExpression v=(RealUnaryExpression) obj;
			return this.op.equals(v.op) && this.getSize()==v.getSize()  && this.expr.equals(v.expr);
		}
		return false;
	}
	
	protected int size=0;
	@Override
	public int getSize() {
		if(size == 0)
		{
			size=1+ getOperand().getSize();
		}
		return size;
	}

	@Override
	public Object execute() {
		// TODO Auto-generated method stub
		return null;
	}

}
