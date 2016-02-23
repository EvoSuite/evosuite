/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.utils;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Reverses the order of another comparator by reversing the arguments to its
 * {@link #compare(Object, Object) compare} method.
 *
 * @since Commons Collections 2.0
 * @version $Revision: 646777 $ $Date: 2008-04-10 13:33:15 +0100 (Thu, 10 Apr
 *          2008) $
 * @author Henri Yandell
 * @author Michael A. Smith
 * @see java.util.Collections#reverseOrder()
 */
public class ReverseComparator<T> implements Comparator<T>, Serializable {

	/** Serialization version from Collections 2.0. */
	private static final long serialVersionUID = 2858887242028539265L;

	/** The comparator being decorated. */
	private Comparator<T> comparator;

	//-----------------------------------------------------------------------
	/**
	 * Creates a comparator that compares objects based on the inverse of their
	 * natural ordering. Using this Constructor will create a ReverseComparator
	 * that is functionally identical to the Comparator returned by
	 * java.util.Collections.<b>reverseOrder()</b>.
	 *
	 * @see java.util.Collections#reverseOrder()
	 */
	public ReverseComparator() {
		this(null);
	}

	/**
	 * Creates a comparator that inverts the comparison of the given comparator.
	 * If you pass in <code>null</code>, the ReverseComparator defaults to
	 * reversing the natural order, as per
	 * {@link java.util.Collections#reverseOrder()}</b>.
	 *
	 * @param comparator
	 *            Comparator to reverse
	 */
	public ReverseComparator(Comparator<T> comparator) {
		if (comparator != null) {
			this.comparator = comparator;
			//		} else {
			//			this.comparator = ComparableComparator.getInstance();
		}
	}

	//-----------------------------------------------------------------------
	/**
	 * {@inheritDoc}
	 *
	 * Compares two objects in reverse order.
	 */
	@Override
	public int compare(T obj1, T obj2) {
		return comparator.compare(obj2, obj1);
	}

	//-----------------------------------------------------------------------
	/**
	 * {@inheritDoc}
	 *
	 * Implement a hash code for this comparator that is consistent with
	 * {@link #equals(Object) equals}.
	 * @since Commons Collections 3.0
	 */
	@Override
	public int hashCode() {
		return "ReverseComparator".hashCode() ^ comparator.hashCode();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns <code>true</code> iff <i>that</i> Object is is a
	 * {@link Comparator} whose ordering is known to be equivalent to mine.
	 * <p>
	 * This implementation returns <code>true</code> iff
	 * <code><i>object</i>.{@link Object#getClass() getClass()}</code> equals
	 * <code>this.getClass()</code>, and the underlying comparators are equal.
	 * Subclasses may want to override this behavior to remain consistent with
	 * the {@link Comparator#equals(Object) equals} contract.
	 * @since Commons Collections 3.0
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (null == object) {
			return false;
		} else if (object.getClass().equals(this.getClass())) {
			ReverseComparator<T> thatrc = (ReverseComparator<T>) object;
			return comparator.equals(thatrc.comparator);
		} else {
			return false;
		}
	}

}
