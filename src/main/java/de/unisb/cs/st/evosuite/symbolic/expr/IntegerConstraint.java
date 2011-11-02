package de.unisb.cs.st.evosuite.symbolic.expr;

public class IntegerConstraint extends Constraint<Long> {

	private static final long serialVersionUID = 5345957507046422507L;

	public IntegerConstraint(Expression<?> left, Comparator cmp, Expression<?> right) {
		super();
		this.left = left;
		this.cmp = cmp;
		this.right = right;
	}

	protected Comparator cmp;

	protected Expression<?> left;
	protected Expression<?> right;

	@Override
	public Comparator getComparator() {
		return cmp;
	}

	@Override
	public Expression<?> getLeftOperand() {
		return left;
	}

	@Override
	public Expression<?> getRightOperand() {
		return right;
	}

	@Override
	public String toString() {
		return left + cmp.toString() + right;
	}

}
