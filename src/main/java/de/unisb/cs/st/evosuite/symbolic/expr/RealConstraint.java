package de.unisb.cs.st.evosuite.symbolic.expr;

public class RealConstraint extends Constraint<Double> {

	private static final long serialVersionUID = 6021027178547577289L;

	public RealConstraint(Expression<Double> left, Comparator cmp,
	        Expression<Double> right) {
		super();
		this.left = left;
		this.cmp = cmp;
		this.right = right;
	}

	protected Comparator cmp;

	protected Expression<Double> left;
	protected Expression<Double> right;

	@Override
	public Comparator getComparator() {
		return cmp;
	}

	@Override
	public Expression<Double> getLeftOperand() {
		return left;
	}

	@Override
	public Expression<Double> getRightOperand() {
		return right;
	}

	@Override
	public String toString() {
		return left + cmp.toString() + right;
	}

}
