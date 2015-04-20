/**
 * 
 */
package org.evosuite.seeding;

import org.objectweb.asm.Type;

/**
 * @author Gordon Fraser
 * 
 */
public interface ConstantPool {

	/**
	 * <p>
	 * getRandomString
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getRandomString();
	
	/**
	 * <p>
	 * getRandomType
	 * </p>
	 * 
	 * @return a {@link org.objectweb.asm.Type} object.
	 */
	public Type getRandomType();
	
	/**
	 * <p>
	 * getRandomInt
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getRandomInt();

	/**
	 * <p>
	 * getRandomFloat
	 * </p>
	 * 
	 * @return a float.
	 */
	public float getRandomFloat();

	/**
	 * <p>
	 * getRandomDouble
	 * </p>
	 * 
	 * @return a double.
	 */
	public double getRandomDouble();

	/**
	 * <p>
	 * getRandomLong
	 * </p>
	 * 
	 * @return a long.
	 */
	public long getRandomLong();

	/**
	 * <p>
	 * add
	 * </p>
	 * 
	 * @param object
	 *            a {@link java.lang.Object} object.
	 */
	public void add(Object object);
	
	public String toString();
}
