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
public class ListItr implements ListIterator<Object> {
	/**
	 * 
	 */
	private final MyArrayList arrayList;
	private int cursor; // index of next element to return
	private int lastRet; // index of last element returned; -1 if no such
	private int expectedModCount;

	public ListItr(MyArrayList arrayList, int index) {

		if (index < 0 || index > arrayList.size())
			throw new IndexOutOfBoundsException("Index: " + index);

		this.arrayList = arrayList;
		this.lastRet = -1;
		this.expectedModCount = this.arrayList.getModCount();
		this.cursor = index;
		
		reportState();
	}

	public boolean hasPrevious() {
		final boolean b = cursor != 0;
		reportState();
		return b;
	}

	public int nextIndex() {
		reportState();
		return cursor;
	}

	// @Pre("(> cursor 0)")
	public int previousIndex() {
		final int i = cursor - 1;
		reportState();
		return i;
	}

	@Override
	public boolean hasNext() {
		boolean superHasNext = super_hasNext();
		reportState();
		return superHasNext;
	}

	@Override
	// @Pre("(< cursor (eval(.size this$0)))")
	public Object next() {
		final Object superNext = super_next();
		reportState();
		return superNext;
	}

	@Override
	// @Pre("(>= lastRet 0)")
	public void remove() {
		super_remove();
		reportState();
	}

	// @SuppressWarnings("unchecked")
	// @Pre("(> cursor 0)")
	public Object previous() {
		checkForComodification();
		int i = cursor - 1;
		if (i < 0)
			throw new NoSuchElementException();
		Object[] elementData = arrayList.elementData;
		if (i >= elementData.length)
			throw new ConcurrentModificationException();
		cursor = i;

		Object object = (Object) elementData[lastRet = i];
		reportState();
		return object;
	}

	// @Pre("(and (>= lastRet 0) (> p0 10))")
	private void setNumber(Integer integer) {
		set((Object) integer);
	}

	public void set(Object e) {
		if (lastRet < 0)
			throw new IllegalStateException();
		checkForComodification();

		try {
			arrayList.set(lastRet, e);
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
		reportState();
	}

	// @Pre("(> p0 0)")
	private void addNumber(Integer integer) {
		add((Object) integer);
	}

	public void add(Object e) {
		checkForComodification();

		try {
			int i = cursor;
			arrayList.add(i, e);
			cursor = i + 1;
			lastRet = -1;
			expectedModCount = this.arrayList.getModCount();
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
		reportState();
	}

	private boolean super_hasNext() {
		final boolean b = cursor != this.arrayList.size;
		return b;
	}

	private Object super_next() {
		checkForComodification();
		int i = cursor;
		if (i >= this.arrayList.size)
			throw new NoSuchElementException();
		Object[] elementData = arrayList.elementData;
		if (i >= elementData.length)
			throw new ConcurrentModificationException();
		cursor = i + 1;
		final Object o = (Object) elementData[lastRet = i];
		return o;
	}

	private void super_remove() {
		if (lastRet < 0)
			throw new IllegalStateException();
		checkForComodification();

		try {
			arrayList.remove(lastRet);
			cursor = lastRet;
			lastRet = -1;
			expectedModCount = this.arrayList.getModCount();
		} catch (IndexOutOfBoundsException ex) {
			throw new ConcurrentModificationException();
		}
	}

	private final void checkForComodification() {
		if (this.arrayList.getModCount() != expectedModCount)
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

	private boolean isStateS127() {
		return isAddEnabled() && isNextEnabled() && isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}
	
	private boolean isStateS95() {
		return isAddEnabled() && isNextEnabled() && !isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}
	
	private boolean isStateS119() {
		return isAddEnabled() && !isNextEnabled() && isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	private boolean isStateS87() {
		return isAddEnabled() && !isNextEnabled() && !isPreviousEnabled() && !isRemoveEnabled() && !isSetEnabled();
	}

	private boolean isStateS511() {
		return isAddEnabled() && isNextEnabled() && isPreviousEnabled() && isRemoveEnabled() && isSetEnabled();
	}

	private boolean isStateS503() {
		return isAddEnabled() && !isNextEnabled() && isPreviousEnabled() && isRemoveEnabled() && isSetEnabled();
	}

	private boolean isStateS479() {
		return isAddEnabled() && isNextEnabled() && !isPreviousEnabled() && isRemoveEnabled() && isSetEnabled();
	}
	
	private void reportState() {
		if (isStateS127())
			reportStateS127();
		else if (isStateS95())
			reportStateS95();
		else if (isStateS119())
			reportStateS119();
		else if (isStateS87())
			reportStateS87();
		else if (isStateS511())
			reportStateS511();
		else if (isStateS503())
			reportStateS503();
		else if (isStateS479())
			reportStateS479();
	}

	private void reportStateS479() {
		
	}

	private void reportStateS503() {
		
	}

	private void reportStateS511() {
		
	}

	private void reportStateS87() {
		
	}

	private void reportStateS119() {
		
	}

	private void reportStateS95() {
		
	}

	private void reportStateS127() {
		
	}
}