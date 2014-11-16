package org.evosuite.ga.variables;

/**
 * Interface for variables
 * 
 * @author José Campos
 */
public interface Variable {

	/**
	 * 
	 * @return an independent clone of this variable
	 */
	public Variable clone();
}
