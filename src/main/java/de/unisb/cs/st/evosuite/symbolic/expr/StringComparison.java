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

	public StringComparison(Expression<String> left, Operator op, Expression<String> right, Long con) {
		super();
		this.left = left;
		this.op = op;
		this.right = right;
		this.con = con;
	}

	protected final Long con;
	protected final Operator op;
	protected final Expression<String> left;
	protected final Expression<String> right;

	@Override
	public Long getConcreteValue() {
		return con;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringComparison) {
			StringComparison other = (StringComparison) obj;
			return this.op.equals(other.op) && this.con.equals(other.con) 
					&& this.getSize() == other.getSize() && this.left.equals(other.left) 
					&& this.right.equals(other.right);
		}

		return false;
	}

	public Expression<String> getRightOperant() {
		return right;
	}

	public Expression<String> getLeftOperant() {
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
			size = 1 + getLeftOperant().getSize() + getRightOperant().getSize();
		}
		return size;
	}

	public Operator getOperator() {
		return op;
	}

}
