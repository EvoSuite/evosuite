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
/**
 * 
 */
package org.evosuite.utils;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

import org.evosuite.Properties;

/**
 * @author Gordon Fraser
 * 
 */
public class DefaultRandomAccessQueue<T> implements RandomAccessQueue<T> {

	private final Set<T> valueSet = new HashSet<T>();

	private final Queue<T> queue = new ArrayDeque<T>();

	/* (non-Javadoc)
	 * @see org.evosuite.primitives.RandomAccessQueue#restrictedAdd(java.lang.Object)
	 */
	@Override
	public void restrictedAdd(T value) {
		if (!valueSet.contains(value)) {
			queue.add(value);
			valueSet.add(value);
			reduceSize();
		}
	}

	private void reduceSize() {
		if (queue.size() > Properties.DYNAMIC_POOL_SIZE) {
			T value = queue.poll();
			valueSet.remove(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.primitives.RandomAccessQueue#getRandomValue()
	 */
	@Override
	public T getRandomValue() {
		return Randomness.choice(valueSet);
	}

	@Override
	public String toString() {
		String res = new String("[ ");
		Iterator<T> itr = queue.iterator();
		while(itr.hasNext()) {
			Object element = itr.next();
			res += element + " ";
		}
		res += "]";
		return res;
	}

}
