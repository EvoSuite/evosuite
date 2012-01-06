package de.unisb.cs.st.evosuite.symbolic.expr;

public class IntegerUnaryExpression extends IntegerExpression implements
        UnaryExpression<Long> {

	private static final long serialVersionUID = 1966395070897274841L;

	protected Long concretValue;

	protected Operator op;

	protected Expression<Long> expr;

	public IntegerUnaryExpression(Expression<Long> e, Operator op2, Long con) {
		this.expr = e;
		this.op = op2;
		this.concretValue = con;
	}

	@Override
	public Long getConcreteValue() {
		return concretValue;
	}

	@Override
	public Expression<Long> getOperand() {
		return expr;
	}

	@Override
	public Operator getOperator() {
		return op;
	}

	@Override
	public String toString() {
		return "(" + op.toString() + "(" + expr + "))";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntegerUnaryExpression) {
			IntegerUnaryExpression v = (IntegerUnaryExpression) obj;
			return this.op.equals(v.op) && this.getSize() == v.getSize()
			        && this.expr.equals(v.expr);
		}
		return false;
	}

	protected int size = 0;

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + getOperand().getSize();
		}
		return size;
	}

	@Override
	public Object execute() {
		// TODO Auto-generated method stub
		return null;
	}

}
