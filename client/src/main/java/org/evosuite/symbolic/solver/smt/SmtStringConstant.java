package org.evosuite.symbolic.solver.smt;

public final class SmtStringConstant extends SmtConstant {

	private final String stringValue;

	public SmtStringConstant(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getConstantValue() {
		return stringValue;
	}
	
	@Override
	public <K, V> K accept(SmtExprVisitor<K, V> v, V arg) {
		return v.visit(this,arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((stringValue == null) ? 0 : stringValue.hashCode());
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
		SmtStringConstant other = (SmtStringConstant) obj;
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		} else if (!stringValue.equals(other.stringValue))
			return false;
		return true;
	}
}
