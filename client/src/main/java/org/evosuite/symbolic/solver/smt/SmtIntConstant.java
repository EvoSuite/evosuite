package org.evosuite.symbolic.solver.smt;

public final class SmtIntConstant extends SmtConstant {

	private final long longValue;

	public SmtIntConstant(int constantValue) {
		this.longValue = constantValue;
	}

	public SmtIntConstant(long constantValue) {
		this.longValue = constantValue;
	}

	public long getConstantValue() {
		return longValue;
	}

	@Override
	public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
		return v.visit(this,arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (longValue ^ (longValue >>> 32));
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
		SmtIntConstant other = (SmtIntConstant) obj;
		if (longValue != other.longValue)
			return false;
		return true;
	}
}
