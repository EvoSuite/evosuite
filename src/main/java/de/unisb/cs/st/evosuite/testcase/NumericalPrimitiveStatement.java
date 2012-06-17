/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;

/**
 * @author Gordon Fraser
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
