/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

/**
 * @author fraser
 * 
 */
public interface OutputTraceVisitor<T extends OutputTraceEntry> {

	public void visit(T entry);
}
