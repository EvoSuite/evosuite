package de.unisb.cs.st.evosuite.symbolic.expr;

import java.io.Serializable;

//TODO <maybe wrong> changed Constraint<T extends Number> to the following
public abstract class Constraint<T extends Object> implements Serializable {

	private static final long serialVersionUID = 7547747352755232472L;

	abstract public Comparator getComparator();

	abstract public Expression<?> getLeftOperand();

	abstract public Expression<?> getRightOperand();

	private int hash = 0;

	@Override
	public int hashCode() {
		if (hash != 0) {

		} else {
			hash = getLeftOperand().hashCode() + getComparator().hashCode()
			        + getRightOperand().hashCode();
		}
		return hash;
	}

	protected int size = 0;

	public int getSize() {
		if (size == 0) {
			size = 1 + getLeftOperand().getSize() + getRightOperand().getSize();
		}
		return size;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Constraint<?>)) {
			return false;
		}

		Constraint<?> other = (Constraint<?>) obj;
		if (this.getComparator().equals(other.getComparator())
		        && this.getSize() == other.getSize()
		        && this.getLeftOperand().equals(other.getLeftOperand())
		        && this.getRightOperand().equals(other.getRightOperand())) {
			return true;
		}
		return false;
	}

	/**
	 * Sound but not complete
	 * 
	 * @return
	 */
	public boolean isSolveable() {
		if (getLeftOperand().equals(getRightOperand())) {
			if (getComparator() == Comparator.LT || getComparator() == Comparator.GT
			        || getComparator() == Comparator.NE) {
				return false;
			}
		}
		return true;
	}

}
