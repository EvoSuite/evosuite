package org.evosuite.symbolic.solver.smt;

public final class SmtBooleanConstant extends SmtConstant {

	private final boolean booleanValue;

	public SmtBooleanConstant(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	@Override
	public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

	public boolean booleanValue() {
		return booleanValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (booleanValue ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SmtBooleanConstant other = (SmtBooleanConstant) obj;
		if (booleanValue != other.booleanValue)
			return false;
		return true;
	}
}
