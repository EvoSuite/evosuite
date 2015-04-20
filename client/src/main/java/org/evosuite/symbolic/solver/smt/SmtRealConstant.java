package org.evosuite.symbolic.solver.smt;

public final class SmtRealConstant extends SmtConstant {

	private final double doubleValue;

	public SmtRealConstant(double realConstant) {
		this.doubleValue = realConstant;
	}

	public double getConstantValue() {
		return doubleValue;
	}
	
	@Override
	public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
		return v.visit(this,arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(doubleValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		SmtRealConstant other = (SmtRealConstant) obj;
		if (Double.doubleToLongBits(doubleValue) != Double
				.doubleToLongBits(other.doubleValue))
			return false;
		return true;
	}
}
