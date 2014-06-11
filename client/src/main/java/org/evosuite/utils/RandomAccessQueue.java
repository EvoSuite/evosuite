/**
 * 
 */
package org.evosuite.utils;

/**
 * @author Gordon Fraser
 * 
 */
public interface RandomAccessQueue<T> {

	public void restrictedAdd(T value);

	public T getRandomValue();
}
