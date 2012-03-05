package de.unisb.cs.st.evosuite.symbolic.expr;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

public class IntegerComparison extends IntegerExpression {

	private static final long serialVersionUID = 8551234172104612736L;

	public IntegerComparison(Expression<Long> left, Expression<Long> right, Long con) {
		super();
		this.left = left;
		this.right = right;
		this.con = con;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	private final Long con;
	private final Expression<Long> left;
	private final Expression<Long> right;

	@Override
	public Long getConcreteValue() {
		return con;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntegerComparison) {
			IntegerComparison other = (IntegerComparison) obj;
			return this.con.equals(other.con) 
					&& this.getSize() == other.getSize()
			        && this.left.equals(other.left) && this.right.equals(other.right);
		}

		return false;
	}

	public Expression<Long> getRightOperant() {
		return right;
	}

	public Expression<Long> getLeftOperant() {
		return left;
	}

	@Override
	public String toString() {
		return "(" + left + " cmp " + right + ")";
	}

	protected int size = 0;

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + left.getSize() + right.getSize();
		}
		return size;
	}

	@Override
	public Object execute() {
		// this is never used 
		return null;
	}

}
