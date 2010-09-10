/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;

/**
 * @author fraser
 *
 */
public class NullReference extends VariableReference {

	/**
	 * @param type
	 * @param position
	 */
	public NullReference(Type type) {
		super(type, -1);
	}

	/**
	 * Return name for source code representation
	 * @return
	 */
	public String getName() {
		return "null";
	}
	
	/**
	 * Add delta to the position of all variables up to a position
	 * @param delta
	 *    The delta that will be added to the position of each variable
	 * @param position
	 *    The maximum position up to which variables are changed
	 */
	public void adjust(int delta, int position) {
		// Do nothing
	}

	/**
	 * Create a copy of the current variable
	 */
	public VariableReference clone() {
		return new NullReference(getType());
	}
}
