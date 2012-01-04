/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

/**
 * @author krusev
 *
 */
public class StringComparison extends StringExpression {

	private static final long serialVersionUID = -2959676064390810341L;

	public StringComparison(Expression<String> left, Operator op, Expression<?> right2, Long con) {
		super();
		this.left = left;
		this.op = op;
		this.right = right2;
		this.conVal = con;
	}

	protected final Long conVal;
	protected final Operator op;
	protected final Expression<String> left;
	protected final Expression<?> right;

	@Override
	public Long getConcreteValue() {
		return conVal;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringComparison) {
			StringComparison other = (StringComparison) obj;
			return this.op.equals(other.op) && this.conVal.equals(other.conVal) 
					&& this.getSize() == other.getSize() && this.left.equals(other.left) 
					&& this.right.equals(other.right);
		}

		return false;
	}

	public Expression<?> getRightOperand() {
		return right;
	}

	public Expression<String> getLeftOperand() {
		return left;
	}

	@Override
	public String toString() {
		return "(" + left + op.toString() + right + ")";
	}

	protected int size = 0;

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + getLeftOperand().getSize() + getRightOperand().getSize();
		}
		return size;
	}

	public Operator getOperator() {
		return op;
	}

}