package de.unisb.cs.st.evosuite.symbolic.expr;

public abstract class IntegerExpression implements Expression<Long> {

	private static final long serialVersionUID = 2896502683190522448L;

	protected int hash = 0;

	@Override
	public int hashCode() {
		if (hash == 0) {
			if (this.getConcreteValue() != null) {
				hash = this.getConcreteValue().hashCode();
			} else {
				hash = 1;
			}
		}
		return hash;

	}

}
