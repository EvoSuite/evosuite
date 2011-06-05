package de.unisb.cs.st.evosuite.symbolic.expr;

public abstract class RealExpression implements Expression<Double> {

	private static final long serialVersionUID = 6773549380102315977L;

	protected int hash = 0;

	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = this.getConcreteValue().hashCode();
		}
		return hash;
	}
}
