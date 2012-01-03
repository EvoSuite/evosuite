/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

/**
 * @author krusev
 *
 */
public abstract class StringExpression implements Expression<String> {

	private static final long serialVersionUID = -1510660880687868188L;
	
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
			if (this.getConcreteValue() != null) {
				hash = this.getConcreteValue().hashCode();
			} else {
				hash = 1;
			}
		}
		return hash;

	}
}
