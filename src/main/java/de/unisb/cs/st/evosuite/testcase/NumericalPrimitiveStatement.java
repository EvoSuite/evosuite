/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;

/**
 * @author fraser
 * 
 */
public abstract class NumericalPrimitiveStatement<T> extends PrimitiveStatement<T> {

	private static final long serialVersionUID = 476613542969677702L;

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public NumericalPrimitiveStatement(TestCase tc, Type type, T value) {
		super(tc, type, value);
	}

	/**
	 * Increase value by smallest possible increment
	 */
	public abstract void increment();

	/**
	 * Decrease value by smallest possible increment
	 */
	public abstract void decrement();

	/**
	 * Change value by delta
	 * 
	 * @param delta
	 */
	public abstract void increment(long delta);

	/**
	 * Change value by delta
	 * 
	 * @param delta
	 */
	public void increment(double delta) {
		increment((long) delta);
	}

	/**
	 * Needed for binary search
	 * 
	 * @param min
	 * @param max
	 */
	public abstract void setMid(T min, T max);

	/**
	 * Is the value >= 0?
	 * 
	 * @return
	 */
	public abstract boolean isPositive();

}
