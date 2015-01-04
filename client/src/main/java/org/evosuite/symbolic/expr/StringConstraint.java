package org.evosuite.symbolic.expr;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringComparison;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringConstraint extends Constraint<String> {

	static Logger log = LoggerFactory.getLogger(StringConstraint.class);

	public StringConstraint(StringComparison left, Comparator comp,
			IntegerConstant right) {
		super();
		this.left = left;
		this.cmp = comp;
		this.right = right;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
			DSEStats.reportConstraintTooLong(getSize());
			throw new ConstraintTooLongException(getSize());
		}
	}

	private final StringComparison left;
	private final Comparator cmp;
	private final IntegerConstant right;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3187023627540040535L;

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

	@Override
	public Constraint<String> negate() {
		return new StringConstraint(left, cmp.not(), right);
	}

	@Override
	public <K, V> K accept(ConstraintVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

}
