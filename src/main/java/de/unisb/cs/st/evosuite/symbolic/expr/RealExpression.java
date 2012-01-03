package de.unisb.cs.st.evosuite.symbolic.expr;

public abstract class RealExpression implements Expression<Double> {

	private static final long serialVersionUID = 6773549380102315977L;

	protected int hash = 0;

	
	private Expression<?> parent = null;
	
	public Expression<?> getParent() {
		return this.parent;
	}
	
	public void setParent(Expression<?> expr) {
		this.parent = expr;
	}

	
	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = this.getConcreteValue().hashCode();
		}
		return hash;
	}
}
