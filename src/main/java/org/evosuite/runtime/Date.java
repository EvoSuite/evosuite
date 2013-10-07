/**
 * 
 */
package org.evosuite.runtime;

/**
 * @author Gordon Fraser
 * 
 */
public class Date {

	public static java.util.Date getDate() {
		long currentMillis = System.currentTimeMillis();
		return new java.util.Date(currentMillis);
	}
}
