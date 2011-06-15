package de.unisb.cs.st.evosuite.symbolic.expr;

public class IntegerBinaryExpression extends IntegerExpression implements BinaryExpression<Long> {

	private static final long serialVersionUID = -986689442489666986L;

	protected Long concretValue;

	protected Operator op;

	protected Expression<Long> left;
	protected Expression<Long> right;

	protected int size = 0;

	public IntegerBinaryExpression(Expression<Long> left2, Operator op2, Expression<Long> right2, Long con) {
		this.concretValue = con;
		this.left = left2;
		this.right = right2;
		this.op = op2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntegerBinaryExpression) {
			IntegerBinaryExpression other = (IntegerBinaryExpression) obj;
			return this.op.equals(other.op) && (this.getSize() == other.getSize()) && this.left.equals(other.left)
					&& this.right.equals(other.right);
		}

		return false;
	}

	@Override
	public Long getConcreteValue() {
		return concretValue;
	}

	@Override
	public Expression<Long> getLeftOperand() {
		return left;
	}

	@Override
	public Operator getOperator() {
		return op;
	}

	@Override
	public Expression<Long> getRightOperand() {
		return right;
	}

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + getLeftOperand().getSize() + getRightOperand().getSize();
		}
		return size;
	}

	@Override
	public String toString() {
		return "(" + left + op.toString() + right + ")";
	}

}
