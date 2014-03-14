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
