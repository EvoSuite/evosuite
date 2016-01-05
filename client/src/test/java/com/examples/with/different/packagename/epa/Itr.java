package com.examples.with.different.packagename.epa;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An optimized version of AbstractList.Itr
 */
class Itr<E> implements Iterator<E> {
	/**
	 * 
	 */
	private final ArrayList<E> arrayListAtItr;

	/**
	 * @param arrayList
	 */
	Itr(ArrayList<E> arrayList) {
		this.arrayListAtItr = arrayList;
		this.cursor=0;
		this.lastRet = -1; 
		this.expectedModCount = this.arrayListAtItr.modCount;
	}

	public int cursor; // index of next element to return
	public int lastRet; // index of last element returned; -1 if no such
	public int expectedModCount;

	public boolean hasNext() {
		return cursor != this.arrayListAtItr.size;
	}

	@SuppressWarnings("unchecked")
	public E next() {
		checkForComodification();
		int i = cursor;
		if (i >= this.arrayListAtItr.size)
			throw new NoSuchElementException();
		Object[] elementData = arrayListAtItr.elementData;
		if (i >= elementData.length)
			throw new ConcurrentModificationException();
		cursor = i + 1;
		return (E) elementData[lastRet = i];
	}

	public void remove() {
		if (lastRet < 0)
			throw new IllegalStateException();
		checkForComodification();

		try {
			arrayListAtItr.remove(lastRet);
			cursor = lastRet;
			lastRet = -1;
			expectedModCount = this.arrayListAtItr.modCount;
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
	}

	final void checkForComodification() {
		if (this.arrayListAtItr.modCount != expectedModCount)
			throw new ConcurrentModificationException();
	}
}