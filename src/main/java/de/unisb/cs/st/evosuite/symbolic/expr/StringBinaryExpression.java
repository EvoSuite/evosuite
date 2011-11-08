/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

/**
 * @author krusev
 *
 */
public class StringBinaryExpression extends StringExpression implements
BinaryExpression<String>{

	private static final long serialVersionUID = -986689442489666986L;

	protected String concretValue;

	protected Operator op;

	protected Expression<String> left;
	protected Expression<?> right;

	public StringBinaryExpression(Expression<String> left2, Operator op2,
	        Expression<?> right2, String con) {
		this.concretValue = con;
		this.left = left2;
		this.right = right2;
		this.op = op2;
	}

	@Override
	public String getConcreteValue() {
		return concretValue;
	}

	@Override
	public Operator getOperator() {
		return op;
	}

	@Override
	public Expression<String> getLeftOperand() {
		return left;
	}

	@Override
	public Expression<?> getRightOperand() {
		return right;
	}

	@Override
	public String toString() {
		return "(" + left + op.toString() + right + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringBinaryExpression) {
			StringBinaryExpression other = (StringBinaryExpression) obj;
			return this.op.equals(other.op) && this.getSize() == other.getSize()
			        && this.left.equals(other.left) && this.right.equals(other.right);
		}

		return false;
	}

	protected int size = 0;

	@Override
	public int getSize() {
		//TODO fix this
		return -1;
//		if (size == 0) {
//			size = 1 + getLeftOperand().getSize() + getRightOperand().getSize();
//		}
//		return size;
	}

}
