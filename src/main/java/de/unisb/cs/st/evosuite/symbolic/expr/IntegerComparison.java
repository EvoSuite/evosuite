package de.unisb.cs.st.evosuite.symbolic.expr;

public class IntegerComparison extends IntegerExpression {

	private static final long serialVersionUID = 8551234172104612736L;

	private final Long con;

	private final Expression<Long> left;
	private final Expression<Long> right;
	protected int size = 0;

	public IntegerComparison(Expression<Long> left, Expression<Long> right, Long con) {
		super();
		this.left = left;
		this.right = right;
		this.con = con;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntegerComparison) {
			IntegerComparison other = (IntegerComparison) obj;
			return this.con.equals(other.con) && (this.getSize() == other.getSize()) && this.left.equals(other.left)
					&& this.right.equals(other.right);
		}

		return false;
	}

	@Override
	public Long getConcreteValue() {
		return con;
	}

	public Expression<Long> getLeftOperant() {
		return left;
	}

	public Expression<Long> getRightOperant() {
		return right;
	}

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + getLeftOperant().getSize() + getRightOperant().getSize();
		}
		return size;
	}

	@Override
	public String toString() {
		return "(" + left + " cmp " + right + ")";
	}

}
