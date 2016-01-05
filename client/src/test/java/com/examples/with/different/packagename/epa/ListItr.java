package com.examples.with.different.packagename.epa;

import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An optimized version of AbstractList.ListItr
 */
// @ClassDefinition(builder="(let [a (doto
// (ar.com.maba.tesis.arrayList.ArrayList.) (.add 1) (.add 2) (.add 3))]
// (.listIterator a))",
// invariant = "(and " +
// "(or (= lastRet -1) (>= lastRet 0)) " +
// "(and (<= 0 cursor) (<= cursor (.size this$0))))")
public class ListItr<E> implements ListIterator<E> {
	/**
	 * 
	 */
	private final ArrayList<E> arrayList;
	private int cursor; // index of next element to return
	private int lastRet; // index of last element returned; -1 if no such
	private int expectedModCount;

	ListItr(ArrayList<E> arrayList, int index) {

		if (index < 0 || index > arrayList.size())
			throw new IndexOutOfBoundsException("Index: " + index);

		this.arrayList = arrayList;
		this.lastRet = -1;
		this.expectedModCount = this.arrayList.modCount;
		this.cursor = index;
	}

	public boolean hasPrevious() {
		return cursor != 0;
	}

	public int nextIndex() {
		return cursor;
	}

	// @Pre("(> cursor 0)")
	public int previousIndex() {
		return cursor - 1;
	}

	@Override
	public boolean hasNext() {
		return super_hasNext();
	}

	@Override
	// @Pre("(< cursor (eval(.size this$0)))")
	public E next() {
		return super_next();
	}

	@Override
	// @Pre("(>= lastRet 0)")
	public void remove() {
		super_remove();
	}

	// @SuppressWarnings("unchecked")
	// @Pre("(> cursor 0)")
	public E previous() {
		checkForComodification();
		int i = cursor - 1;
		if (i < 0)
			throw new NoSuchElementException();
		Object[] elementData = arrayList.elementData;
		if (i >= elementData.length)
			throw new ConcurrentModificationException();
		cursor = i;
		return (E) elementData[lastRet = i];
	}

	// @Pre("(and (>= lastRet 0) (> p0 10))")
	public void setNumber(Integer integer) {
		set((E) integer);
	}

	public void set(E e) {
		if (lastRet < 0)
			throw new IllegalStateException();
		checkForComodification();

		try {
			arrayList.set(lastRet, e);
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
	}

	// @Pre("(> p0 0)")
	public void addNumber(Integer integer) {
		add((E) integer);
	}

	public void add(E e) {
		checkForComodification();

		try {
			int i = cursor;
			arrayList.add(i, e);
			cursor = i + 1;
			lastRet = -1;
			expectedModCount = this.arrayList.modCount;
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
	}

	private boolean super_hasNext() {
		return cursor != this.arrayList.size;
	}

	private E super_next() {
		checkForComodification();
		int i = cursor;
		if (i >= this.arrayList.size)
			throw new NoSuchElementException();
		Object[] elementData = arrayList.elementData;
		if (i >= elementData.length)
			throw new ConcurrentModificationException();
		cursor = i + 1;
		return (E) elementData[lastRet = i];
	}

	private void super_remove() {
		if (lastRet < 0)
			throw new IllegalStateException();
		checkForComodification();

		try {
			arrayList.remove(lastRet);
			cursor = lastRet;
			lastRet = -1;
			expectedModCount = this.arrayList.modCount;
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
	}

	private final void checkForComodification() {
		if (this.arrayList.modCount != expectedModCount)
			throw new ConcurrentModificationException();
	}

	private boolean isAddEnabled() {
		// add is always enabled
		return true;
	}

	private boolean isNextEnabled() {
		return cursor < this.arrayList.size;
	}

	private boolean isPreviousEnabled() {
		return cursor - 1 >= 0;
	}

	private boolean isRemoveEnabled() {
		return lastRet >= 0;
	}

	private boolean isSetEnabled() {
		return lastRet >= 0;
	}

	public boolean isState1() {
		return isAddEnabled() && !isNextEnabled() && !isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	public boolean isState2() {
		return isAddEnabled() && isNextEnabled() && !isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	public boolean isState3() {
		return isAddEnabled() && !isNextEnabled() && isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	public boolean isState4() {
		return isAddEnabled() && isNextEnabled() && isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	public boolean isState5() {
		return isAddEnabled() && !isNextEnabled() && isPreviousEnabled() && isRemoveEnabled() && isSetEnabled();
	}

	public boolean isState6() {
		return isAddEnabled() && isNextEnabled() && isPreviousEnabled() && isRemoveEnabled() && isSetEnabled();
	}

	public boolean isState7() {
		return isAddEnabled() && isNextEnabled() && !isPreviousEnabled() && isRemoveEnabled() && isSetEnabled();
	}

}