/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

/**
 * @author krusev
 *
 */
public abstract interface Cast<T> {

	public Expression<T> getConcreteObject();
}
